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

  def apply[A <: AnyRef : c.WeakTypeTag]: Tree = {
    val targetType = weakTypeOf[A]
    lazy val targetTypeSymbol = targetType.typeSymbol
    lazy val isModule = targetTypeSymbol.annotation(ModuleAnnotationName).isDefined

    def bind(symbol: MethodSymbol) = {
      val lookupAnnotation = symbol.annotation(LookupAnnotationName)
      val symbolName = symbol.name
      val lookupName = lookupAnnotation
        .flatMap(_.get("field", "method", "value").map(TermName.apply))
        .getOrElse(symbolName)
      val dependencyTree = q"$lookupName"
      val returnType = symbol.returnType.asSeenFrom(targetType, symbol.owner)
      lazy val functionType = c.typecheck(tq"$targetType => $returnType", mode = c.TYPEmode).tpe
      lazy val isStableSymbol = symbol.isStable

      def typecheckDependency(as: Type) = {
        c.typecheck(dependencyTree, pt = as, silent = true) match {
          case EmptyTree => None
          case typeCheckedDependency => Some(typeCheckedDependency)
        }
      }

      def bindDependency(dependency: Tree) = {
        if (isStableSymbol) {
          q"override lazy val $symbolName: $returnType = $dependency"
        } else {
          q"override def $symbolName: $returnType = $dependency"
        }
      }

      def bindDependencyFunction(dependency: Tree) = {
        val fun = c.untypecheck(dependency)
        bindDependency(q"$fun(this)")
      }

      def makeDependency = bindDependency(q"_root_.bali.scala.make[$returnType]")

      def abortWrongType(dependency: Tree) = {
        val dependencySymbol = dependency.symbol
        abort(s"Dependency $dependencySymbol in ${dependencySymbol.owner} must be assignable to type $returnType or ($functionType), but has type ${dependency.tpe}:")
      }

      def abortNotFound = {
        abort(s"No dependency found to bind $symbol in ${symbol.owner} of type $returnType as seen from $targetTypeSymbol:")
      }

      typecheckDependency(returnType).map(bindDependency)
        .orElse(typecheckDependency(functionType).map(bindDependencyFunction))
        .orElse(typecheckDependency(WildcardType).map(abortWrongType))
        .orElse(Some(makeDependency).filter(_ => isModule && lookupAnnotation.isEmpty))
        .getOrElse(abortNotFound)
    }

    val body = targetType
      .members
      .collect { case member: MethodSymbol => member }
      .filter(_.isAbstract)
      .filter(_.paramLists.flatten.isEmpty)
      .map(bind)
    if (body.nonEmpty) {
      q"new $targetType { ..$body }"
    } else if (targetTypeSymbol.isAbstract) {
      q"new $targetType {}"
    } else {
      q"new $targetType"
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
