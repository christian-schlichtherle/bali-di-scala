package bali.scala.sample

import bali.{Lookup, Module}

trait Provider[A] {

  @Lookup(field = "field", method = "method", param = "param", value = "value")
  def get: A
}

@Module
trait ProviderModule {

  private[sample] var field: Null = null

  private[sample] def method: Nothing = ???

  def provider[A >: Null](param: A): Provider[A]
}
