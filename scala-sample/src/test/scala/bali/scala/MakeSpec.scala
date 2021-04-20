/*
 * Copyright © 2021 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bali.scala

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers._

import java.util.function.Supplier

class MakeSpec extends AnyFeatureSpec {

  import MakeSpec._

  Feature("Dependencies can be auto-wired using the `make` macro") {

    Scenario("A[Int]") {
      val foo = 1
      val a = make[A[Int]]
      a.foo shouldBe foo
    }

    Scenario("A[String]") {
      def foo = new String("foo")

      val a = make[A[String]]
      a.foo shouldBe foo
      a.foo shouldNot be theSameInstanceAs a.foo
    }

    Scenario("A1") {
      def foo = new String("foo")

      val a1 = make[A1]
      a1.foo shouldBe foo
      a1.foo should be theSameInstanceAs a1.foo
    }

    Scenario("ABC[Int]") {
      val foo = 1
      val bar = 2

      def baz = 3

      val abc = make[ABC[Int]]
      abc.foo shouldBe foo
      abc.bar shouldBe bar
      abc.baz shouldBe baz
    }

    Scenario("ABC[String]") {
      def foo = new String("foo")

      def bar = new String("bar")

      def baz = new String("baz")

      val abc = make[ABC[String]]
      abc.foo shouldBe foo
      abc.foo shouldNot be theSameInstanceAs abc.foo
      abc.bar shouldBe bar
      abc.bar shouldNot be theSameInstanceAs abc.bar
      abc.baz shouldBe baz
      abc.baz shouldNot be theSameInstanceAs abc.baz
    }

    Scenario("StableABC[String]") {
      def foo = new String("foo")

      def bar = new String("bar")

      def baz = new String("baz")

      val abc = make[StableABC[String]]
      abc.foo shouldBe foo
      abc.foo should be theSameInstanceAs abc.foo
      abc.bar shouldBe bar
      abc.bar should be theSameInstanceAs abc.bar
      abc.baz shouldBe baz
      abc.baz should be theSameInstanceAs abc.baz
    }

    Scenario("Supplier[String]") {
      def get = "Hello world!"

      val supplier = make[Supplier[String]]
      supplier.get shouldBe "Hello world!"
    }
  }
}

private object MakeSpec {

  trait A[T] {

    def foo: T
  }

  trait A1 extends A[String] {

    override val foo: String
  }

  trait B[T] {

    def bar: T
  }

  trait C[T] {

    def baz: T
  }

  trait ABC[T] extends A[T] with B[T] with C[T]

  trait StableABC[T] extends ABC[T] {

    override val foo: T
    override val bar: T
    override val baz: T
  }
}
