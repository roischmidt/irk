package irk.config

import java.io.FileNotFoundException

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._

import scala.concurrent.duration._


/**
  * main irk.config file for running the irk
  * irk.config file name located in application.conf
  *
  * @param sequential   : clients should send messages sequential or in parallel
  * @param numOfConnections : number of irk.http connections to use
  * @param numOfThreads : number of threads (connections) for every irk.client
  * @param duration : time to work in seconds
  * @param requestsPath : path to file of entities
  * @param getRequest : uri for a get request - used when needs to test single GET API call
  */
case class IrkConfig(
    sequential: Boolean = false,
    numOfConnections: Int = 5,
    numOfThreads: Int = 10,
    duration: Duration = 60.seconds,
    requestsPath: Option[String] = None,
    getRequest: Option[String] = None
)

object IrkConfig {
    
    implicit object DurationFormat extends Format[Duration] {
        def reads(json: JsValue): JsResult[Duration] = LongReads.reads(json).map(_.seconds)
        def writes(o: Duration): JsValue = LongWrites.writes(o.toSeconds)
    }
    
    implicit val fmtJson = Json.format[IrkConfig]
    
    var conf: IrkConfig = IrkConfig()
    
    
    def loadFromFile(path: String): Option[IrkConfig] = {
        println("Loading irk.config file....")
        
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
    
    def print(config: IrkConfig) =
        s"""
          |run in sequence = ${config.sequential}
          |number of connections = ${config.numOfConnections}
          |number of threads = ${config.numOfThreads}
          |running time = ${config.duration}
          |requests file location = ${config.requestsPath.getOrElse("NA")}
          |api to call = ${config.getRequest.getOrElse("NA")}
        """.stripMargin
    
}
