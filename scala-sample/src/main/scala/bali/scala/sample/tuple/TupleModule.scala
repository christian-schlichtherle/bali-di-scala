package bali.scala.sample.tuple

import bali.scala.make
import bali.{Lookup, Module}

trait Tuple2[
  @specialized(Int, Long, Double, Char, Boolean/*, AnyRef*/) +T1,
  @specialized(Int, Long, Double, Char, Boolean/*, AnyRef*/) +T2
] {

  val _1: T1

  val _2: T2

  @Lookup("apply")
  def copy[U1 >: T1, U2 >: T2](_1: U1 = _1, _2: U2 = _2): Tuple2[U1, U2]
}

object Tuple2 {

  def apply[T1, T2](_1: T1, _2: T2): Tuple2[T1, T2] = make[Tuple2[T1, T2]]
}

@Module
trait TupleModule {

  def apply[T1, T2](_1: T1, _2: T2): Tuple2[T1, T2]
}
