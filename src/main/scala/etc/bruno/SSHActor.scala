package etc.bruno.ssh

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.jcraft.jsch._
import etc.bruno.ssh.SSHActor._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.StdIn

object SSHActorApp extends App {

  implicit val system = ActorSystem("ssh-system")

  val credentials = SSHCredentials("root", "screencast", "10.42.12.136", 2222)

  val ssh = system.actorOf(Props(new SSHActor(credentials)), "ssh")
  val consumer = system.actorOf(Props(new SSHActorConsumer(ssh)), "consumer")

  implicit val executionContext = system.dispatcher

  val rate = 30.seconds
  val delay = 2.seconds

  system.scheduler.schedule(delay, rate, consumer, "disk")
  system.scheduler.schedule(delay, rate, consumer, "top")
  system.scheduler.schedule(delay, rate, consumer, "free")
  system.scheduler.schedule(delay, rate, consumer, "proc")

  println("Press RETURN to stop...")

  StdIn.readLine()
  system.terminate()

}

object SSHActor {

  case class SSHCredentials(user: String, password: String, host: String, port: Int = 22)

  case class SSHCommand(cmd: String)

  case class SSHResult(cmd: SSHCommand, result: String)

  protected[ssh] case class RequestSession(source: ActorRef, cmd: SSHCommand)

  protected[ssh] case class ExecuteRequest(source: ActorRef, session: Session, cmd: SSHCommand)

  protected[ssh] case class ProcessedRequest(source: ActorRef, session: Session, cmd: SSHCommand, content: String)

  protected[ssh] case class ReleaseSession(session: Session, cmd: SSHCommand)

}

class SSHActor(creds: SSHCredentials) extends Actor with ActorLogging with SSHTrait {

  implicit val timeout = Timeout(5 seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case cmd: SSHCommand =>
      log.info(s"""SSHCommand receive "$cmd"""")
      self ? RequestSession(sender, cmd) pipeTo self

    case RequestSession(source, cmd) =>
      log.info(s"""RequestSession receive "$cmd"""")
      val sess = createSession(creds)
      self ! ExecuteRequest(source, sess, cmd)

    case ExecuteRequest(source, session, cmd) =>
      log.info(s"""ExecuteRequest will run "$cmd"""")
      val content = execAsync(session, cmd.cmd)
      content.map(ProcessedRequest(source, session, cmd, _)) pipeTo self

    case ProcessedRequest(source, session, cmd, content) =>
      log.info(s"""Processed run"$cmd" and got "$content"""")

      self ! ReleaseSession(session, cmd)
      source ! SSHResult(cmd, content)

    case ReleaseSession(session, cmd) =>
      log.info(s"""ReleaseSession for "$cmd"""")
      session.disconnect()
  }

}

protected[ssh] trait SSHTrait {

  private val jsch = new JSch

  def createSession(creds: SSHCredentials) = {
    val session = jsch.getSession(creds.user, creds.host, creds.port)
    session.setPassword(creds.password)

    session.setConfig("StrictHostKeyChecking", "no")
    session.connect()

    session
  }

  def exec(session: Session, cmd: String): String = {
    val channel: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec]
    channel.setCommand(cmd)
    channel.connect()

    val content = scala.io.Source.fromInputStream(channel.getInputStream).mkString

    channel.disconnect()
    content
  }

  def execAsync(session: Session, cmd: String)(implicit ec: ExecutionContext) =
    Future {
      exec(session, cmd)
    }

}

class SSHActorConsumer(ssh: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case "disk" => ssh ! SSHCommand("df -h | grep /dev/sda3")

    case "top" => ssh ! SSHCommand("top -b -d0 -n1 | grep 'load average'")

    case "free" => ssh ! SSHCommand("free -m -w")

    case "proc" => ssh ! SSHCommand("nproc")

    case SSHResult(SSHCommand("nproc"), content) => log.info(s"""Consumer wow... we have "${content.trim.toInt}" nprocs! """)

    case SSHResult(cmd, content) => log.info(s"""Consumer got cmd $cmd, with content: "$content""")
  }

}