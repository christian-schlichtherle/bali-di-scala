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

trait Greeting {

  def message(entity: String): String
}

private trait RealGreeting extends Greeting {

  def formatter: Formatter

  final def message(entity: String): String = formatter.format(entity)
}

trait Formatter {

  def format(args: Any*): String
}

private trait RealFormatter extends Formatter {

  def theFormat: String

  final def format(args: Any*): String = theFormat.format(args: _*)
}

trait GreetingModule {

  import GreetingModule._

  final lazy val greeting: Greeting = make[RealGreeting]

  final lazy val formatter: Formatter = make[RealFormatter]
}

object GreetingModule {

  val theFormat = "Hello %s!"
}
