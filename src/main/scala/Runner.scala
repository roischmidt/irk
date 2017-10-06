import config.IrkConfig
import http.{Method, Request, RequestBuilder, RequestContainer}

object Runner extends App{
        
        val parser = new scopt.OptionParser[IrkConfig]("scopt") {
            head("scopt", "3.x")
            
            opt[Int]('c', "clientsNum").action((c, conf) =>
                conf.copy(numOfClients = c)
            ).text("number of clients")
    
            opt[Int]('t', "threadsNum").action((t, conf) =>
                conf.copy(numOfThreads = t)
            ).text("number of threads per client")
    
            opt[Int]('d', "duration").action((d, conf) =>
                conf.copy(duration = d)
            ).text("duration in seconds")
    
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
                    case false =>
                        println("ERROR: missing requests file path or url for single request")
                        System.exit(0)
                }
            case None =>
                // arguments are bad, error message will have been displayed
                println("ERROR: Bad arguments")
                System.exit(0)
        }
    
    def validateConfig(config: IrkConfig) : Boolean = {
        config.requestsPath.nonEmpty || config.getRequest.nonEmpty
    }
    
    
}
