package bali.scala.sample

import bali.{Lookup, Module}

trait Param {

  @Lookup("concat")
  def appendZero(s: String): String

/*
  @Lookup("concat")
  def prependQuestionMark(n: Int): Int
*/
}

@Module
trait ParamModule {

  def concat(s: String = "?", n: Int = 0): String = s + n

  val param: Param
}
