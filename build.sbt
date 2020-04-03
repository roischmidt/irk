name := "irk"

version := "0.1"

organization := "roi.schmidt"

scalaVersion := "2.13.1"

test in assembly := {}

assemblyJarName in assembly := s"irk-${version.value}.jar"

mainClass in assembly := Some("Runner")

libraryDependencies ++= {
    val akkaHttpV = "10.1.11"
    val playV = "2.8.0"
    val scalaTestV = "3.1.1"
    
    Seq(
        "com.typesafe.play" %% "play-json" % playV,
        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "com.typesafe" % "config" % "1.3.1",
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.github.scopt" %% "scopt" % "4.0.0-RC2",
        "nl.grons" %% "metrics4-akka_a25" % "4.1.1",
        "org.slf4j" % "slf4j-simple" % "1.7.25",
        "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
        "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % "2.0.6" % Test,
        "com.softwaremill.sttp.client" %% "core" % "2.0.6",
        "com.typesafe.akka" %% "akka-stream" % "2.6.4"
    )
}

// META-INF discarding
assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case x => MergeStrategy.first
}


        