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

  def apply[A <: AnyRef : c.WeakTypeTag]: Tree = {
    val targetType = weakTypeOf[A]

    def targetTypeSymbol = targetType.typeSymbol

    def binding(symbol: MethodSymbol) = {
      val name = symbol.name
      val dependency = q"$name"
      val returnType = symbol.returnType.asSeenFrom(targetType, symbol.owner)
      lazy val functionType = c.typecheck(tq"$targetType => $returnType", mode = c.TYPEmode).tpe

      def isStable = symbol.isStable

      def typeCheckDependency(as: Type) = {
        c.typecheck(dependency, pt = as, silent = true) match {
          case EmptyTree => None
          case typeCheckedDependency => Some(typeCheckedDependency)
        }
      }

      def returnValueBinding(dependency: Tree) = {
        if (isStable) {
          q"override lazy val $name: $returnType = $dependency"
        } else {
          q"override def $name: $returnType = $dependency"
        }
      }

      def functionBinding(dependency: Tree) = {
        val fun = c.untypecheck(dependency)
        if (isStable) {
          q"override lazy val $name: $returnType = $fun(this)"
        } else {
          q"override def $name: $returnType = $fun(this)"
        }
      }

      def wrongType(dependency: Tree) = {
        val dependencySymbol = dependency.symbol
        abort(s"Dependency $dependencySymbol in ${dependencySymbol.owner} must be assignable to type $returnType or ($functionType), but has type ${dependency.tpe}:")
      }

      def notFound = {
        abort(s"No dependency found to bind the $symbol in ${symbol.owner} of type $returnType as seen from $targetTypeSymbol:")
      }

      typeCheckDependency(returnType).map(returnValueBinding)
        .orElse(typeCheckDependency(functionType).map(functionBinding))
        .orElse(typeCheckDependency(WildcardType).map(wrongType))
        .getOrElse(notFound)
    }

    val body = targetType
      .members
      .collect { case member: MethodSymbol => member }
      .filter(_.isAbstract)
      .filter(_.paramLists.flatten.isEmpty)
      .map(binding)
    if (body.nonEmpty) {
      q"new $targetType { ..$body }"
    } else if (targetTypeSymbol.isAbstract) {
      q"new $targetType {}"
    } else {
      q"new $targetType"
    }
  }
}
