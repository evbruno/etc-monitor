package etc.bruno

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

package object Model {

  sealed abstract class DataTask

  case class DataMemTask(val kind: String,
                         val total: Int,
                         val used: Int,
                         val free: Int) extends DataTask

  case class DataDiskTask(val fileSystem: String,
                          val total: Int,
                          val used: Int,
                          val available: Int,
                          val percentage: Float) extends DataTask

  case class ServerTask(kind: String,
                        data: DataTask)

  case class ServerComponent(alias: String,
                             status: String,
                             tasks: List[ServerTask],
                             lastSuccessTask: String)

}

trait ModelProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  import Model._

  implicit val dataMemTaskFormat = jsonFormat4(DataMemTask)

  implicit val dataDiskTaskFormat = jsonFormat5(DataDiskTask)

  implicit val serverTaskFormat = new RootJsonFormat[ServerTask] {

    override def read(json: JsValue): ServerTask = ???

    override def write(obj: ServerTask): JsValue = {
      val any = obj.data match {
        case s: DataMemTask => ("mem", dataMemTaskFormat.write(s))
        case d: DataDiskTask => ("disk", dataDiskTaskFormat.write(d))
      }

      JsObject(
        "kind" -> JsString(any._1),
        "data" -> any._2
      )
    }

  }

  implicit val serverComponentFormat = jsonFormat4(ServerComponent)

}