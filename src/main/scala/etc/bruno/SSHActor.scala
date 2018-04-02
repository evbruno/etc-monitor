package etc.bruno.ssh

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.jcraft.jsch._
import etc.bruno.ssh.SSHActor._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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
