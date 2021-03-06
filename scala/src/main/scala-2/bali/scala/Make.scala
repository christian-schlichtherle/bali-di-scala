/*
 * Copyright © 2021 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bali.scala

import scala.reflect.macros.blackbox

private final class Make(val c: blackbox.Context) extends MakeCompat {

  import c.universe._

  type SymbolTest = Symbol => Boolean

  private def abort(msg: String) = c.abort(c.enclosingPosition, msg)

  def apply[A >: Null <: AnyRef : c.WeakTypeTag]: Tree = {

    lazy val body = targetType.members.collect { case m: TermSymbol if m.isAbstract =>

      lazy val abortNotFound = abort(s"No dependency found to bind $memberSignature.")

      def abortWrongType(ref: Tree) = {
        val withType = if (ref.tpe.paramLists.flatten.isEmpty) ": " else ""
        abort(s"${ref.symbol}$withType${ref.tpe} in ${ref.symbol.owner} is not applicable to bind $memberSignature.")
      }

      def bindAs(rhs: Tree) = {
        if (member.isStable) {
          q"final override lazy val $memberName: $returnType = $rhs"
        } else {
          q"final override def $memberName[..$typeParams4Lhs](...$explParamLists4Lhs)(implicit ..$implParams4Lhs): $returnType = $rhs"
        }
      }

      def bindDependency(ref: Tree) = bindAs(rightHandSide(ref))

      lazy val (explParamLists4Lhs, implParams4Lhs) = {
        val (i, e) = paramLists.partition(_.exists(_.isImplicit))
        paramLists4Lhs(e) -> paramLists4Lhs(i).flatten
      }

      lazy val fieldAlias = lookupAnnotationMap.get("field")

      lazy val fieldAliasAndConstraint = fieldAlias.map(_ -> fieldConstraint)

      lazy val fieldConstraint: SymbolTest = {
        case s: MethodSymbol => s.isVal || s.isVar
        case _ => false
      }

      lazy val lookupAnnotation = member.annotation(LookupAnnotationName)

      lazy val lookupAnnotationMap = {
        lookupAnnotation.map(_.toMap).getOrElse(Map.empty).collect {
          case (key, value: String) if value.nonEmpty => key -> TermName(value)
        }
      }

      lazy val makeDependency = bindAs(q"_root_.bali.scala.make[$returnType]")

      // If skipped, member annotations may be missing, e.g. @Lookup, resulting in code generation errors:
      lazy val member = internal.initialize(m)

      lazy val memberName: TermName = member.name

      lazy val memberOwner = member.owner

      lazy val memberSignature = {
        val withType = if (paramLists.flatten.isEmpty) ": " else ""
        val inherited = if (targetTypeSymbol != memberOwner) s" in $memberOwner as seen by " else " in "
        member.toString + withType + memberType + inherited + targetTypeSymbol
      }

      lazy val memberType = member.infoIn(targetType)

      lazy val methodAlias = lookupAnnotationMap.get("method")

      lazy val methodAliasAndConstraint = methodAlias.map(_ -> methodConstraint)

      lazy val methodConstraint: SymbolTest = _.isMethod

      lazy val paramAlias = lookupAnnotationMap.get("param")

      lazy val paramAliasAndConstraint = paramAlias.map(_ -> paramConstraint)

      lazy val paramConstraint: SymbolTest = _.isParameter

      lazy val paramLists = memberType.paramLists

      def paramLists4Lhs(l: List[List[Symbol]]) = l.map(_.map(s => q"${s.name.toTermName}: ${s.info}"))

      lazy val paramLists4Rhs = paramLists.map(_.map(_.name.toTermName))

      lazy val returnType = memberType.finalResultType

      def rightHandSide(ref: Tree) = {
        paramLists4Rhs match {
          case List(List()) if member.isJava => q"$ref"
          case _ => q"$ref(...$paramLists4Rhs)"
        }
      }

      def typecheckAndExtract(ref: TermName) = {
        val freshName = c.freshName(memberName)
        val rhs = rightHandSide(q"$ref")
        typecheck(q"def $freshName[..$typeParams4Lhs](...$explParamLists4Lhs)(implicit ..$implParams4Lhs): $returnType = $rhs")
          .flatMap {
            case q"def $_[..$_](...$_): $_ = ${ref: Tree}[..$_](...$_)" => Some(ref)
            case _ => None
          }
      }

      lazy val matchDependency = {
        matchPlan.iterator.flatMap {
          case (alias, constraint) => typecheckAndExtract(alias).filter(ref => constraint(ref.symbol))
        }.nextOption()
      }

      lazy val matchPlan = {
        Seq(
          paramAliasAndConstraint,
          methodAliasAndConstraint,
          fieldAliasAndConstraint,
          Some(
            valueAliasOrMethodName ->
              Seq(paramAliasAndConstraint, methodAliasAndConstraint, fieldAliasAndConstraint)
                .flatten
                .map(_._2)
                .reduceOption((t1, t2) => (s: Symbol) => t1(s) || t2(s))
                .map(t => (s: Symbol) => !t(s))
                .getOrElse((_: Symbol) => true)
          )
        ).flatten
      }

      def reportWrongType = {
        matchPlan.flatMap {
          case (alias, constraint) => typecheck(q"$alias").filter(ref => constraint(ref.symbol))
        }.map(abortWrongType).lastOption
      }

      lazy val typeParams4Lhs = memberType.typeParams.map(internal.typeDef)

      lazy val valueAlias = lookupAnnotationMap.get("value")

      lazy val valueAliasOrMethodName = valueAlias.getOrElse(memberName)

      Option
        .when(isModule && lookupAnnotation.isEmpty)(makeDependency)
        .orElse(matchDependency.map(bindDependency))
        .orElse(reportWrongType)
        .getOrElse(abortNotFound)
    }

    lazy val isModule = targetTypeSymbol.annotation(ModuleAnnotationName).isDefined

    lazy val targetType = weakTypeOf[A]

    lazy val targetTypeSymbol = targetType.typeSymbol

    if (body.nonEmpty) {
      q"new $targetType { ..$body }"
    } else if (targetTypeSymbol.isAbstract) {
      q"new $targetType {}"
    } else {
      q"new $targetType"
    }
  }

  private def typecheck(tree: Tree) = {
    c.typecheck(tree, silent = true, withImplicitViewsDisabled = true) match {
      case EmptyTree => None
      case typecheckedTree => Some(typecheckedTree)
    }
  }

  private implicit class AnnotationOps(a: Annotation) {

    def toMap: Map[String, Any] = {
      a.tree.children.tail.collect {
        case NamedArg(Ident(TermName(key)), Literal(Constant(value))) => key -> value
      }.toMap
    }
  }

  private implicit class SymbolOps(s: Symbol) {

    def annotation(name: String): Option[Annotation] = s.annotations.find(_.tree.tpe.typeSymbol.fullName == name)
  }
}
