package bali.scala.sample.param

import bali.scala.make
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

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
