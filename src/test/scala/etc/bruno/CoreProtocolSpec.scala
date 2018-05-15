package etc.bruno

import akka.TaskResultProtocol
import etc.bruno.core.TaskResultDefinition._
import org.scalatest.{FlatSpec, Matchers}

class CoreProtocolSpec extends FlatSpec with Matchers with TaskResultProtocol {

  import spray.json._

  "TaskResult Definition" should "be parsed from json with all fields" in {
    val json =
      """{
        |  "kind": "mem",
        |  "total": 240,
        |  "used": 130,
        |  "free": 110,
        |  "percentage": 54
        |}""".stripMargin

    val subject: MemTaskResultContent = json.parseJson.convertTo[MemTaskResultContent]

    println(subject)

  }

}
