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

import ReleaseTransformations._

inThisBuild(Seq(
  homepage := Some(url("https://github.com/christian-schlichtherle/bali-di-scala")),
  licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  organization := "global.namespace.bali",
  organizationHomepage := Some(new URL("http://schlichtherle.de")),
  organizationName := "Schlichtherle IT Services",
  pomExtra := {
    <developers>
      <developer>
        <name>Christian Schlichtherle</name>
        <email>christian AT schlichtherle DOT de</email>
        <organization>Schlichtherle IT Services</organization>
        <timezone>2</timezone>
        <roles>
          <role>owner</role>
        </roles>
        <properties>
          <picUrl>http://www.gravatar.com/avatar/e2f69ddc944f8891566fc4b18518e4e6.png</picUrl>
        </properties>
      </developer>
    </developers>
      <issueManagement>
        <system>Github</system>
        <url>https://github.com/christian-schlichtherle/bali-di-scala/issues</url>
      </issueManagement>
  },
  publishArtifact := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    Some(
      if (version(_ endsWith "-SNAPSHOT").value) {
        "snapshots" at nexus + "content/repositories/snapshots"
      } else {
        "releases" at nexus + "service/local/staging/deploy/maven2"
      }
    )
  },
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommandAndRemaining("+test"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    setNextVersion,
    commitNextVersion,
    pushChanges,
  ),
  scalacOptions ++= Seq("-deprecation", "-feature", "-Ymacro-annotations"),
  scalaVersion := "2.13.5",
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/christian-schlichtherle/bali-di-scala"),
    connection = "scm:git:git@github.com/christian-schlichtherle/bali-di-scala.git",
    devConnection = Some("scm:git:git@github.com/christian-schlichtherle/bali-di-scala.git")
  )),

  // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
  Test / logBuffered := false,
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
))

lazy val root: Project = project
  .in(file("."))
  .aggregate(scala, scalaSample)
  .settings(
    name := "Bali DI Root",
    normalizedName := "bali-root",
  )

lazy val scala: Project = project
  .settings(
    libraryDependencies ++= Seq(
      Dependency.ScalaTest % Test,
      "global.namespace.bali" % "bali-annotation" % "0.11.2",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    ),
    name := "Bali DI for Scala " + scalaBinaryVersion.value,
    normalizedName := "bali-scala",
    publishArtifact := true,
  )

lazy val scalaSample: Project = project
  .in(file("scala-sample"))
  .dependsOn(scala % Provided)
  .settings(
    libraryDependencies ++= Seq(
      Dependency.ScalaTest % Test,
    ),
    name := s"Bali DI for Scala ${scalaBinaryVersion.value} Samples",
    normalizedName := "bali-scala-sample",
  )
