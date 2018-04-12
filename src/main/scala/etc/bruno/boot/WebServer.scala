package etc.bruno.boot

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, getFromResource, getFromResourceDirectory, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import etc.bruno.Configz.MonitorDefinition
import etc.bruno.{ConfigzProtocol, EnableCORSDirectives}

import scala.concurrent.duration._
import scala.io.StdIn

object WebServer extends ConfigzProtocol with EnableCORSDirectives with MonitorHeartBeatProtocol {

  def boot(): Unit = {

    implicit val system = ActorSystem("etc-monitor")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val apiRoute = hireAllActors(MonitorDefinitionLoader.monitorDefinition)

    val staticRoute: Route = path("") {
      get {
        getFromResource("index.html")
      }
    } ~ path("main.js") {
      get {
        getFromResource("main.js")
      }
    } ~ path("my_worker.js") {
      get {
        getFromResource("my_worker.js")
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

    val route = staticRoute ~ helloWorldRoute ~ apiRoute

    val port = 7788
    val host = "localhost"

    val bindingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  private def hireAllActors(monitorDef: MonitorDefinition)(implicit system: ActorSystem): Route = {

    //import MonitorHeartBeat._

    import akka.util.Timeout

    implicit val executionContext = system.dispatcher
    implicit val timeout = Timeout(120 seconds)

    val accumulatorInMemory = system.actorOf(Props(new TaskResultAccumulatorInMemory(monitorDef)), "accum")
    val supervisor = system.actorOf(Props(new MonitorSupervisor(accumulatorInMemory, monitorDef.servers)), "monitor")
    val rate = monitorDef.refreshRate.getOrElse(30).seconds
    system.scheduler.schedule(0.seconds, rate, supervisor, MonitorHeartBeat)
    system.log.info(s"Monitor refresh rate set to $rate")

    val monitorDefActor = system.actorOf(Props(new MonitorDefinitionActor(monitorDef)), "route66")

    enableCORS {
      pathPrefix("api") {
        path("servers") {
          get {
            complete((monitorDefActor ? 'all).mapTo[MonitorDefinition])
          }
        } ~ path("tasks" / Segment) { serverAlias =>
          get {
            complete((accumulatorInMemory ? MonitorTaskRequest(serverAlias)).mapTo[Seq[MonitorHeartBeatResponse]])
          }
        }
      }
    }
  }

}
