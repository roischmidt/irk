import irk.config.IrkConfig
import irk.utils.ScoptArgsParser
import org.scalatest.{FunSpec, Matchers}

class RunnerTest extends FunSpec with Matchers{
    
    describe("test main arguments") {
        val argsParser = new ScoptArgsParser
        val testConf = IrkConfig(
            sequential = true,
            numOfClients = 1,
            numOfThreads = 1,
            duration = 1,
            getRequest = Some("http://test.com:8888"))
        
        it("valid args") {
            argsParser.parseArgs(Array(
                "-s", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfClients}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}",
                "http://test.com:8888"
            ))
    
            IrkConfig.conf shouldBe testConf
        }
        
        it("requestPath and getRequest missing"){
            val argsPArser = new ScoptArgsParser
            argsPArser.parseArgs(Array(
                "-s", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfClients}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}"
            )) shouldBe false
    
            
        }
        
        it("verify default params when no args") {
            val argsPArser = new ScoptArgsParser
            argsPArser.parseArgs(Array(
                "-c", s"${testConf.numOfClients}",
                "http://test.com:8888"
            ))
    
            IrkConfig.conf shouldBe IrkConfig().copy(numOfClients = testConf.numOfClients,getRequest = Some("http://test.com:8888"))
        }
        
        it("bad args (-g)") {
            val argsPArser = new ScoptArgsParser
            argsPArser.parseArgs(Array(
                "-g", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfClients}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}"
            )) shouldBe false
        }
    }
    
}
