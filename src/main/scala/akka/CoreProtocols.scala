package akka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import etc.bruno.core._
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}

trait CoreDefinitionProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  import CoreDefinition._

//  implicit val ServerDefinitionFormat = jsonFormat6(ServerDefinition)
//  implicit val MonitorDefinitionFormat = jsonFormat2(MonitorDefinition)
//
//  //implicit val AbstractTaskDefinitionFormat = jsonFormat1(TaskDefinition)
//
//  implicit val TaskDefinitionFormat = new RootJsonFormat[TaskDefinition] {
//
//    override def write(obj: TaskDefinition): JsValue = ???
//    override def read(json: JsValue): TaskDefinition = ???
//
//  }

}

trait UserDefinitionProtocol extends SprayJsonSupport with DefaultJsonProtocol {

//  import UserDefinition._
//
//  implicit val TaskDefinitionFormat = new RootJsonFormat[TaskDefinition] {
//
//    override def write(obj: TaskDefinition): JsValue = ???
//    override def read(json: JsValue): TaskDefinition = ???
//
//  }

}

trait TaskResultProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  import TaskResultDefinition._

  implicit val MemTaskResultContentFormat = jsonFormat4(MemTaskResultContent)
  //implicit val AbstractTaskResultFormat = jsonFormat3(TaskResult)

  implicit val TaskDefinitionFormat = new RootJsonFormat[TaskResult] {

    override def write(obj: TaskResult): JsValue = ???

    override def read(json: JsValue): TaskResult = ???

  }

}