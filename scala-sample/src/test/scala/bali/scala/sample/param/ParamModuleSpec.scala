package bali.scala.sample.param

import bali.scala.make
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ParamModuleSpec extends AnyWordSpec {

  "A ParamModule" should {
    val param = make[ParamModule].param
    import param._

    "support binding methods with parameters" in {
      appendZero("A") shouldBe "A0"
    }

    "support binding methods with multiple parameter lists, including an implicit parameter list" in {
      implicit val txt: Extension = Extension("TXT")
      pathName(".")("README") shouldBe "./README.TXT"
    }
  }
}
