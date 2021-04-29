package bali.scala.sample.simplegreeting

import bali.Module
import bali.scala.sample.greeting.Greeting

@Module
trait SimpleGreetingModule {

  private val Format = "Hello %s!"

  protected final def message(entity: String): String = Format.format(entity)

  val greeting: Greeting
}
