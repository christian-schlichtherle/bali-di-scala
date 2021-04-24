package bali.scala.sample

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import bali.scala.make

class ParamModuleSpec extends AnyWordSpec {

  "A ParamModule" should {
    val param = make[ParamModule].param
    import param._

    "work" in {
      appendZero("A") shouldBe "A0"
//      prependQuestionMark(1) shouldBe "?1"
    }
  }
}
