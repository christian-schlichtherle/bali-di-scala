package bali.scala.sample

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import bali.scala.make

class IdentityModuleSpec extends AnyWordSpec {

  "An IdentityModule" should {
    val module = make[IdentityModule]
    import module._

    "work" in {
      identity(1) shouldBe 1
      identity("Hello world!") shouldBe "Hello world!"
    }
  }
}
