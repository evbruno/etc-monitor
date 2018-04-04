package etc.bruno

import etc.bruno.Configz._
import org.scalatest.{FlatSpec, Matchers}

class ModelzProtocolSpec extends FlatSpec with Matchers with ConfigzProtocol {

  import spray.json._

  "Server Definition" should "be parsed from json with all fields" in {
    val json =
      """{
        |  "alias": "docker-sshd",
        |  "password": "screencast",
        |  "host": "127.0.0.1",
        |  "port": 2222,
        |  "user": "root"
        |}""".stripMargin

    val subject: ServerDefinition = json.parseJson.convertTo[ServerDefinition]

    subject.alias should be("docker-sshd")
    subject.host should be(Some("127.0.0.1"))
    subject.password should be(Some("screencast"))
    subject.port should be(Some(2222))
    subject.user should be(Some("root"))
  }

  "Memory Task" should "be parsed from json with all fields" in {
    val json =
      """{
        |  "type": "mem",
        |  "threshold": 12.34
        |}""".stripMargin

    val subject: MemTask = json.parseJson.convertTo[MemTask]

    subject.`type` should be("mem")
    subject.threshold shouldBe (Some(12.34f))
  }

  "Memory Task" should "be parsed from json with required fields" in {
    val json =
      """{
        |  "type": "mem"
        |}""".stripMargin

    val subject: MemTask = json.parseJson.convertTo[MemTask]

    subject.`type` should be("mem")
    subject.threshold shouldBe empty
  }

  "Disk Task" should "be parsed from json with all fields" in {
    val json =
      """{
        |  "type": "disk",
        |  "fileSystem" : "/dev/sda3",
        |  "threshold": 70.5
        |}""".stripMargin

    val subject: DiskTask = json.parseJson.convertTo[DiskTask]

    subject.`type` should be("disk")
    subject.fileSystem should be("/dev/sda3")
    subject.threshold shouldBe (Some(70.5f))
  }

  "Disk Task" should "be parsed from json with required fields" in {
    val json =
      """{
        |  "type": "disk",
        |  "fileSystem" : "/dev/sda2"
        |}""".stripMargin

    val subject: DiskTask = json.parseJson.convertTo[DiskTask]

    subject.`type` should be("disk")
    subject.fileSystem should be("/dev/sda2")
    subject.threshold shouldBe empty
  }

  "Memory Task" should "fail be parsed from json with invalid fields" in {
    val json =
      """{
        |  "type": "disk",
        |  "threshold": 70.5
        |}""".stripMargin

    assertThrows[spray.json.DeserializationException] {
      json.parseJson.convertTo[MemTask]
    }

  }

  "Bunch of tasks" should "be parsed" in {
    val json =
      """[{"threshold":50.0,"type":"mem"},
        |{"type":"mem"},
        |{"fileSystem":"/dev/sda3","type":"disk"},
        |{"fileSystem":"/dev/sda3","threshold":11.220000267028809,"type":"disk"}
        |]""".stripMargin 

    val tasks = json.parseJson.convertTo[Seq[TaskDefinition]]

    tasks.length should be(4)

    tasks(0) should be(MemTask(Some(50)))

  }

}
