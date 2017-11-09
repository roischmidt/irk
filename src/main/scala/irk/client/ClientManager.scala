package irk.client

import java.util.concurrent.Executors

import irk.config.IrkConfig
import irk.utils.Metrics

import scala.concurrent.{ExecutionContext, Future}

object ClientManager {
    
    
    /*
        secure threads for active clients (numOfThreads) chosen by user
     */
    implicit val clientManagerExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(IrkConfig.conf.numOfThreads))
    
    /*
        this will be the pool for connections - used across all clients
     */
//    val ConnectionExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(
//        Executors.newFixedThreadPool(IrkConfig.conf.numOfConnections)
//    )

    
    def runClients(): Future[Boolean] = {
        
        val config = IrkConfig.conf
        println(s"starting ${config.numOfThreads} threads")
        val clients : List[HttpClient]= {
            var ls : List[HttpClient] = Nil
            1 to config.numOfThreads foreach   { _ =>
                ls = new HttpClient(config.duration,config.numOfConnections, config.sequential) :: ls
            }
            ls
        }

        val futureRuns = clients.map(_.run)
        val f = Future sequence futureRuns
        f map {
            results =>
              clients.foreach(_.shutDown)
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
