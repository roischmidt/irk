package entities

import java.io.{File, PrintWriter}
import java.util.NoSuchElementException

import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

class EntityContainerSpec extends FunSpec with Matchers with BeforeAndAfterAll{
    
    val testFileName = "entities.txt"
    
    val ft = "GET,www.get.com,Accept: text/plain\npost,www.post.com,Accept: text/plain,post data\nput,www.put.com,,put data"
    
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
            createFile(",header,")
            val entityContainer = new EntityContainer(testFileName)
            entityContainer.isEmpty shouldBe true

            intercept[NoSuchElementException] {
                entityContainer.getNextEntity
            }
            releaseFile
        }
        
        it("valid data"){
            createFile(ft)
            val entityContainer = new EntityContainer(testFileName)
            entityContainer.getNextEntity shouldBe Entity(Method.GET,"www.get.com","Accept: text/plain","")
            entityContainer.getNextEntity shouldBe Entity(Method.POST,"www.post.com","Accept: text/plain","post data")
            entityContainer.getNextEntity shouldBe Entity(Method.PUT,"www.put.com","","put data")
            entityContainer.getNextEntity shouldBe Entity(Method.GET,"www.get.com","Accept: text/plain","")
            releaseFile
        }
    }
    
    
    override def afterAll(): Unit = releaseFile
    
}
