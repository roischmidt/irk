package irk.client

import java.util.concurrent.Executors

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.{ForceWrapped, SttpBackend, TestHttpServer}
import irk.config.IrkConfig
import irk.http.{Method, Request, RequestContainer}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{path => _, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds

class HttpClientSpec extends FunSpec with Matchers
        with BeforeAndAfterAll
        with ScalaFutures
        with OptionValues
        with IntegrationPatience
        with TestHttpServer
        with ForceWrapped
        with BeforeAndAfterEach {
    
    implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))
    
    var responseTimeMap : Map[Int,Long] = Map.empty // responses map - key=responseCode, value=TimeStamp in millis
    
    def putIfAbsent(key: Int,value: Long) =
        responseTimeMap.getOrElse(key, {
            responseTimeMap = responseTimeMap.updated(key, value)
        })
    
    override def serverRoutes: Route =
        pathPrefix("test") {
            path("200".?) {
                get {
                    complete {
                        putIfAbsent(StatusCodes.OK.intValue,System.currentTimeMillis())
                        HttpResponse(StatusCodes.OK)
                    }
                } ~ put {
                    entity(as[String]) { body: String =>
                        complete {
                            putIfAbsent(StatusCodes.OK.intValue,System.currentTimeMillis())
                            HttpResponse(StatusCodes.OK,entity = body)
                        }
                    }
                } ~ post {
                    entity(as[String]) { body: String =>
                        complete {
                            putIfAbsent(StatusCodes.OK.intValue,System.currentTimeMillis())
                            HttpResponse(StatusCodes.OK,entity = body)
                        }
                    }
                } ~ delete {
                    entity(as[String]) { body: String =>
                        complete {
                            putIfAbsent(StatusCodes.OK.intValue,System.currentTimeMillis())
                            HttpResponse(StatusCodes.OK,entity = body)
                        }
                    }
                }
                
            } ~ path("201".?) {
                get {
                    complete {
                        putIfAbsent(StatusCodes.Created.intValue,System.currentTimeMillis())
                        HttpResponse(StatusCodes.Created)
                    }
                }
            } ~ path("202".?) {
                get {
                    complete {
                        putIfAbsent(StatusCodes.Accepted.intValue,System.currentTimeMillis())
                        HttpResponse(StatusCodes.Accepted)
                    }
                }
            } ~ path("203".?) {
                get {
                    complete {
                        putIfAbsent(StatusCodes.NonAuthoritativeInformation.intValue,System.currentTimeMillis())
                        HttpResponse(StatusCodes.NonAuthoritativeInformation)
                    }
                }
                
            }
        }
    
    
    override def port = 9999
    
    var closeBackends: List[() => Unit] = Nil
    
    runTests("Async Http Client - Future")(AsyncHttpClientFutureBackend(),
        ForceWrappedValue.future)
    
    override protected def beforeAll(): Unit = {
        super.beforeAll()
        RequestContainer.clear
    }
    
    def runTests[R[_]](name: String)(
        implicit backend: SttpBackend[R, Nothing],
        forceResponse: ForceWrappedValue[R]): Unit = {
    
        val ConnectionExecutionContextPool: ExecutionContext = ExecutionContext.fromExecutor(
            Executors.newFixedThreadPool(2)
        )
        
        closeBackends = backend.close _ :: closeBackends
        
        it("test simple get request") {
            val client = new HttpClient(1.seconds)(ConnectionExecutionContextPool)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.GET, uri, List.empty))) { res =>
                res shouldBe 200
            }
        }
    
        it("test simple put request") {
            val client = new HttpClient(1.second)(ConnectionExecutionContextPool)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.PUT, uri, List.empty, Some("put test")))) { res =>
                res shouldBe 200
            }
        }
    
        it("test simple delete request") {
            val client = new HttpClient(1.seconds)(ConnectionExecutionContextPool)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.DELETE, uri, List.empty, Some("delete test")))) { res =>
                res shouldBe 200
            }
        }
    
        it("test simple post request") {
            val client = new HttpClient(1.seconds)(ConnectionExecutionContextPool)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.POST, uri, List.empty, Some("post test")))) { res =>
                res shouldBe 200
            }
        }
        
        it("send ordered sequence of requests") {
            responseTimeMap = Map.empty
            val baseUri = "localhost:9999/test"
            val requests = List(
                Request(Method.GET,s"$baseUri/200",List.empty),
                Request(Method.GET,s"$baseUri/201",List.empty),
                Request(Method.GET,s"$baseUri/202",List.empty),
                Request(Method.GET,s"$baseUri/203",List.empty))
            RequestContainer.setRequestList(requests)
            val client = new HttpClient(1.seconds,sequenced = true)(ConnectionExecutionContextPool)
            client.run
            Thread.sleep(1000)
            responseTimeMap(StatusCodes.OK.intValue) should be < responseTimeMap(StatusCodes.Created.intValue)
            responseTimeMap(StatusCodes.Created.intValue) should be < responseTimeMap(StatusCodes.Accepted.intValue)
            responseTimeMap(StatusCodes.Accepted.intValue) should be < responseTimeMap(StatusCodes.NonAuthoritativeInformation.intValue)
        }
    
        it("send sequence of requests in parallel") {
            responseTimeMap = Map.empty
            val baseUri = "localhost:9999/test"
            val requests = List(
                Request(Method.GET,s"$baseUri/200",List.empty),
                Request(Method.GET,s"$baseUri/201",List.empty),
                Request(Method.GET,s"$baseUri/202",List.empty),
                Request(Method.GET,s"$baseUri/203",List.empty))
            RequestContainer.setRequestList(requests)
            val client = new HttpClient(11.seconds, sequenced = false)(ConnectionExecutionContextPool)
            client.run
            Thread.sleep(1000)
            responseTimeMap.get(StatusCodes.OK.intValue).nonEmpty shouldBe true
            responseTimeMap.get(StatusCodes.Created.intValue).nonEmpty shouldBe true
            responseTimeMap.get(StatusCodes.Accepted.intValue).nonEmpty shouldBe true
            responseTimeMap.get(StatusCodes.NonAuthoritativeInformation.intValue).nonEmpty shouldBe true
        }
        
        
    }
    
    
    override protected def afterAll(): Unit = {
        super.afterAll()
    }
}
