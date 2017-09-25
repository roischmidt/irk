package config

import java.io.{File, PrintWriter}

import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

class IrkConfigSpec extends FunSpec with Matchers with BeforeAndAfterAll {
    
    
    val ft = IrkConfig(sequential = true, 2, 5, 60,Some("irk.conf"))
    
    val configFileName = "irk.conf"
    
    def createFile(data: String) = {
        val f = new File(configFileName)
        val pw = new PrintWriter(f)
        pw.write(data)
        pw.close()
    }
    
    def releaseFile = {
        val f = new File(configFileName)
        if (f.exists())
            f.delete()
    }
    
    override def beforeAll(): Unit = {
        releaseFile
    }
    
    describe(s"testing config file") {
        it("file doesn't exists") {
            IrkConfig.loadFromFile(configFileName) shouldBe None
        }
        
        it("file has corrupted data") {
            createFile("{}")
            IrkConfig.loadFromFile(configFileName) shouldBe None
            releaseFile
        }
        
        it("file has valid data") {
            createFile(IrkConfig.fmtJson.writes(ft).toString())
            IrkConfig.loadFromFile(configFileName) shouldBe Some(ft)
            releaseFile
        }
        
    }
    
    override def afterAll(): Unit = releaseFile
    
    
}
