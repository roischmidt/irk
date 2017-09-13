package http

import akka.http.scaladsl.model.headers.RawHeader
import org.scalatest.{FunSpec, Matchers}
import utils.HttpRawRequestParser

class RequestSpec extends FunSpec with Matchers {
    
    describe("Request object") {
        it("convert list of headers to list of HttpHeader") {
            val headers = List("Server: LiteSpeed", "Connection: close", "X-Powered-By: W3 Total Cache/0.8", "Pragma: public")
            val actualSorted = Request.headerstoHttpHeaderList(headers).sortBy(_.lowercaseName())
            val expectedSorted = List(RawHeader("Server", "LiteSpeed"),
                                         RawHeader("Connection", "close"),
                                         RawHeader("X-Powered-By", "W3 Total Cache/0.8"),
                                         RawHeader("Pragma", "public")).sortBy(_.lowercaseName)
            actualSorted.head.name shouldBe expectedSorted.head.name
            actualSorted.last.value shouldBe expectedSorted.last.value
            
        }
        
        it("parse request from raw http string") {
            val rawStr = scala.io.Source.fromResource("rawrequest.txt").mkString
            val parser = new HttpRawRequestParser
            parser.parseRawRequest(rawStr)
            val request = Request.requestFromRawParser(parser)
            request.headers.min should equal(List(
                                                     "Host: www.nowhere123.com",
                                                     "Accept: image/gif, image/jpeg, */*",
                                                     "Accept-Language: en-us",
                                                     "Accept-Encoding: gzip, deflate",
                                                     "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)"
                                                 ).min)
            request.method shouldBe Method.GET
            request.uri shouldBe "www.nowhere123.com"
            request.postData shouldBe ""
        }
    }
    
}
