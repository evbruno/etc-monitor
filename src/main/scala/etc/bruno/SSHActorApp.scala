package etc.bruno

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import etc.bruno.ssh.SSHActor
import etc.bruno.ssh.SSHActor._

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
