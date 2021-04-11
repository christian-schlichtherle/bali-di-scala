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

import java.util.concurrent.ThreadLocalRandom

trait WeatherStation extends Clock {

  def temperature: Temperature

  trait Temperature {

    val value: Double

    val unit: String
  }
}

private trait AprilWeatherStation extends WeatherStation {

  import AprilWeatherStation._

  def temperature: Temperature = make[Temperature]
}

private object AprilWeatherStation {

  def value: Double = ThreadLocalRandom.current.nextDouble(5D, 25D)

  val unit: String = "˚ Celsius"
}

trait WeatherStationModule {

  lazy val station: WeatherStation = make[AprilWeatherStation]
}
