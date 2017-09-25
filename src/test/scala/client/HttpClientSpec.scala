package client

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.softwaremill.sttp.asynchttpclient.future.AsyncHttpClientFutureBackend
import com.softwaremill.sttp.{ForceWrapped, SttpBackend, TestHttpServer}
import http.{Method, Request, RequestContainer}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{path => _, _}
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
    
    override def serverRoutes: Route =
        pathPrefix("test") {
            path("200".?) {
                get {
                    complete(HttpResponse(StatusCodes.OK))
                } ~ put {
                    entity(as[String]) { body: String =>
                        complete(HttpResponse(StatusCodes.OK, entity = body))
                    }
                } ~ post {
                    entity(as[String]) { body: String =>
                        complete(HttpResponse(StatusCodes.OK, entity = body))
                    }
                } ~ delete {
                    entity(as[String]) { body: String =>
                        complete(HttpResponse(StatusCodes.OK, entity = body))
                    }
                }
                
            } ~ path("201".?) {
                get {
                    complete(HttpResponse(StatusCodes.Created))
                }
            } ~ path("202".?) {
                get {
                    complete(HttpResponse(StatusCodes.Accepted))
                }
            } ~ path("203".?) {
                get {
                    complete(HttpResponse(StatusCodes.NonAuthoritativeInformation))
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
        
        val numOfThreads = 2
        val ttl = 2.seconds
        
        closeBackends = backend.close _ :: closeBackends
        
        it("test simple get request") {
            val client = new HttpClient(numOfThreads, ttl.toSeconds)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.GET, uri, List.empty))) { res =>
                res.code shouldBe 200
            }
        }
    
        it("test simple put request") {
            val client = new HttpClient(numOfThreads, ttl.toSeconds)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.PUT, uri, List.empty, Some("put test")))) { res =>
                res.code shouldBe 200
                res.unsafeBody shouldBe "put test"
            }
        }
    
        it("test simple delete request") {
            val client = new HttpClient(numOfThreads, ttl.toSeconds)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.DELETE, uri, List.empty, Some("delete test")))) { res =>
                res.code shouldBe 200
                res.unsafeBody shouldBe "delete test"
            }
        }
    
        it("test simple post request") {
            val client = new HttpClient(numOfThreads, ttl.toSeconds)
            val uri = "http://localhost:9999/test/200"
            whenReady(client.sendRequest(Request(Method.POST, uri, List.empty, Some("post test")))) { res =>
                res.code shouldBe 200
                res.unsafeBody shouldBe "post test"
            }
        }
        
        it("send ordered sequence of requests") {
            val baseUri = "localhost:9999/test"
            val requests = List(
                Request(Method.GET,s"$baseUri/200",List.empty),
                Request(Method.GET,s"$baseUri/201",List.empty),
                Request(Method.GET,s"$baseUri/202",List.empty),
                Request(Method.GET,s"$baseUri/203",List.empty))
            val startTime = System.currentTimeMillis()
            RequestContainer.setRequestList(requests)
            val client = new HttpClient(numOfThreads, ttl.toSeconds,sequenced = true)
            client.run
            System.currentTimeMillis() - startTime should be >= ttl.toMillis
        }
    
        it("send sequence of requests in parallel") {
            val baseUri = "localhost:9999/test"
            val requests = List(
                Request(Method.GET,s"$baseUri/200",List.empty),
                Request(Method.GET,s"$baseUri/201",List.empty),
                Request(Method.GET,s"$baseUri/202",List.empty),
                Request(Method.GET,s"$baseUri/203",List.empty))
            val startTime = System.currentTimeMillis()
            RequestContainer.setRequestList(requests)
            val client = new HttpClient(numOfThreads, ttl.toSeconds, sequenced = false)
            client.run
            System.currentTimeMillis() - startTime should be >= ttl.toMillis
        }
        
        
    }
    
    
    override protected def afterAll(): Unit = {
        super.afterAll()
    }
}
