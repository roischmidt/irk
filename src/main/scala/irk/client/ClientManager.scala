package irk.client

import java.util.concurrent.Executors

import irk.config.IrkConfig
import irk.utils.Metrics

import scala.concurrent.{ExecutionContext, Future}

object ClientManager {
    
    var clients: List[HttpClient] = Nil
    /*
        secure threads for active clients (numOfThreads) chosen by user
     */
    implicit val clientManagerExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(IrkConfig.conf.numOfThreads))
    
    /*
        this will be the pool for connections - used across all clients
     */
    val ConnectionExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(IrkConfig.conf.numOfConnections)
    )
    
    def runClients(): Future[Boolean] = {
        
        val config = IrkConfig.conf
        println(s"starting ${config.numOfThreads} threads")
        val futureClients = for (i <- 0 until config.numOfThreads)
            yield {
                new HttpClient(config.duration, config.sequential)(ConnectionExecutionContextPool).run
            }
        val f = Future sequence futureClients
        f map {
            results =>
                //print results map
                println(Metrics.metersToSimpleNameStringList.map(
                    e => s"[${e._1}] occurred ${e._2} times"
                ).mkString("\n"))
                results.forall(x => x)
        } recoverWith {
            case e =>
                println(s"spread error: ${e.getMessage}")
                Future.successful(false)
        }
    }
    
}
