package irk.http

import java.io.{File, PrintWriter}
import java.util.NoSuchElementException

import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

class RequestContainerSpec extends FunSpec with Matchers with BeforeAndAfterAll {
    
    val testFileName = "requests.txt"
    
    val ft = scala.io.Source.fromResource("rawrequest.txt").mkString
    val fts = scala.io.Source.fromResource("rawrequests.txt").mkString
    
    def createFile(data: String) = {
        val f = new File(testFileName)
        val pw = new PrintWriter(f)
        pw.write(data)
        pw.close()
    }
    
    def releaseFile = {
        val f = new File(testFileName)
        if (f.exists())
            f.delete()
    }
    
    override def beforeAll(): Unit = {
        RequestContainer.clear
        releaseFile
    }
    
    describe(s"uploading data from entityFile") {
        it("corrupted data") {
            createFile(ft.replace("GET", "UNKNOWN"))
            val requestBuilder = new RequestBuilder()
            intercept[NoSuchElementException] {
                requestBuilder.loadFromFile(testFileName)
            }
            intercept[NoSuchElementException] {
                RequestContainer.getNextRequest
            }
            releaseFile
        }
        
        it("valid request") {
            createFile(ft)
            val requestBuilder = new RequestBuilder()
            RequestContainer.setRequestList(requestBuilder.loadFromFile(testFileName))
            val req = RequestContainer.getNextRequest
            req shouldBe Request(Method.GET, "www.nowhere123.com/docs/index.html", req.headers)
            releaseFile
        }
        
        it("multiple requests"){
            createFile(fts)
            val requestBuilder = new RequestBuilder()
            RequestContainer.setRequestList(requestBuilder.loadFromFile(testFileName))
            val req1 = RequestContainer.getNextRequest
            req1.method shouldBe Method.GET
            req1.postData shouldBe None
            val req2 = RequestContainer.getNextRequest
            req2.method shouldBe Method.PUT
            
            req2.postData shouldBe Some("{put}")
            val req3 = RequestContainer.getNextRequest
            req3.method shouldBe Method.POST
            req3.headers.contains("Header: 3") shouldBe true
            req3.postData shouldBe Some("{post}")
            val req4 = RequestContainer.getNextRequest
            req4.method shouldBe Method.DELETE
            req4.postData shouldBe Some("{delete}")
            req4.uri shouldBe "www.test4.com/docs/index.html"
            releaseFile
        }
    }
    describe("RequestBuilder tests") {
        it("loadFromUrl") {
            val requests = new RequestBuilder().loadFromUrl("www.test.com")
            requests.size should be > 0
            requests.head.method shouldBe Method.GET
        }
    }
    
    
    override def afterAll(): Unit = releaseFile
    
}
