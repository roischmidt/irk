package entities

import akka.http.scaladsl.model.headers.RawHeader
import org.scalatest.{FunSpec, Matchers}

class EntitySpec extends FunSpec with Matchers{
    
    describe("test Entity object"){
        it("convert string of headers to list of HttpHeader") {
            val headers = "Server: LiteSpeed\nConnection: close\nX-Powered-By: W3 Total Cache/0.8\nPragma: public\nExpires: Sat, 28 Nov 2009 05:36:25 GMT"
            Entity.toHttpHeaderList(Entity(Method.POST,"",headers,"")) shouldBe
                    List(RawHeader("Server"," LiteSpeed"),RawHeader("Connection"," close"),RawHeader("X-Powered-By"," W3 Total Cache/0.8"),RawHeader("Pragma"," public"),RawHeader("Expires"," Sat, 28 Nov 2009 05"))
        }
    }
    
}
