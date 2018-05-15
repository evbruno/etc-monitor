package akka

object ProtocolsApp extends App with TaskResultProtocol {

  import spray.json._

  val jsonRaw =
    """{
      |  "kind": "mem",
      |  "total": 240,
      |  "used": 130,
      |  "free": 110,
      |  "percentage": 54
      |}""".stripMargin

  val x = jsonRaw.parseJson
  println(x)
}
