package etc.bruno

object Configz {

  type Percentage = Float

  case class ServerDefinition(alias: String,
                              host: Option[String] = None,
                              user: Option[String] = None,
                              password: Option[String] = None,
                              port: Option[Int] = None)

  abstract sealed class TaskDefinition(val `type`: String)

  case class MemTask(threshold: Option[Percentage] = None) extends TaskDefinition("mem")

  case class DiskTask(fileSystem: String, threshold: Option[Percentage] = None) extends TaskDefinition("disk")

  case class LoadAverageTask(threshold: Option[Float] = None) extends TaskDefinition("load-average")

  case class GetUrlValidation(`type`: String, value: Any)

  case class GetUrlTask(address: String, validation: GetUrlValidation) extends TaskDefinition("url-get")

}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

trait ConfigzProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  import scala.language.implicitConversions

//  implicit def stringToJsTring(s: String): JsValue = JsString(s)
//
//  implicit def optNumberToJs(num: Option[Float]): JsValue = num.map(JsNumber(_)).getOrElse(JsNull)

  import Configz._

  implicit val ServerDefinitionFormat = jsonFormat5(ServerDefinition)

  implicit val DiskTaskFormat0 = jsonFormat2(DiskTask)

  implicit val MemTaskFormat0 = jsonFormat1(MemTask)

  private def isTypeOf(json: JsValue, `type`: String): Boolean = {
    val objs = json.asJsObject.getFields("type")
    objs.size == 1 && objs(0) == JsString(`type`)
  }

  implicit val TaskDefinitionFormat = new RootJsonFormat[TaskDefinition] {

    override def read(json: JsValue): TaskDefinition = json match {
      case JsObject(_) if isTypeOf(json, "disk") => DiskTaskFormat0.read(json)
      case JsObject(_) if isTypeOf(json, "mem") => MemTaskFormat0.read(json)
      case _ => throw new DeserializationException("Invalid DiskTask")
    }

    override def write(obj: TaskDefinition): JsValue = {
      obj match {
        case t @ DiskTask(_, _) =>
          val ret = DiskTaskFormat0.write(t).asJsObject
          JsObject(ret.fields + ("type" -> JsString("disk")))
        case t @ MemTask(_) =>
          val ret = MemTaskFormat0.write(t).asJsObject
          JsObject(ret.fields + ("type" -> JsString("mem")))
        case _ => throw new DeserializationException("Invalid Task")
      }
    }
  }

//  implicit val MemTaskFormat = new RootJsonFormat[MemTask] {
//
//    override def read(json: JsValue): MemTask = json match {
//      case JsObject(_) if isTypeOf(json, "mem") => MemTaskFormat0.read(json)
//      case _ => throw new DeserializationException("Invalid MemTask")
//    }
//
//    override def write(obj: MemTask): JsValue = {
//      JsObject(
//        "type" -> "mem",
//        "threshold" -> obj.threshold
//      )
//    }
//  }
//
//  implicit val DiskTaskFormat = new RootJsonFormat[DiskTask] {
//
//    override def read(json: JsValue): DiskTask = json match {
//      case JsObject(_) if isTypeOf(json, "disk") => DiskTaskFormat0.read(json)
//      case _ => throw new DeserializationException("Invalid DiskTask")
//    }
//
//    override def write(obj: DiskTask): JsValue = {
//      val ret = DiskTaskFormat0.write(obj).asJsObject
//      JsObject(ret.fields + ("type" -> JsString("disk")))
//    }
//  }

}
