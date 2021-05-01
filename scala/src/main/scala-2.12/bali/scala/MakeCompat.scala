package bali.scala

private trait MakeCompat {
  self: Make =>

  import c.universe._

  protected val NamedArg: AssignOrNamedArgExtractor = AssignOrNamedArg

  protected implicit class IteratorOps[A](i: Iterator[A]) {

    def nextOption(): Option[A] = if (i.hasNext) Some(i.next()) else None
  }

  protected implicit class OptionTypeOps(o: Option.type) {

    def when[A](cond: Boolean)(a: => A): Option[A] = if (cond) Some(a) else None
  }
}
