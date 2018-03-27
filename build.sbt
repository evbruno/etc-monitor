name := "etc-monitor"

organization := "br.etc.bruno"

version := "1.0"

scalaVersion := "2.12.2"

lazy val akkaVersion = "2.5.11"
lazy val akkaHttpVersion = "10.1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",

  "com.jcraft" % "jsch" % "0.1.54",

  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)
