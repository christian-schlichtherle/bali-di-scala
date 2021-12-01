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
  crossScalaVersions := Seq("2.12.15", "2.13.7"),
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
  scalacOptions ++= Seq("-deprecation", "-feature"),
  scalaVersion := crossScalaVersions.value.last,
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/christian-schlichtherle/bali-di-scala"),
    connection = "scm:git:git@github.com/christian-schlichtherle/bali-di-scala.git",
  )),
  versionScheme := Some("early-semver"),

  // http://www.scalatest.org/user_guide/using_scalatest_with_sbt
  Test / logBuffered := false,
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
))

ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val root: Project = project
  .in(file("."))
  .aggregate(scala, scalaSample)
  .settings(
    crossScalaVersions := Nil,
    releaseCrossBuild := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges,
    ),
    sonatypeProfileName := "global.namespace",
    name := "Bali DI Root for Scala " + scalaBinaryVersion.value,
    normalizedName := "bali-scala-root",
  )

lazy val scala: Project = project
  .settings(
    libraryDependencies ++= Seq(
      Dependency.ScalaTest % Test,
      "global.namespace.bali" % "bali-annotation" % "0.11.3",
    ) ++ {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
        case _ => Seq.empty
      }
    },
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
    name := "Bali DI Samples for Scala " + scalaBinaryVersion.value,
    normalizedName := "bali-scala-sample",
  )
