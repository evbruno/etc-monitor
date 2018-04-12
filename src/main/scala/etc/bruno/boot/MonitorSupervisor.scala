package etc.bruno.boot


import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import etc.bruno.Configz.{DiskTask, MonitorDefinition, ServerDefinition, TaskDefinition}
import etc.bruno.ConfigzProtocol
import spray.json.DefaultJsonProtocol

import scala.collection.mutable.{ListBuffer => LBuffer, Map => MMap}
import scala.concurrent.duration._
import scala.util.Random

case object MonitorHeartBeat

case class MonitorHeartBeatResponse(serverDefinition: ServerDefinition,
                                    taskDefinition: TaskDefinition,
                                    content: String,
                                    time: Long,
                                    date: java.util.Date = new java.util.Date())

//case class MonitorHeartBeatNOResponseYet(override val serverDefinition: ServerDefinition,
//                                         override val taskDefinition: TaskDefinition)
//  extends MonitorHeartBeatResponse(serverDefinition, taskDefinition, "", -1)

case class MonitorTaskRequest(serverAlias: String)

class TaskException(msg: String) extends RuntimeException(msg)

class MonitorDefinitionActor(monitorDefinition: MonitorDefinition) extends Actor with ActorLogging {

  lazy val monDef =  monitorDefinition.copy(
    servers = monitorDefinition.servers.map { m =>
      m.copy(password = None)
    }
  )

  def receive = {
    case 'all =>
      log.info(s"MonitorDefinitionActor received 'all ")
      sender ! monDef

    case wtf =>
      log.info(s"MonitorDefinitionActor unknown message: $wtf")
  }
}

class MonitorSupervisor(accumulator: ActorRef, servers: Seq[ServerDefinition]) extends Actor with ActorLogging {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 minute, loggingEnabled = true) {

      // FIXME
      case _: TaskException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

  // FIXME watch your kids die !
  val children: MMap[ServerDefinition, ActorRef] = MMap()

  override def preStart(): Unit = {
    log.info(s"MonitorSupervisor preStart for ${servers.length} servers ")

    servers.foreach { server =>
      val newRef = context.actorOf(Props(new TaskMonitorSupervisor(accumulator, server)), server.alias)
      children += (server -> newRef)
    }
  }

  def receive = {

    case MonitorHeartBeat =>
      log.info(s"MonitorSupervisor receive heart beat, will propagate to its ${children.size} children")
      children.values.foreach(_ ! MonitorHeartBeat)

    case wtf =>
      log.info(s"MonitorSupervisor unknown message: $wtf")
  }

}

class TaskMonitorSupervisor(accumulator: ActorRef, server: ServerDefinition) extends Actor with ActorLogging {

  val children: MMap[TaskDefinition, ActorRef] = MMap()

  override def preStart(): Unit = {
    log.info(s"MonitorSupervised preStart for ${server.tasks.length} tasks ")

    server.tasks.zipWithIndex.foreach { case (task, idx) =>
      val newRef = context.actorOf(Props(new SimpleTaskActor(server, task)), s"task_${task.`type`}_$idx")
      children += (task -> newRef)
    }
  }

  def receive = {

    case MonitorHeartBeat =>
      log.info(s"MonitorSupervised receive heart beat, will propagate to its ${children.size} children")
      children.values.foreach(_ ! MonitorHeartBeat)

    case r: MonitorHeartBeatResponse =>
      log.info(s"MonitorSupervised receive heart beat response, will accumulate")
      accumulator ! r

    case wtf =>
      log.info(s"MonitorSupervised unknown message: $wtf")
  }

}

class SimpleTaskActor(server: ServerDefinition, task: TaskDefinition) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"SimpleTaskActor preStart $task")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.info(s"SimpleTaskActor preRestart $task, reason: $reason")
  }

  def receive = {
    case MonitorHeartBeat =>
      log.info(s"SimpleTaskActor receive heart beat, will process something...")

      //if (new Random().nextBoolean())
      //  throw new TaskException(s"Random task $task will die !")

      // fake process
      val wait = new Random().nextInt(50) * 10l
      val content = s"Task $task will wait for $wait millis. Now: ${new java.util.Date()}"
      println(s">>> $content")
      Thread.sleep(wait)
      sender ! MonitorHeartBeatResponse(server, task, content, wait, new java.util.Date())

    case wtf =>
      log.info(s"SimpleTaskActor unknown message: $wtf")
  }

}

class TaskResultAccumulatorInMemory(monitorDef: MonitorDefinition) extends Actor with ActorLogging {

  type ResultKey = (ServerDefinition, TaskDefinition)

  lazy val success: MMap[ResultKey, MonitorHeartBeatResponse] = MMap()

  lazy val history: MMap[ResultKey, LBuffer[MonitorHeartBeatResponse]] = MMap()
  //lazy val history: MMap[ResultKey, LBuffer[Option[MonitorHeartBeatResponse]]] = MMap()


  def receive = {

    case response : MonitorHeartBeatResponse =>
      log.info(s"TaskResultAccumulatorInMemory received ${response.content} from $sender")

      val resultKey = (response.serverDefinition, response.taskDefinition)

      success.put(resultKey, response)

      val results = history.getOrElseUpdate(resultKey, LBuffer())
      results += response


    case MonitorTaskRequest(serverAlias) =>
      log.info(s"TaskResultAccumulatorInMemory received request for server $serverAlias")
      val server = monitorDef.servers.find(_.alias == serverAlias).get

      val results = server.tasks.map { task =>
        success.getOrElse((server, task), None)
      }

      sender ! results

    case wtf =>
      log.info(s"TaskResultAccumulatorInMemory unknown message: $wtf")
  }
}


trait MonitorHeartBeatProtocol extends SprayJsonSupport with DefaultJsonProtocol with ConfigzProtocol {

  import java.text._
  import java.util._
  import scala.util.Try
  import spray.json._

  implicit object DateFormat extends JsonFormat[Date] {
    def write(date: Date) = JsString(dateToIsoString(date))
    def read(json: JsValue) = json match {
      case JsString(rawDate) =>
        parseIsoDateString(rawDate)
          .fold(deserializationError(s"Expected ISO Date format, got $rawDate"))(identity)
      case error => deserializationError(s"Expected JsString, got $error")
    }
  }

  private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
    override def initialValue() = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  }

  private def dateToIsoString(date: Date) =
    localIsoDateFormatter.get().format(date)

  private def parseIsoDateString(date: String): Option[Date] =
    Try{ localIsoDateFormatter.get().parse(date) }.toOption

  implicit val MonitorHeartBeatResponseFormat = jsonFormat5(MonitorHeartBeatResponse)

}