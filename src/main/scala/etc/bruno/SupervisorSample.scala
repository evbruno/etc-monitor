
import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.stream.ActorMaterializer

import scala.collection.mutable.{Map => MMap}
import scala.concurrent.duration._
import scala.io.StdIn

object SupervisorSample extends App {

  implicit val system = ActorSystem("sample")

  val supervisor = system.actorOf(Props[Supervisor], "supervisor")

  // running samples:
  // if < 0, IllegalArgumentException, child Stop for that $num
  // if == 0, CustomResumeException, child will Resume for that $num
  // if % 0 == 0, CustomRestartException, child will Restart for that $num
  // Supervisor gets a Double num

  supervisor ! SampleCalculate(1)
  supervisor ! SampleCalculate(2)
  supervisor ! SampleCalculate(3)
  supervisor ! SampleCalculate(2)
  supervisor ! SampleCalculate(1)
  supervisor ! SampleCalculate(-1)
  supervisor ! SampleCalculate(0)
  supervisor ! SampleCalculate(-1)
  supervisor ! SampleCalculate(0)

  println("Press RETURN to stop...")

  StdIn.readLine()
  system.terminate()
}

case object SampleAction

case class SampleCalculate(num: Int)

case class SampleResult(num: Double)

class CustomResumeException(msg: String) extends RuntimeException(msg)

class CustomRestartException(num: Int) extends RuntimeException(s"CustomRestart error for num: $num")

class Supervisor extends Actor with ActorLogging {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 minute, loggingEnabled = true) {

      case _: IllegalArgumentException => Stop
      case _: CustomResumeException => Resume
      case _: CustomRestartException => Restart

      case _: Exception => Escalate
    }

  val children: MMap[Int, ActorRef] = MMap()

  def receive = {

    case SampleCalculate(num) =>
      log.info(s"Supervisor receive SampleCalculate ${num}")
      val actor = children.getOrElseUpdate(num, context.actorOf(Props(new Child(num)), s"child_$num"))
      actor ! SampleAction

    case SampleResult(num) =>
      log.info(s"Supervisor receive SampleResult ${num}")
  }

}

class Child(val num: Int) extends Actor with ActorLogging {

  log.info(s"Child creation ${num}")

  var actionCount = 0

  override def preStart(): Unit = {
    log.info(s"Child will preStart $num actionCount $actionCount")
    super.preStart()
  }

  override def preRestart(cause: Throwable, msg: Option[Any]): Unit = {
    actionCount += 1
    log.info(s"Child will preRestart num: $num actionCount: $actionCount cause: $cause")
    super.preRestart(cause, msg)
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info(s"Child will postRestart num: $num actionCount: $actionCount reason: $reason")
    super.postRestart(reason)
  }

  def receive = {
    case SampleAction =>
      log.info(s"Child SampleAction num: $num")
      actionCount += 1

      if (num < 0)
        throw new IllegalArgumentException(s"num ($num) can not be negative")
      else if (num == 0)
        throw new CustomResumeException(s"I don't like zero! I told ya $actionCount times !")
      else if (num % 2 == 0)
        throw new CustomRestartException(num)
      else
        sender ! SampleResult(Math.PI / num)
  }
}