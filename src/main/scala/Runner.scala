import irk.config.IrkConfig
import irk.http.{Method, Request, RequestBuilder, RequestContainer}

/**
  *  main runner class. use scopt [https://github.com/scopt/scopt] in order to parse app arguments
  */
object Runner extends App{
        
    
    if (parseArgs(args)) {
    
    } else {
        System.exit(0)
    }
    
    
    def parseArgs(args: Array[String]) : Boolean = {
        val parser = new scopt.OptionParser[IrkConfig]("scopt") {
            head("scopt", "3.x")
        
            opt[Int]('c', "clientsNum").action((c, conf) =>
                conf.copy(numOfClients = c)
            ).text("number of clients")
        
            opt[Int]('t', "threadsNum").action((t, conf) =>
                conf.copy(numOfThreads = t)
            ).text("number of threads per irk.client")
        
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
