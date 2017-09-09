package client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import entities.{Entity, Method}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AkkaHttpClient(numOfThreads: Int) {
    
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    
    val clientPool = java.util.concurrent.Executors.newFixedThreadPool(numOfThreads)
    
    def sendRequest(entity: Entity): Future[HttpResponse] =
        Http().singleRequest(HttpRequest(method = HttpMethod.custom(entity.method.toString),
                                            uri = entity.uri,
                                            headers = Entity.toHttpHeaderList(entity),
                                            entity = HttpEntity(entity.postData)))
}

//TODO: for tests
object client{
    def main(args: Array[String]): Unit = {
        val c = new AkkaHttpClient(1)
        val headers = "Server: LiteSpeed\nConnection: close\nX-Powered-By: W3 Total Cache/0.8\nPragma: public\nExpires: Sat, 28 Nov 2009 05:36:25 GMT"
        val entity = Entity(Method.POST,"http://localhost:12345/200",headers,"some data")
        val res = Await.result(c.sendRequest(entity),Duration("10 seconds"))
        println(res)
    }
}

