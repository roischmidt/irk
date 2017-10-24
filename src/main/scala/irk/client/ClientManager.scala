package irk.client

import java.util.concurrent.Executors

import irk.config.IrkConfig
import irk.utils.Metrics

import scala.concurrent.{ExecutionContext, Future}

object ClientManager {
    
    var clients: List[HttpClient] = Nil
    implicit val clientManagerExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(IrkConfig.conf.numOfClients))
    
    
    def runClients(): Future[Boolean] = {
        
        val config = IrkConfig.conf
        println(s"starting ${config.numOfClients} clients")
        val futureClients = for (i <- 0 until config.numOfClients)
            yield {
                new HttpClient(config.numOfThreads, config.duration, config.sequential).run
            }
        val f = Future sequence futureClients
        f map {
            results =>
                //print results map
                println(Metrics.metersToSimpleNameStringList.map(
                    e => s"Http Code [${e._1}] occurred ${e._2} times"
                ).mkString("\n"))
                results.forall(x => x)
        } recoverWith {
            case e =>
                println(s"spread error: ${e.getMessage}")
                Future.successful(false)
        }
    }
    
}
