package config

import java.io.{File, PrintWriter}

import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

class IrkConfigSpec extends FunSpec with Matchers with BeforeAndAfterAll{
    
    val ft = IrkConfig(sequential = true,2,5,"x.x")
    
    def createFile(data: String) = {
        val f = new File(IrkConfig.configFileName)
        val pw = new PrintWriter(f)
        pw.write(data)
        pw.close()
    }
    
    def releaseFile = {
        val f = new File(IrkConfig.configFileName)
        if (f.exists())
            f.delete()
    }
    
    override def beforeAll(): Unit = {
      releaseFile
    }
    
    describe(s"testing ${IrkConfig.configFileName} file") {
        it("file doesn't exists") {
                IrkConfig.loadFromFile shouldBe None
        }
        
        it("file has corrupted data"){
            createFile("{}")
            IrkConfig.loadFromFile shouldBe None
            releaseFile
        }
    
        it("file has valid data"){
            createFile(IrkConfig.fmtJson.writes(ft).toString())
            IrkConfig.loadFromFile shouldBe Some(ft)
            releaseFile
        }
        
        it("init should return false if file doesn't exits"){
            IrkConfig.init shouldBe false
        }
    
        it("init should return true if file exits"){
            val config = ft
            createFile(IrkConfig.fmtJson.writes(config).toString())
            IrkConfig.init shouldBe true
            releaseFile
        }
    }
    
    override def afterAll(): Unit = releaseFile
    
}
