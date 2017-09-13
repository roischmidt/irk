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
           "com.typesafe.akka" %% "akka-http" % akkaHttpV,
           "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test

       )
}
        