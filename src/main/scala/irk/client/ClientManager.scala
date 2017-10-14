package irk.client

import irk.config.IrkConfig

object ClientManager {
    

    def runClients() = {
        try {
            val config = IrkConfig.conf
            for (i <- 0 until config.numOfClients)
                new HttpClient(config.numOfThreads, config.duration, config.sequential).run
        } catch {
            case e : Exception =>
                println(s"there was an exception  : $e")
        }
    }
    
}
