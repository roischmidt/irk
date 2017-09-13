package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import config.IrkConfig
import http.{Request, Method}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AkkaHttpClient(numOfThreads: Int) {
    
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    
    val clientPool = java.util.concurrent.Executors.newFixedThreadPool(numOfThreads)
    
    def sendRequest(request: Request): Future[HttpResponse] =
        Http().singleRequest(HttpRequest(method = HttpMethod.custom(request.method.toString),
                                            uri = request.uri,
                                            headers = Request.headerstoHttpHeaderList(request.headers),
                                            entity = HttpEntity(request.postData)))
    
    def sendSequenceParallel : Unit = {
        //TODO: change entityContainer to object and take entity file from given location
    }
    
    def sendSequenceOrdered : Unit = {
    
    }
}


