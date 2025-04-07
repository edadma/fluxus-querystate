ThisBuild / licenses += "ISC"      -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme          := Some("semver-spec")
ThisBuild / evictionErrorLevel     := Level.Warn
ThisBuild / scalaVersion           := "3.6.4"
ThisBuild / organization           := "io.github.edadma"
ThisBuild / organizationName       := "edadma"
ThisBuild / organizationHomepage   := Some(url("https://github.com/edadma"))
ThisBuild / version                := "0.0.1"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"

ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true).withChecksums(Vector.empty)
ThisBuild / resolvers ++= Seq(Resolver.mavenLocal)
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots") ++ Resolver.sonatypeOssRepos("releases")

ThisBuild / sonatypeProfileName := "io.github.edadma"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/edadma/fluxus-querystate"),
    "scm:git@github.com:edadma/fluxus-querystate.git",
  ),
)
ThisBuild / developers := List(
  Developer(
    id = "edadma",
    name = "Edward A. Maxedon, Sr.",
    email = "edadma@gmail.com",
    url = url("https://github.com/edadma"),
  ),
)

ThisBuild / homepage := Some(url("https://github.com/edadma/fluxus-querystate"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
  ),
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
//  scalaJSLinkerConfig ~= { _.withModuleSplitStyle(ModuleSplitStyle.SmallestModules) },
  scalaJSLinkerConfig ~= { _.withSourceMap(false) },
)

lazy val library = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    name := "fluxus-querystate",
    description := "A lightweight library for synchronizing Fluxus application state with URL query parameters, enabling shareable URLs and browser history integration",
    libraryDependencies ++= Seq(
      "org.scalatest"    %%% "scalatest" % "3.2.19" % "test",
      "com.lihaoyi"      %%% "pprint"    % "0.9.0"  % "test",
      "io.github.edadma" %%% "fluxus"    % "0.0.10",
    ),
    jsEnv                                  := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    scalaJSUseMainModuleInitializer        := true,
    Test / scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseTestModuleInitializer := true,
    Test / parallelExecution               := false,
    publishMavenStyle                      := true,
    Test / publishArtifact                 := false,
  )

lazy val examples = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(library % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(
    name := "examples",
    libraryDependencies ++= Seq(
      "io.github.edadma" %%% "fluxus" % "0.0.10",
    ),
    scalaJSUseMainModuleInitializer := true,
    publish / skip                  := true,
    publishLocal / skip             := true,
  )

lazy val fluxus_querystate = project
  .in(file("."))
  .aggregate(library, examples)
  .settings(
    publish / skip := true,
  )
