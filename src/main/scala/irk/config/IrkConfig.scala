package irk.config

import java.io.FileNotFoundException

import play.api.libs.json.{JsError, JsSuccess, Json}

/**
  * main irk.config file for running the irk
  * irk.config file name located in application.conf
  *
  * @param sequential   : clients should send messages sequential or in parallel
  * @param numOfClients : number of irk.http clients to use
  * @param numOfThreads : number of threads (connections) for every irk.client
  * @param duration : time to work in seconds
  * @param requestsPath : path to file of entities
  * @param getRequest : uri for a get request - used when needs to test single GET API call
  */
case class IrkConfig(
    sequential: Boolean = false,
    numOfClients: Int = 5,
    numOfThreads: Int = 10,
    duration: Long = 60,
    requestsPath: Option[String] = None,
    getRequest: Option[String] = None
)

object IrkConfig {
    
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
          |number of clients = ${config.numOfClients}
          |number of threads per client = ${config.numOfThreads}
          |running time in seconds = ${config.duration}
          |requests file location = ${config.requestsPath.getOrElse("NA")}
          |api to call = ${config.getRequest.getOrElse("NA")}
        """.stripMargin
    
}
