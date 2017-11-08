package irk.client


import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import irk.config.IrkConfig
import irk.http.{Request, RequestContainer}
import irk.utils.FutureUtils.ForeachAsync
import irk.utils.Instrumented
import irk.utils.TimeUtils._
import irk.utils.clients.{AsyncApacheHttpClient, IrkClient, SttpClient}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class HttpClient(timeout: Duration, sequenced: Boolean = false)(implicit numOfConnections: ExecutionContext) extends Instrumented {

  val connectionTimeout: java.time.Duration = ConfigFactory.load().getDuration("irk.client.connectionTimeout")
  // client to use
  val client: IrkClient = new AsyncApacheHttpClient(connectionTimeout,IrkConfig.conf.numOfConnections)//new SttpClient(connectionTimeout)


  val FINISHED = true

  val shouldRun: Boolean = true


  def sendRequest(request: Request): Future[Unit] = {
    metrics.counter("requestCount").inc()
    client.sendRequest(request).map{
      responseCode =>
        metrics.meter(s"$responseCode").mark()
    } recover{
      case e =>
        metrics.meter(s"${e.getMessage}").mark()
        metrics.counter("requestCount").dec()
    }
  }

  /**
    * main irk.client function. start all requests until time ends
    */
  def run: Future[Boolean] =
    if (sequenced)
      sendSequenceOrdered
    else
      sendSequenceParallel


  private def sendSequenceParallel: Future[Boolean] = Future {
    runUntil(timeout) {
      sendRequest(RequestContainer.getNextRequest)
    }
    client.close
    FINISHED
  }


  private def sendSequenceOrdered: Future[Boolean] = Future {
    val startTime = System.currentTimeMillis()
    val ttl = startTime + timeout.toMillis
    runUntil(timeout) {
      Await.ready(
         RequestContainer.getAsList.foreachAsync(sendRequest)
        ,Duration(ttl - (System.currentTimeMillis()-startTime) ,TimeUnit.MILLISECONDS))
    }
    client.close
    FINISHED
  }


}

