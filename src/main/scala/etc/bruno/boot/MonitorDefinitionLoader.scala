package etc.bruno.boot

import java.io.File

import etc.bruno.Configz.MonitorDefinition
import etc.bruno.ConfigzProtocol
import spray.json.JsonParser.ParsingException

import scala.util.{Failure, Success, Try}

object MonitorDefinitionLoader extends ConfigzProtocol {

  // FIXME replace sysout for log

  private var monitorDef: Option[MonitorDefinition] = None

  import spray.json._

  def loadDefinition(fileName: String) {
    println(s"[etc-monitor] Will load definitions from file: $fileName")

    val file = new File(fileName)

    if (!file.exists() || file.length() < 1) {
      Console.err.println(s"[etc-monitor] $fileName not found. Aborting !")
      throw new RuntimeException(s"[etc-monitor] $fileName not found. Aborting !")
    }

    val text = io.Source.fromFile(file).mkString

    val monitor = toJSON(text)

    monitor match {
      case Success(m) =>
        monitorDef = Some(m)
        val summary = monitorDefinition.servers.map(s => s"${s.alias} (${s.tasks.length} tasks)").mkString(", ")
        println(s"[etc-monitor] Definitions loaded from file: $fileName. ")
        println(s"[etc-monitor] The monitored servers are: $summary")
      case Failure(f) => {
        Console.err.println(s"[etc-monitor] Error loading $fileName. Aborting ! Reason: ${f.getMessage}")
        throw new RuntimeException(f)
      }
    }
  }

  def loadDefinitionFromString(text: String) {
    val monitor = toJSON(text)

    monitor match {
      case Success(m) =>
        monitorDef = Some(m)
        val summary = monitorDefinition.servers.map(s => s"${s.alias} (${s.tasks.length} tasks)").mkString(", ")
        println(s"[etc-monitor] Definitions loaded from text. ")
        println(s"[etc-monitor] The monitored servers are: $summary")
      case Failure(f) => {
        Console.err.println(s"[etc-monitor] Error loading text. Aborting ! Reason: ${f.getMessage}")
        throw new RuntimeException(f)
      }
    }
  }

  lazy val monitorDefinition: MonitorDefinition = {
    monitorDef match {
      case None =>
        throw new RuntimeException("Monitor not initialized yet")
      case Some(monitor) =>
        monitor
    }
  }

  private def toJSON(json: String): Try[MonitorDefinition] = {
    try {
      val monitor = json.parseJson.convertTo[MonitorDefinition]
      Success(monitor)
    } catch {
      case rt: ParsingException => Failure(rt)
    }
  }
}


object MonitorConstants {

  val MonitorDefinitionFileName = "monitor.json"

}