name := "irk"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= {
    val akkaHttpV = "10.0.10"
    val playV = "2.6.3"
    val scalaTestV = "3.0.1"
    
    Seq(
        "com.typesafe.play" %% "play-json" % playV,
        "org.scalatest" %% "scalatest" % scalaTestV % "test",
        "com.softwaremill.sttp" %% "core" % "0.0.14",
        "com.softwaremill.sttp" %% "async-irk.http-irk.client-backend-future" % "0.0.14",
        "com.typesafe" % "config" % "1.3.1",
        "com.typesafe.akka" %% "akka-irk.http" % akkaHttpV,
        "com.github.scopt" %% "scopt" % "3.7.0"
    
    )
}
        