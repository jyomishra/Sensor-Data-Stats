import Dependencies._

ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.somecomp"
ThisBuild / organizationName := "techtest"

lazy val root = (project in file("."))
  .settings(
    name := "Sensor-Data-Stats",
    libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "3.0.3",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.17" ,
    libraryDependencies +=  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.17",
    libraryDependencies +=  "com.typesafe.akka" %% "akka-testkit" % "2.6.17",
    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.7.8",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += configDep % Compile,
    libraryDependencies += scalaLogging % Compile,
    libraryDependencies += logback % Compile,
)
exportJars := true
mainClass := Some("com.tech.test.Main")

