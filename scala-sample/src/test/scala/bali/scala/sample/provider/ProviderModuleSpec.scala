package bali.scala.sample.provider

import bali.scala.make
import bali.scala.sample.provider.ProviderModuleSpec._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class ProviderModuleSpec extends AnyWordSpec {

  "A ProviderModule" should {
    val module = make[ProviderModule]
    import module._

    "work" in {
      provider("Hello world!").get shouldBe "Hello world!"
    }

    "execute a side effect at most once" in new Fixture { self =>
      import self.{incrementAndGet => method}
//      def method: Int = incrementAndGet
      val counter = make[StableProvider[Int]]
      count shouldBe 0
      counter.get shouldBe 1
      count shouldBe 1
      counter.get shouldBe 1
      count shouldBe 1
    }

    "execute a side effect on each call" in new Fixture {
      val counter = module.provider(incrementAndGet)
      count shouldBe 0
      counter.get shouldBe 1
      count shouldBe 1
      counter.get shouldBe 2
      count shouldBe 2
    }
  }
}

object ProviderModuleSpec {

  private trait Fixture {

    final var count = 0

    final def incrementAndGet: Int = {
      count += 1
      count
    }

    val counter: Provider[Int]
  }
}
