package bali.scala.sample

import bali.Module

@Module
trait SimpleGreetingModule {

  private val Format = "Hello %s!"

  protected final def message(entity: String): String = Format.format(entity)

  val greeting: Greeting
}
