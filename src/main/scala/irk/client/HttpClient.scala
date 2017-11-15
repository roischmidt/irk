package irk.client


import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import irk.config.IrkConfig
import irk.http.{Request, RequestContainer}
import irk.utils.FutureUtils.ForeachAsync
import irk.utils.Instrumented
import irk.utils.TimeUtils._
import irk.utils.clients.{AsyncApacheHttpClient, IrkClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class HttpClient(timeout: Duration,numberOfConnections: Int, sequenced: Boolean = false) extends Instrumented {

  val connectionTimeout: java.time.Duration = ConfigFactory.load().getDuration("irk.client.connectionTimeout")
  // client to use
  val client: IrkClient = new AsyncApacheHttpClient(connectionTimeout,numberOfConnections)


  val FINISHED = true

  def sendRequest(request: Request): Future[Unit] = {
    client.sendRequest(request).map{
      responseCode =>
        metrics.counter("requestCount").inc()
        metrics.meter(s"$responseCode").mark()
    } recover{
      case e =>
        metrics.meter(s"${e.getMessage}").mark()
    }
  }

  /**
    * BLOCKING!!!
    * main irk.client function. start all requests until time ends
    */
  def run: Boolean =
    if (sequenced)
      sendSequenceOrdered
    else
      sendSequenceParallel

  /**
    * BLOCKING!!!
    * @return
    */
  private def sendSequenceParallel: Boolean = {
    val startTime = System.currentTimeMillis()
    val requestList = createFutureRequest
    val ttl = startTime + timeout.toMillis
    runUntil(timeout) {
      Await.ready(
        Future sequence requestList.map(sendRequest)
        ,Duration(ttl - (System.currentTimeMillis()-startTime) ,TimeUnit.MILLISECONDS))
    }
    FINISHED
  }

  /**
    * creates a list of requests of size numOfConnections.
    * @return
    */
  private def createFutureRequest : List[Request] = {
    var ls : List[Request]  = Nil
    // in case that we have connectionNum less than request list size we should have only one iteration
    val numOfRequestsToProcessInOneIteration = Math.max(1,IrkConfig.conf.numOfConnections/RequestContainer.getAsList.size)
    for(i <- 0 until numOfRequestsToProcessInOneIteration)
      ls =  RequestContainer.getAsList ::: ls
    ls
  }

  /**
    * BLOCKING!!!
    * @return
    */
  private def sendSequenceOrdered: Boolean = {
    val startTime = System.currentTimeMillis()
    val ttl = startTime + timeout.toMillis
    runUntil(timeout) {
      Await.ready(
         RequestContainer.getAsList.foreachAsync(sendRequest)
        ,Duration(ttl - (System.currentTimeMillis()-startTime) ,TimeUnit.MILLISECONDS))
    }
    FINISHED
  }

  def shutDown = client.close


}

