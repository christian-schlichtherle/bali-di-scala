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
package bali.scala.sample.greeting

import bali.{Lookup, Module}

trait Formatter {

  def format(args: Any*): String
}

trait Greeting {

  def message(entity: String): String
}

trait GreetingModule {

  val greeting: Greeting
}

private trait RealFormatter extends Formatter {

  @Lookup("Format")
  protected def format: String

  final def format(args: Any*): String = format.format(args: _*)
}

private trait RealGreeting extends Greeting {

  protected def formatter: Formatter

  final def message(entity: String): String = formatter.format(entity)
}

@Module
private trait RealGreetingModule extends GreetingModule {

  // TODO: If this method is abstract, then the make[...] call would be automatically added but it wouldn't have access to private fields or imported values anymore?!
  final lazy val formatter: RealFormatter = bali.scala.make[RealFormatter]

  override val greeting: RealGreeting

  private val Format = "Hello %s!"
}
