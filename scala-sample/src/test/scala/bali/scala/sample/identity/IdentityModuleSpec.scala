package bali.scala.sample.identity

import bali.scala.make
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

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
