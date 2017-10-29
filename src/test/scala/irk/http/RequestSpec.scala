package irk.http

import org.scalatest.{FunSpec, Matchers}
import irk.utils.HttpRawRequestParser

class RequestSpec extends FunSpec with Matchers {
    
    describe("Request object") {
        
        it("parse request from raw irk.http string") {
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
            request.uri shouldBe "www.nowhere123.com/docs/index.html"
            request.postData shouldBe None
        }
        
        it("headersToMap") {
            val headerList = List("Content-Type: application/json","Host: localhost:1234","X-Rtbkit-Timestamp: 21739172319")
            Request.headersToMap(headerList) shouldBe Map("Content-Type" -> "application/json",
                "Host" -> "localhost:1234",
            "X-Rtbkit-Timestamp" -> "21739172319")
        }
    }
    
}
