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
    val targetTypeSymbol = targetType.typeSymbol

    def binding(symbol: MethodSymbol) = {
      lazy val name: TermName = symbol.name
      lazy val dependency: Tree = q"$name"
      lazy val stable: Boolean = symbol.isStable
      lazy val returnType: Type = symbol.returnType.asSeenFrom(targetType, symbol.owner)
      lazy val functionType: Type = c.typecheck(tq"$targetType => $returnType", mode = c.TYPEmode).tpe

      def typeCheckDependencyAs(dependencyType: Type): Option[Tree] = {
        c.typecheck(dependency, pt = dependencyType, silent = true) match {
          case EmptyTree => None
          case typeCheckedDependency => Some(typeCheckedDependency)
        }
      }

      def returnValueBinding(dependency: Tree): Tree = {
        if (stable) {
          q"lazy val $name: $returnType = $dependency"
        } else {
          q"def $name: $returnType = $dependency"
        }
      }

      def functionBinding(dependency: Tree): Tree = {
        val fun = c.untypecheck(dependency)
        if (stable) {
          q"lazy val $name: $returnType = $fun(this)"
        } else {
          q"def $name: $returnType = $fun(this)"
        }
      }

      {
        typeCheckDependencyAs(returnType).map(returnValueBinding)
      }.orElse {
        typeCheckDependencyAs(functionType).map(functionBinding)
      }.getOrElse {
        typeCheckDependencyAs(WildcardType).map { dependency =>
          abort(s"Dependency `$dependency` must be assignable to type `$returnType` or `$functionType`, but has type `${dependency.tpe}`:")
        }.getOrElse {
          abort(s"No dependency available to bind method `$name: $returnType` as seen from $targetTypeSymbol:")
        }
      }
    }

    val body = targetType
      .members
      .collect { case member: MethodSymbol => member }
      .filter(_.isAbstract)
      .filter(_.paramLists.flatten.isEmpty)
      .map(binding)
    if (body.nonEmpty) {
      q"new $targetType { ..$body }"
    } else /*if (targetTypeSymbol.isAbstract)*/ {
      q"new $targetType {}"
//    } else {
//      q"new $targetType"
    }
  }
}
