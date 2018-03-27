package etc.bruno

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import etc.bruno.Model.{ServerComponent, ServerTask}

import scala.io.StdIn

object WebServer extends App with EnableCORSDirectives {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._

  implicit val system = ActorSystem("sys-monitor")
  implicit val materializer = ActorMaterializer()

  // needed for the future flatMap/onComplete in the end

  implicit val executionContext = system.dispatcher

  // actors cfg

  import akka.pattern.ask
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout = Timeout(120 seconds)

  val counter = system.actorOf(Props[MyCounterActor], "counter")
  val fakeApi = system.actorOf(Props[MyFakeAPI], "api")

  implicit val serverComponentFormat = jsonFormat2(ApiCount)

  val staticRoute = path("") {
    get {
      getFromResource("index.html")
    }
  } ~ path("main.js") {
    get {
      getFromResource("main.js")
    }
  } ~ pathPrefix("static") {
    get {
      getFromResourceDirectory("static")
    }
  }

  val helloWorldRoute = path("hello") {
    get {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    }
  }

  val apiRoute = enableCORS {
    path("api" / "v1") {
      get {
        complete((counter ? "count").mapTo[ApiCount])
      }
    } ~ path("api" / "v2") {
      get {
        complete((fakeApi ? "tick").mapTo[List[ServerComponent]])
      }
    }
  }

  val route = staticRoute ~ apiRoute ~ helloWorldRoute

  // run server

  val port = 9999
  val bindingFuture = Http().bindAndHandle(route, "localhost", port)

  println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

// Actors

case class ApiCount(val count: Int, val now: String)

class MyCounterActor extends Actor with ActorLogging {

  private var apiCounter = 0

  override def preStart(): Unit = {
    log.info("preStart")
  }

  override def receive: Receive = {
    case "count" => {
      log.info(s"count request from $sender, current value $apiCounter")
      apiCounter += 1
      sender() ! ApiCount(apiCounter, new java.util.Date().toString)
    }
  }
}

class MyFakeAPI extends Actor with ActorLogging {

  def receive = {
    case "tick" =>
      log.info(s"fake api request from $sender")
      sender() ! values
  }

  private def values = List(
    ServerComponent("localhost-0", "offline", List.empty, "none"),
    ServerComponent("localhost", "online", tasks, new java.util.Date().toString)
  )

  private def tasks: List[ServerTask] = {
    List(
      ServerTask("mem", Map(
        "kind" -> "ram",
        "total" -> "16000",
        "used" -> "12000",
        "free" -> "4000")),
      ServerTask("disk", Map(
        "fileSystem" -> "/dev/sda1",
        "total" -> "240000",
        "used" -> "40000",
        "available" -> "20000",
        "percentage" -> "75.5%")),
      ServerTask("disk", Map(
        "fileSystem" -> "/dev/sda3",
        "total" -> "40000",
        "used" -> "2000",
        "available" -> "20000",
        "percentage" -> "50%"))
    )
  }

}