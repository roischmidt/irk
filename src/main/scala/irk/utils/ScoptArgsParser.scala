package irk.utils

import irk.config.IrkConfig
import irk.http.{RequestBuilder, RequestContainer}

import scala.concurrent.duration.Duration

class ScoptArgsParser {
    
    def parseArgs(args: Array[String]) : Boolean = {
        val parser = new scopt.OptionParser[IrkConfig]("irk") {
            head("irk", "3.x")
            
            opt[Int]('c', "connectionsNum").action((c, conf) =>
                conf.copy(numOfConnections = c)
            ).text("number of clients")
            
            opt[Int]('t', "threadsNum").action((t, conf) =>
                conf.copy(numOfThreads = t)
            ).text("number of threads per irk.client")
            
            opt[String]('d', "duration").action((d, conf) =>
                conf.copy(duration = Duration( d.replace("m","minute")))
            ).text("duration (2s,15m,1h)")
            
            opt[Int]('s', "sequenced").action((s, conf) =>
                conf.copy(sequential = s == 1)
            ).text("run all requests in sequence")
            
            opt[String]('f', "requestsFile").action((f, conf) =>
                conf.copy(requestsPath = Some(f))
            ).text("number of clients")
            
            arg[String]("<HTTP GET>...").unbounded().optional().action( (r,conf) =>
                conf.copy(getRequest = Some(r))).text("optional request url")
            
            help("help").text("prints this usage text")
            
        }
        
        parser.parse(args, IrkConfig()) match {
            case Some(config) =>
                println(IrkConfig.print(config))
                validateConfig(config) match {
                    case true =>
                        IrkConfig.conf = config
                        val requestBuilder = new RequestBuilder()
                        config.requestsPath match {
                            case Some(path) =>
                                RequestContainer.setRequestList(requestBuilder.loadFromFile(path))
                            case None =>
                                //we can call get because we know for sure that if file is not exists we must have url in args
                                RequestContainer.setRequestList(requestBuilder.loadFromUrl(config.getRequest.get))
                        }
                        true
                    case false =>
                        println("ERROR: missing requests file path or url for single request")
                        false
                }
            case None =>
                // arguments are bad, error message will have been displayed
                println("ERROR: Bad arguments")
                false
        }
    }
    
    def validateConfig(config: IrkConfig) : Boolean = {
        config.requestsPath.nonEmpty || config.getRequest.nonEmpty
    }
    
}
