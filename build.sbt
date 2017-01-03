enablePlugins(ScalaJSPlugin)

name := "MuniMuni"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.lihaoyi" %%% "upickle" % "0.4.4"
)