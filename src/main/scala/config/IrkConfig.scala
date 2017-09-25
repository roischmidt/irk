package config

import java.io.FileNotFoundException

import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * main config file for running the irk
  * config file name located in application.conf
  *
  * @param sequential   : clients should send messages sequential or in parallel
  * @param numOfClients : number of http clients to use
  * @param numOfThreads : number of threads (connections) for every client
  * @param duration : time to work in seconds
  * @param requestsPath : path to file of entities
  */
case class IrkConfig(
    sequential: Boolean,
    numOfClients: Int,
    numOfThreads: Int,
    duration: Long,
    requestsPath: Option[String]
)

object IrkConfig {
    
    implicit val fmtJson = Json.format[IrkConfig]
    
    var conf: IrkConfig = IrkConfig(ConfigFactory.load().getBoolean("config.irk-config-sequential"),
        ConfigFactory.load().getInt("config.irk-config-numOfClients"),
        ConfigFactory.load().getInt("config.irk-config-numOfThreads"),
        ConfigFactory.load().getInt("config.irk-duration-in-seconds"),
        None)
    
    
    def loadFromFile(path: String): Option[IrkConfig] = {
        println("Loading config file....")
        
        try {
            val configStr = scala.io.Source.fromFile(path, "UTF-8").getLines.mkString
            
            IrkConfig.fmtJson.reads(Json.parse(configStr)) match {
                case JsSuccess(config, _) =>
                    Some(config)
                case JsError(e) =>
                    println(s"Failed to parse the content in $path: $e")
                    None
            }
            
        } catch {
            case e: FileNotFoundException =>
                println(s"$path not found")
                None
            case e: Exception =>
                println(s"error while trying to load $path file: $e")
                None
        }
    }
    
}
