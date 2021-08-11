package bali.scala.sample.param

import bali.{Lookup, Module}

final case class Extension(name: String)

trait Param {

  @Lookup("concat")
  def appendZero(s: String): String

  def pathName(dirName: String)(baseName: String)(implicit extension: Extension): String
}

@Module
trait ParamModule {

  def concat(s: String = "?", n: Int = 0): String = s + n

  def pathName(dirName: String)(baseName: String)(implicit extension: Extension): String = {
    dirName + '/' + baseName + '.' + extension.name
  }

  val param: Param
}
