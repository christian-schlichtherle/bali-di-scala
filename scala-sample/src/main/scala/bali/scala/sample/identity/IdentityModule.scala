package bali.scala.sample.identity

import bali.{Lookup, Module}

trait Fun[A, B] {

  @Lookup("fun")
  def apply(a: A): B
}

@Module
trait IdentityModule {

  protected def fun[A](a: A): A = a

  def identity[A]: Fun[A, A]
}
