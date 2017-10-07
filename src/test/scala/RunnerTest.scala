import config.IrkConfig
import org.scalatest.{FunSpec, Matchers}

class RunnerTest extends FunSpec with Matchers{
    
    describe("test main arguments") {
        val testConf = IrkConfig(
            sequential = true,
            numOfClients = 50,
            numOfThreads = 20,
            duration = 120,
            requestsPath = Some("/mnt/c/tmp/api.txt"),
            getRequest = Some("http://test.com:8888"))
        
        it("valid args") {
            Runner.main(Array(
                "-s", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfClients}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}",
                "-f", s"${testConf.requestsPath.get}",
                "http://test.com:8888"
            ))
    
            IrkConfig.conf shouldBe testConf
        }
        
        it("requestPath and getRequest missing"){
            Runner.parseArgs(Array(
                "-s", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfClients}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}"
            )) shouldBe false
    
            
        }
        
        it("verify default params when no args") {
            Runner.main(Array(
                "-f", s"${testConf.requestsPath.get}"
            ))
    
            IrkConfig.conf shouldBe IrkConfig().copy(requestsPath = testConf.requestsPath)
        }
        
        it("bad args (-g)") {
            Runner.parseArgs(Array(
                "-g", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfClients}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}"
            )) shouldBe false
        }
    }
    
}
