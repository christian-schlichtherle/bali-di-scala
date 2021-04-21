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

private final class Make(val c: blackbox.Context) {

  import c.universe._

  private def abort(msg: String) = c.abort(c.enclosingPosition, msg)

  def apply[A >: Null <: AnyRef : c.WeakTypeTag]: Tree = {

    lazy val body = {
      targetType.members.collect { case member: MethodSymbol => member }.filter(_.isAbstract).map(implement)
    }

    def implement(methodSymbol: MethodSymbol) = {

      def abortNotFound = abort(s"No dependency found to implement $methodSignature.")

      def abortWrongType(ref: Tree) = {
        abort(s"${ref.symbol} with type ${ref.tpe} in ${ref.symbol.owner} is not applicable to implement $methodSignature.")
      }

      def callDependency: PartialFunction[Tree, Tree] = {
        case q"def $_[..$_](...$_): $_ = ${ref: Tree}[..$_](...$_)" => implementAs(rhs(ref))
      }

      def freshName = c.freshName(methodName)

      def implementAs(rhs: Tree) = {
        if (methodSymbol.isStable) {
          q"override lazy val $methodName: $returnType = $rhs"
        } else {
          q"override def $methodName[..$typeParamsDecl](...$paramDeclLists): $returnType = $rhs"
        }
      }

      lazy val lookupAnnotation = methodSymbol.annotation(LookupAnnotationName)

      def lookupDefTree = q"def $freshName[..$typeParamsDecl](...$paramDeclLists): $returnType = ${rhs(lookupNameTree)}"

      def lookupName = lookupAnnotation
        .flatMap(_.get("field", "method", "value").map(TermName.apply))
        .getOrElse(methodName)

      def lookupNameTree = q"$lookupName"

      def makeDependency = implementAs(q"_root_.bali.scala.make[$returnType]")

      lazy val methodName = methodSymbol.name

      lazy val methodOwner = methodSymbol.owner

      lazy val methodSignature = {
        val inherited = if (targetTypeSymbol != methodOwner) s" inherited from $methodOwner" else ""
        s"$methodSymbol with type $methodType$inherited in $targetTypeSymbol"
      }

      lazy val methodType = methodSymbol.infoIn(targetType)

      lazy val paramNameLists = methodType.paramLists.map(_.map(_.name))

      lazy val paramDeclLists = methodType.paramLists.map(_.map(internal.valDef))

      lazy val returnType = methodType.finalResultType

      def rhs(ref: Tree) = {
        paramNameLists match {
          case List(List()) if methodSymbol.isJava => q"$ref"
          case _ => q"$ref(...$paramNameLists)"
        }
      }

      lazy val typeParamsDecl = methodType.typeParams.map(internal.typeDef)

      Option
        .when(isModule && lookupAnnotation.isEmpty)(makeDependency)
        .orElse(typecheck(lookupDefTree).map(callDependency))
        .orElse(typecheck(lookupNameTree).map(abortWrongType))
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
    c.typecheck(tree, silent = true) match {
      case EmptyTree => None
      case typecheckedTree => Some(typecheckedTree)
    }
  }

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
