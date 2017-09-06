package config

import play.api.libs.json.{JsError, JsSuccess, Json}

case class IrkConfig(
    sequential: Boolean, // clients should send messages sequential or in parallel
    numOfClients: Int, // number of http clients to use
    numOfThreads: Int, // number of threads (connections) for every client
    entitiesPath: String // path to file of entities
)

object IrkConfig {
    
    implicit val fmtJson = Json.format[IrkConfig]
    
    var irkConfig: Option[IrkConfig] = None
    
    def loadFromFile = {
        println("Loading config file....")
        
        try {
            val configStr = scala.io.Source.fromFile("irk.conf", "UTF-8").getLines.mkString
            
            IrkConfig.fmtJson.reads(Json.parse(configStr)) match {
                case JsSuccess(config, _) =>
                    irkConfig = Some(config)
                case JsError(e) =>
                    println(s"Failed to load config file: $e")
                    System.exit(1)
            }
            
        } catch {
            case e: Exception => println(s"error while trying to load config file: $e")
        }
    }
    
}
