// look ma, no packages !!!
// package etc.bruno.boot

import etc.bruno.boot.MonitorConstants.MonitorDefinitionFileName
import etc.bruno.boot.{MonitorDefinitionLoader, WebServer}

object BootMonitor extends App {

  //MonitorDefinitionLoader.loadDefinition(MonitorDefinitionFileName)
  MonitorDefinitionLoader.loadDefinitionFromString(Spike.json)
  WebServer.boot()

}


object Spike {
  val json =
    """{
      |  "refreshRate" : 60,
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
      |    }
      |  ]
      |}""".stripMargin

}