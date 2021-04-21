package bali.scala.sample

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import bali.scala.make

class ProviderModuleSpec extends AnyWordSpec {

  "A ProviderModule" should {
    val module = make[ProviderModule]
    import module._

    "work" in {
      provider("Hello world!").get shouldBe "Hello world!"
    }
  }
}
