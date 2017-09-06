name := "irk"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= {
    val akkaV = "2.4.16"
    val playV = "2.6.3"
    
    Seq("com.typesafe.play" %% "play-json" % playV
       )
}
        