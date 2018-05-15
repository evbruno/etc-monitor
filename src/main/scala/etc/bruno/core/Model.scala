package etc.bruno.core

package object CoreDefinition {

  abstract sealed class TaskDefinition(val `@type`: String)

  case class MemTask(val threshold: Option[Float] = None)
    extends TaskDefinition("mem")

  case class DiskTask(val fileSystem: String,
                      val threshold: Option[Float] = None)
    extends TaskDefinition("disk")

  case class LoadAverageTask(
                              val threshold: Option[Float] = None
                            ) extends TaskDefinition("load-average")

  case class ServerDefinition(val alias: String,
                              val host: Option[String] = None,
                              val user: Option[String] = None,
                              val password: Option[String] = None,
                              val port: Option[Int] = None,
                              val tasks: Seq[TaskDefinition] = Seq.empty)

  case class MonitorDefinition(val refreshRate: Option[Int] = None,
                               val servers: Seq[ServerDefinition])

}

package object UserDefinition {

  abstract sealed class TaskDefinition(val `@type`: String, val `@uuid`: String)

  case class MemTask(uid: String,
                     val threshold: Option[Float] = None)
    extends TaskDefinition("mem", uid)

  case class DiskTask(uid: String,
                      val fileSystem: String,
                      val threshold: Option[Float] = None)
    extends TaskDefinition("disk", uid)

  case class LoadAverageTask(uid: String,
                             val threshold: Option[Float] = None)
    extends TaskDefinition("load-average", uid)

}

package object TaskResultDefinition {

  abstract class TaskResult(val `@type`: String, val `@uuid`: String, content: Any)

  case class MemTaskResultContent(val kind: String,
                                  val total: Int,
                                  val used: Int,
                                  val free: Int)

  case class MemTaskResult(`type`: String,
                           uid: String,
                           memContent: MemTaskResultContent)
    extends TaskResult(`type`, uid, memContent)

//  case class DiskTaskResult(`type`: String,
//                            uid: String,
//                            val fileSystem: String,
//                            val total: Int,
//                            val used: Int,
//                            val available: Int,
//                            val percentage: Float) extends TaskResult(`type`, uid)
//
//  case class LoadAverageTaskResult(`type`: String,
//                                   uid: String,
//                                   val fileSystem: String,
//                                   val total: Int,
//                                   val used: Int,
//                                   val available: Int,
//                                   val percentage: Float) extends TaskResult(`type`, uid)

}