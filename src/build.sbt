name := "BitCoins"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.5-M1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.5-M1",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.5-M1",
  "org.scalatest" %% "scalatest" % "1.9.2-SNAP2" % "test")

retrieveManaged := true

EclipseKeys.relativizeLibs := true


