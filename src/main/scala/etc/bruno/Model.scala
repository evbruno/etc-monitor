package etc.bruno

package object Model {

  import spray.json.DefaultJsonProtocol._

  case class ServerTask(kind: String,
                        data: Map[String, String])

  case class ServerComponent(alias: String,
                             status: String,
                             tasks: List[ServerTask],
                             lastSuccessTask: String)

  implicit val serverTaskFormat = jsonFormat2(ServerTask)
  implicit val serverComponentFormat = jsonFormat4(ServerComponent)

}