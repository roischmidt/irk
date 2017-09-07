name := "irk"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= {
    val akkaV = "10.0.10"
    val playV = "2.6.3"
    val scalaTestV = "3.0.1"
    
    Seq(
           "com.typesafe.play" %% "play-json" % playV,
           "org.scalatest" %% "scalatest" % scalaTestV % "test",
           "com.typesafe.akka" %% "akka-http" % akkaV,
           "com.typesafe.akka" %% "akka-http-testkit" % akkaV % Test

       )
}
        