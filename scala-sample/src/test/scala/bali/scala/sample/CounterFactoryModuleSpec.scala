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
package bali.scala.sample

import bali.scala.make
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class CounterFactoryModuleSpec extends AnyWordSpec {

  "A CounterFactoryModule" should {
    val module = make[CounterFactoryModule]
    import module._

    "work" in {
      counterFactory should be theSameInstanceAs counterFactory
      counterFactory.counter should not be theSameInstanceAs(counterFactory.counter)
    }
  }
}
