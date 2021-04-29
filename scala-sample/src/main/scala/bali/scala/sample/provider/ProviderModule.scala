package bali.scala.sample.provider

import bali.{Lookup, Module}

trait Provider[A] {

  @Lookup(field = "field", method = "method", param = "param")
  def get: A
}

trait StableProvider[A] extends Provider[A] {

  @Lookup(field = "field", method = "method", param = "param")
  val get: A
}

@Module
trait ProviderModule {

  protected var field: Null = null

  protected def method: Nothing = ???

  def provider[A](param: => A): Provider[A]

  def stableProvider[A](param: => A): StableProvider[A]
}
