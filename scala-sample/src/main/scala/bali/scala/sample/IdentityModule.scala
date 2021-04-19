package bali.scala.sample

import bali.Module

trait Apply {

  def apply[A](a: A): A
}

@Module
trait IdentityModule {

  protected def apply[A](a: A): A = a

  def identity: Apply
}
