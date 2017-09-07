package config

import java.io.FileNotFoundException
import com.typesafe.config.ConfigFactory

import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * main config file for running the irk
  * config file name located in application.conf
  * @param sequential : clients should send messages sequential or in parallel
  * @param numOfClients : number of http clients to use
  * @param numOfThreads : number of threads (connections) for every client
  * @param entitiesPath : path to file of entities
  */
case class IrkConfig(
    sequential: Boolean,
    numOfClients: Int,
    numOfThreads: Int,
    entitiesPath: String
)

object IrkConfig {
    
    val configFileName : String = ConfigFactory.load().getString("config.irk-config-file-name")
    
    implicit val fmtJson = Json.format[IrkConfig]
    
    var irkConfig: Option[IrkConfig] = None
    
    def init : Boolean =
        loadFromFile match {
            case c@Some(_) =>
                irkConfig = c
                true
            case None =>
                false
        }
    
    def loadFromFile : Option[IrkConfig] = {
        println("Loading config file....")
        
        try {
            val configStr = scala.io.Source.fromFile(configFileName, "UTF-8").getLines.mkString
            
            IrkConfig.fmtJson.reads(Json.parse(configStr)) match {
                case JsSuccess(config, _) =>
                    Some(config)
                case JsError(e) =>
                    println(s"Failed to parse the content in $configFileName: $e")
                    None
            }
            
        } catch {
            case e: FileNotFoundException =>
                println(s"$configFileName not found")
                None
            case e: Exception =>
                println(s"error while trying to load $configFileName file: $e")
                None
        }
    }
    
}
