package http

import java.io.{File, PrintWriter}
import java.util.NoSuchElementException

import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

class RequestContainerSpec extends FunSpec with Matchers with BeforeAndAfterAll {
    
    val testFileName = "requests.txt"
    
    val ft = scala.io.Source.fromResource("rawrequest.txt").mkString
    
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
        
        it("valid data") {
            createFile(ft)
            val requestBuilder = new RequestBuilder()
            RequestContainer.setRequestList(requestBuilder.loadFromFile(testFileName))
            val req = RequestContainer.getNextRequest
            req shouldBe Request(Method.GET, "www.nowhere123.com", req.headers, "")
            // RequestContainer.getNextRequest shouldBe Request(Method.GET, "www.get.com", List("Accept: text/plain"), "")
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
