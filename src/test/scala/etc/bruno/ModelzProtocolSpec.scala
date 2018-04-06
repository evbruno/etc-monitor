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
        |  "user": "root",
        |  "tasks": []
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

//    assertThrows[spray.json.DeserializationException] {
//      json.parseJson.convertTo[MemTask]
//    }

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

  "Whole server" should "be parsed" in {
    val json =
      """{
        |  "alias": "docker-sshd",
        |  "password": "screencast",
        |  "host": "127.0.0.1",
        |  "port": 2222,
        |  "user": "root",
        |  "tasks" : [
        |    {"type":"mem", "threshold":50.0},
        |    {"type":"mem"},
        |    {"type":"disk", "fileSystem":"/dev/sda3"},
        |    {"type":"disk", "fileSystem":"/dev/sda2","threshold":11.22}
        |  ]
        |}""".stripMargin

    val server = json.parseJson.convertTo[ServerDefinition]

    server.alias should be("docker-sshd")

    server.tasks.size should be (4)

    server.tasks(0) should be(MemTask(Some(50)))
    server.tasks(1) should be(MemTask())
    server.tasks(2) should be(DiskTask("/dev/sda3"))
    server.tasks(3) should be(DiskTask("/dev/sda2", Some(11.22f)))

  }

  "Whole configuration root " should "be parsed" in {
    val json =
      """[
        |  {
        |    "alias": "docker-sshd",
        |    "password": "screencast",
        |    "host": "127.0.0.1",
        |    "port": 2222,
        |    "user": "root",
        |    "tasks" : [
        |      {"type":"mem", "threshold":50.0},
        |      {"type":"mem"},
        |      {"type":"disk", "fileSystem":"/dev/sda3"},
        |      {"type":"disk", "fileSystem":"/dev/sda2","threshold":11.22}
        |    ]
        |  },
        |  {
        |    "alias": "localhost",
        |    "tasks" : [
        |        {"type":"mem"}
        |      ]
        |  },
        |  {
        |    "alias": "localhost1",
        |    "tasks" : [ ]
        |  }
        |]""".stripMargin

    val servers = json.parseJson.convertTo[List[ServerDefinition]]

    servers.length should be(3)

    val s0 = servers(0)

    s0.alias should be("docker-sshd")
    s0.tasks.size should be (4)
    s0.tasks(0) should be(MemTask(Some(50)))
    s0.tasks(1) should be(MemTask())
    s0.tasks(2) should be(DiskTask("/dev/sda3"))
    s0.tasks(3) should be(DiskTask("/dev/sda2", Some(11.22f)))

    val s1 = servers(1)

    s1.alias should be("localhost")
    s1.tasks.size should be (1)
    s1.tasks(0) should be(MemTask())

    val s2 = servers(2)

    s2.alias should be("localhost1")
    s2.tasks.size should be (0)
  }

  "Monitor definition" should "be parsed" in {
    val json =
      """{
        |  "refreshRate" : 10,
        |  "servers" : [
        |    {
        |      "alias": "docker-sshd",
        |      "password": "screencast",
        |      "host": "127.0.0.1",
        |      "port": 2222,
        |      "user": "root",
        |      "tasks" : [
        |        {"type":"mem", "threshold":50.0},
        |        {"type":"mem"},
        |        {"type":"disk", "fileSystem":"/dev/sda3"},
        |        {"type":"disk", "fileSystem":"/dev/sda2","threshold":11.22}
        |      ]
        |    },
        |    {
        |      "alias": "localhost",
        |      "tasks" : [
        |          {"type":"mem"}
        |        ]
        |    },
        |    {
        |      "alias": "localhost1",
        |      "tasks": []
        |    }
        |  ]
        |}""".stripMargin

    val monitor = json.parseJson.convertTo[MonitorDefinition]
    monitor.refreshRate should be (Some(10))

    val servers = monitor.servers
    servers.length should be(3)

    val s0 = servers(0)

    s0.alias should be("docker-sshd")
    s0.tasks.size should be (4)

    val s1 = servers(1)
    s1.alias should be("localhost")
    s1.tasks.size should be (1)

    val s2 = servers(2)
    s2.alias should be("localhost1")
    s2.tasks.size should be (0)
  }

  "Minimum Monitor definition" should "be parsed" in {
    val json =
      """{
        |  "servers" : []
        |}""".stripMargin

    val monitor = json.parseJson.convertTo[MonitorDefinition]

    monitor.refreshRate should be (None)

    val servers = monitor.servers

    servers.length should be(0)
  }

}
