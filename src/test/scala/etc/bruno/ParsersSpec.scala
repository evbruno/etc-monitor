package etc.bruno

import org.scalatest.{FlatSpec, Matchers}

class ParsersSpec extends FlatSpec with Matchers {

  "Load Average" should "extract values" in {

    val subj = new LoadAverageCmd {}
    val r = subj.parse("top - 15:30:10 up 3 days,  1:35,  1 user,  load average: 1.80, 1.47, 1.43\n")
    r.isSuccess shouldBe true

    r.get._1 shouldBe (1.80f +- .1f)
    r.get._2 shouldBe (1.47f +- .1f)
    r.get._3 shouldBe (1.43f +- .1f)

  }

  "Load Average" should "fail to extract values" in {

    val subj = new LoadAverageCmd {}
    val r = subj.parse("top - 15:30:10 up 3 days,  1:35,  1 user")

    r.isFailure shouldBe true

    val error = r.failed.get
    error shouldBe an[NumberFormatException]
  }

  "Memory" should "extract values" in {

    val subj = new MemoryCmd {}
    val r = subj.parse("Mem:          15804       13942         219         889        1643         585")
    r.isSuccess shouldBe true

    r.get._1 shouldBe 15804
    r.get._2 shouldBe 13942
    r.get._3 shouldBe 219
  }


  "Memory" should "fail to extract values" in {

    val subj = new LoadAverageCmd {}
    val r = subj.parse("              total        used        free      shared  buff/cache   available")

    r.isFailure shouldBe true

    val error = r.failed.get
    error shouldBe an[NumberFormatException]

  }

  "Disk" should "extract values" in {

    val subj = new DiskCmd {}
    val r = subj.parse("/dev/sda3       212G   21G  181G  11% /etc/hosts")
    r.isSuccess shouldBe true

    r.get._1 shouldBe "/dev/sda3"
    r.get._2 shouldBe "212G"
    r.get._3 shouldBe "21G"
    r.get._4 shouldBe "181G"
    r.get._5 shouldBe 11
  }


}
