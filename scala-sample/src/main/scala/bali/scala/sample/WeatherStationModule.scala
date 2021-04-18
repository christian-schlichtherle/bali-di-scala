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

import bali.scala.sample.Temperature.Celsius
import bali.{Lookup, Module}

import java.util.Date
import java.util.concurrent.ThreadLocalRandom

trait Temperature[U <: Temperature.Unit] {

  @Lookup("tempValue")
  val value: Double

  @Lookup("tempUnit")
  val unit: U

  final override def toString: String = s"$value˚ $unit"
}

object Temperature {

  sealed trait Unit

  type Celsius = Celsius.type

  object Celsius extends Unit {

    override def toString: String = "Celsius"
  }

  type Fahrenheit = Fahrenheit.type

  object Fahrenheit extends Unit {

    override def toString: String = "Fahrenheit"
  }
}

trait WeatherStation[U <: Temperature.Unit] extends Clock {

  def temp: Temperature[U]
}

@Module
trait WeatherStationModule {

  val april: WeatherStation[Celsius]

  protected def now: Date

  protected def temp: Temperature[Celsius]

  protected final def tempValue: Double = ThreadLocalRandom.current.nextDouble(5D, 25D)

  protected final val tempUnit = Temperature.Celsius
}
