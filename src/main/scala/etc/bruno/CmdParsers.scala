package etc.bruno

import etc.bruno.CmdParsers._

import scala.util.{Failure, Success, Try}

object CmdParsers {

  val LoadAverageCmd = "top -b -d0 -n1 " :: "grep 'load average'" :: Nil

  val MemoryCmd = "free -m " :: "grep 'Mem:'" :: Nil

  val DiskCmd = (fileSystem: String) => "df -h" :: s"grep '$fileSystem'" :: Nil

  def strToFloat(in: String): Float = in.replaceAll(",", "").toFloat

  def strToInt(in: String): Int = in.replaceAll("\\D", "").toInt

}

trait LoadAverageCmd {

  type LoadAverageResult = (Float, Float, Float)

  def parse(in: String): Try[LoadAverageResult] = {
    try {
      val nums = in.split("\\s+").takeRight(3).map(strToFloat)
      Success(nums(0), nums(1), nums(2))
    } catch {
      case e: RuntimeException => Failure(e)
    }
  }

}

trait MemoryCmd {

  type MemoryResult = (Int, Int, Int)

  def parse(in: String): Try[MemoryResult] = {
    try {
      val nums = in.split("\\s+").drop(1).take(3).map(_.toInt)
      Success(nums(0), nums(1), nums(2))
    } catch {
      case e: RuntimeException => Failure(e)
    }
  }

}

trait DiskCmd {

  type DiskResult = (String, String, String, String, Float)

  def parse(in: String): Try[DiskResult] = {
    try {
      val cols = in.split("\\s+").take(5).map(_.trim)
      Success(cols(0), cols(1), cols(2), cols(3), strToInt(cols(4)))
    } catch {
      case e: RuntimeException => Failure(e)
    }
  }

}