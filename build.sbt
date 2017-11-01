name := "irk"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= {
    val akkaHttpV = "10.0.10"
    val playV = "2.6.7"
    val scalaTestV = "3.0.1"
    
    Seq(
        "com.typesafe.play" %% "play-json" % playV,
        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "com.softwaremill.sttp" %% "core" % "0.0.14",
        "com.softwaremill.sttp" %% "async-http-client-backend-future" % "0.0.14",
        "com.typesafe" % "config" % "1.3.1",
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.github.scopt" %% "scopt" % "3.7.0",
        "nl.grons" %% "metrics-scala" % "3.5.9"
    
    )
}
        