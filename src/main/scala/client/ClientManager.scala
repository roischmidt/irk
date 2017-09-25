package client

import config.IrkConfig

object ClientManager {

    def runClients() = {
        val config = IrkConfig.conf
        for (i <- 0 until config.numOfClients)
            new HttpClient(config.numOfThreads,config.duration,config.sequential).run
    }
    
}
