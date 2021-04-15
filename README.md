[![Release Notes](https://img.shields.io/github/release/christian-schlichtherle/bali-di-scala.svg)](https://github.com/christian-schlichtherle/bali-di-scala/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/global.namespace.bali/bali-scala)](https://search.maven.org/artifact/global.namespace.bali/bali-scala)
[![Apache License 2.0](https://img.shields.io/github/license/christian-schlichtherle/bali-di-scala.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Test Workflow](https://github.com/christian-schlichtherle/bali-di-scala/workflows/test/badge.svg)](https://github.com/christian-schlichtherle/bali-di-scala/actions?query=workflow%3Atest)

# Bali DI for Scala

This is a reimplementation of [Bali DI](https://github.com/christian-schlichtherle/bali-di) for Scala.

# Getting Started

If you use SBT, you need to add the following dependency to your project:

```sbt
libraryDependencies += "global.namespace.bali" %% "bali-scala" % "0.1.0" % Provided
```

Note that this is a compile-time-only dependency - there is no runtime dependency of your code on Bali DI for Scala!
