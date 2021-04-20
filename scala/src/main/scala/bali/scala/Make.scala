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
      val methodType = methodSymbol.infoIn(targetType)
      lazy val methodOwner = methodSymbol.owner

      val lookupAnnotation = methodSymbol.annotation(LookupAnnotationName)
      val lookupName = lookupAnnotation
        .flatMap(_.get("field", "method", "value").map(TermName.apply))
        .getOrElse(methodName)

      val returnType = methodType.finalResultType
      lazy val tparamsDecl = methodType.typeParams.map(internal.typeDef)
      lazy val paramssDecl = methodType.paramLists.map(_.map(internal.valDef))
      lazy val paramss = methodType.paramLists.map(_.map(_.name))

      def rhs(ref: Tree) = {
        paramss match {
          case List(List()) if methodSymbol.isJava => q"$ref"
          case _ => q"$ref(...$paramss)"
        }
      }

      lazy val lookupRhs = rhs(q"$lookupName")

      def freshName = c.freshName(methodName)

      def implement(rhs: Tree) = {
        if (methodSymbol.isStable) {
          q"override lazy val $methodName: $returnType = $rhs"
        } else {
          q"override def $methodName[..$tparamsDecl](...$paramssDecl): $returnType = $rhs"
        }
      }

      def makeDependency = implement(q"_root_.bali.scala.make[$returnType]")

      def methodSignature = {
        val inherited = if (targetTypeSymbol != methodOwner) s" inherited from $methodOwner" else ""
        s"$methodSymbol with type $methodType$inherited in $targetTypeSymbol"
      }

      def abortWrongType(rhs: Tree) = {
        abort(s"${rhs.symbol} with type ${rhs.tpe} in ${rhs.symbol.owner} is not applicable to implement $methodSignature.")
      }

      def abortNotFound = {
        abort(s"No dependency found to implement $methodSignature.")
      }

      Option
        .when(isModule && lookupAnnotation.isEmpty)(makeDependency)
        .orElse(typecheck(q"def $freshName[..$tparamsDecl](...$paramssDecl): $returnType = $lookupRhs").map { case q"def $_[..$_](...$_): $_ = $ref[..$_](...$_)" => implement(rhs(ref)) })
        .orElse(typecheck(q"$lookupName").map(abortWrongType))
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

  private def typecheck(tree: Tree, mode: c.TypecheckMode = c.TYPEmode, pt: Type = WildcardType) = {
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
