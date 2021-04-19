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
import scala.util.chaining._

private final class Make(val c: blackbox.Context) {

  import c.universe._

  def apply[A <: AnyRef : c.WeakTypeTag]: Tree = {
    val targetType = weakTypeOf[A]
    lazy val targetTypeSymbol = targetType.typeSymbol
    lazy val isModule = targetTypeSymbol.annotation(ModuleAnnotationName).isDefined

    def bind(methodSymbol: MethodSymbol) = {
      val methodName = methodSymbol.name
      val methodSignature = methodSymbol.infoIn(targetType)
      val lookupAnnotation = methodSymbol.annotation(LookupAnnotationName)
      val lookupName = lookupAnnotation
        .flatMap(_.get("field", "method", "value").map(TermName.apply))
        .getOrElse(methodName)
      val returnType = methodSignature.finalResultType
      lazy val tparamsDecl = methodSignature.typeParams.map(internal.typeDef)
      lazy val tparams = methodSignature.typeParams.map(_.name)
      lazy val paramssDecl = methodSignature.paramLists.map(_.map(internal.valDef))
      lazy val paramss = methodSignature.paramLists.map(_.map(_.name))

      def rhs(ref: Tree) = {
        paramss match {
          case List(List()) if methodSymbol.isJava => q"$ref"
          case _ => q"$ref(...$paramss)"
        }
      }

      lazy val lookupRhs = rhs(q"$lookupName")

      lazy val functionType = c.typecheck(tq"$targetType => $returnType", mode = c.TYPEmode).tpe

      def freshName = c.freshName(methodName)

      def bind(rhs: Tree) = {
        if (methodSymbol.isStable) {
          q"override lazy val $methodName: $returnType = $rhs"
        } else {
          q"override def $methodName[..$tparamsDecl](...$paramssDecl): $returnType = $rhs"
        }
      }

      def makeDependency = bind(q"_root_.bali.scala.make[$returnType]")

      def bindFunction(rhs: Tree) = bind(q"${c.untypecheck(rhs)}(this)")

      def abortWrongType(rhs: Tree) = {
        abort(s"Expression ($rhs) must be assignable to type $returnType or ($functionType), but has type ${rhs.tpe}:")
      }

      def abortNotFound = {
        val inherited = if (targetTypeSymbol != methodSymbol.owner) s" (inherited from ${methodSymbol.owner})" else ""
        abort(s"No dependency found of type $returnType or ($functionType) to bind $methodSymbol$inherited.")
      }

      Option
        .when(isModule && lookupAnnotation.isEmpty)(makeDependency)
//        .orElse(typecheck(lookupRhs, pt = returnType).map(bind))
        .orElse(typecheck(lookupRhs, pt = functionType).map(bindFunction))
        .orElse(typecheck(q"def $freshName[..$tparamsDecl](...$paramssDecl): $returnType = $lookupRhs", mode = c.TYPEmode)/*.tap(debug)*/.map { case q"def $_[..$_](...$_): $_ = $ref[..$_](...$_)" => ref }.map(ref => bind(rhs(ref)/*.tap(debug)*/)))
        .orElse(typecheck(lookupRhs).map(abortWrongType))
        .getOrElse(abortNotFound)
    }

    val body = targetType
      .members
      .collect { case member: MethodSymbol => member }
      .filter(_.isAbstract)
      .map(bind)
    if (body.nonEmpty) {
      q"new $targetType { ..$body }"
    } else if (targetTypeSymbol.isAbstract) {
      q"new $targetType {}"
    } else {
      q"new $targetType"
    }
  }

  private def debug(any: Any): Unit = c.echo(c.enclosingPosition, show(any))

  private def typecheck(tree: Tree, mode: c.TypecheckMode = c.TERMmode, pt: Type = WildcardType) = {
    c.typecheck(tree, mode = mode, pt = pt, silent = true) match {
      case EmptyTree => None
      case typecheckedTree => Some(typecheckedTree)
    }
  }

  private def abort(msg: String) = c.abort(c.enclosingPosition, msg)

  private implicit class AnnotationOps(a: Annotation) {

    def get(keys: String*): Option[String] = {
      a.tree.children.tail.collectFirst {
        case NamedArg(Ident(TermName(key)), Literal(Constant(value))) if keys.contains(key) => value.toString
      }
    }
  }

  private implicit class SymbolOps(s: Symbol) {

    def annotation(name: String): Option[Annotation] = s.annotations.find(_.tree.tpe.typeSymbol.fullName == name)
  }
}
