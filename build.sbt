ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val alai    = "com.alai"
lazy val scala3Version = "3.8.2"

///////////////////////////////////////////////////////////////////////////////////////////////////////////
// Common - contains domain model
///////////////////////////////////////////////////////////////////////////////////////////////////////////

lazy val core = (crossProject(JSPlatform, JVMPlatform) in file("common"))
  .settings(
    name         := "common",
    scalaVersion := scala3Version,
    organization := alai 
  )
  .jvmSettings(
    // add here if necessary
  )
  .jsSettings(
    // Add JS-specific settings here
  )
  
///////////////////////////////////////////////////////////////////////////////////////////////////////////
// Frontend
///////////////////////////////////////////////////////////////////////////////////////////////////////////

lazy val tyrianVersion = "0.6.1"
lazy val fs2DomVersion = "0.1.0"
lazy val circeVersion  = "0.14.0"

lazy val app = (project in file("app"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name         := "app",
    scalaVersion := scala3Version,
    organization := alai,
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "tyrian-io"     % tyrianVersion,
      "com.armanbilge"  %%% "fs2-dom"       % fs2DomVersion,
      "io.circe"        %%% "circe-core"    % circeVersion,
      "io.circe"        %%% "circe-parser"  % circeVersion,
      "io.circe"        %%% "circe-generic" % circeVersion
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    semanticdbEnabled := true,
    autoAPIMappings   := true
  )
  .dependsOn(core.js)

val Http4sVersion = "0.23.30"
val CirceVersion = "0.14.14"
val MunitVersion = "1.1.1"
val JansiVersion = "1.8"
val LogbackVersion = "1.5.18"
val MunitCatsEffectVersion = "2.1.0"

lazy val server = (project in file("server"))
  .settings(
    name         := "server",
    scalaVersion := scala3Version,
    organization := alai,
    libraryDependencies ++= Seq(
      "org.http4s"           %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"           %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"           %% "http4s-circe"        % Http4sVersion,
      "org.http4s"           %% "http4s-dsl"          % Http4sVersion,
      "org.scalameta"        %% "munit"               % MunitVersion           % Test,
      "org.typelevel"        %% "munit-cats-effect"   % MunitCatsEffectVersion % Test,
      "org.fusesource.jansi" %  "jansi"               % JansiVersion           % Runtime,
      "ch.qos.logback"       %  "logback-classic"     % LogbackVersion         % Runtime,
    ),
    Compile / mainClass := Some("com.alai.muhil.Application")
  )
  .dependsOn(core.jvm)
