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

//libraryDependencies += "com.decodified" %% "scala-ssh" % "0.9.0"
//
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

//libraryDependencies += "com.github.mideo" % "sssh_2.12" % "0.0.1"

test in assembly := {}

mainClass in assembly := Some("etc.bruno.SSHActorApp")

assemblyJarName in assembly := "etc-monitor.jar"

