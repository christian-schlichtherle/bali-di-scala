package bali.scala.sample

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import bali.scala.make

class TupleModuleSpec extends AnyWordSpec {

  "A TupleModule" should {
    val module = make[TupleModule]

    "make a working Tuple2" in {
      test(module("foo", "bar"))
    }
  }

  "The Tuple2 companion object" should {
    "make a working Tuple2" in {
      test(Tuple2("foo", "bar"))
    }
  }

  private def test(t: Tuple2[String, String]) = {
    t._1 shouldBe "foo"
    t._2 shouldBe "bar"

    val u = t.copy(_1 = "one")
    u._1 shouldBe "one"
    u._2 shouldBe "bar"

    val w = u.copy(_2 = "two")
    w._1 shouldBe "one"
    w._2 shouldBe "two"
  }
}
