[![Release Notes](https://img.shields.io/github/release/christian-schlichtherle/bali-di-scala.svg)](https://github.com/christian-schlichtherle/bali-di-scala/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/global.namespace.bali/bali-scala_2.13)](https://search.maven.org/artifact/global.namespace.bali/bali-scala_2.13)
[![Apache License 2.0](https://img.shields.io/github/license/christian-schlichtherle/bali-di-scala.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Test Workflow](https://github.com/christian-schlichtherle/bali-di-scala/workflows/test/badge.svg)](https://github.com/christian-schlichtherle/bali-di-scala/actions?query=workflow%3Atest)

# Bali DI for Scala

Bali DI for Scala is a pure def macro which transforms the abstract syntax tree to automate dependency injection. It
currently supports Scala 2.13, with Scala 3 on the roadmap.

This project is a reimplementation of [Bali DI for Java](https://github.com/christian-schlichtherle/bali-di) in Scala.
As a reimplementation, it is based on the exact same concepts and aims for eventual feature parity.

> Bali is also an [island](https://en.wikipedia.org/wiki/Bali) between Java and Lombok in Indonesia.
> For disambiguation, the name of this project is "Bali DI for Scala", not just "Bali", where DI is an acronym for
> [_dependency injection_](https://en.wikipedia.org/wiki/Dependency_injection).
> In code however, the term "DI" is dropped because there is no ambiguity in this context.

# Getting Started

If you use SBT, you need to add the following dependency to your project:

```sbt
libraryDependencies += "global.namespace.bali" %% "bali-scala" % "0.1.0" % Provided
```

Note that this is a compile-time-only dependency - there is no runtime dependency of your code on Bali DI for Scala!
