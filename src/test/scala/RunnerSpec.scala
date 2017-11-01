import irk.config.IrkConfig
import irk.utils.ScoptArgsParser
import org.scalatest.{FunSpec, Matchers}
import scala.concurrent.duration._


class RunnerSpec extends FunSpec with Matchers{
    
    describe("test main arguments") {
        val argsParser = new ScoptArgsParser
        val testConf = IrkConfig(
            sequential = true,
            numOfConnections = 1,
            numOfThreads = 1,
            duration = 1.seconds,
            getRequest = Some("http://test.com:8888"))
        
        it("valid args") {
            argsParser.parseArgs(Array(
                "-s", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfConnections}",
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
                "-c", s"${testConf.numOfConnections}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}"
            )) shouldBe false
    
            
        }
        
        it("verify default params when no args") {
            val argsPArser = new ScoptArgsParser
            argsPArser.parseArgs(Array(
                "-c", s"${testConf.numOfConnections}",
                "http://test.com:8888"
            ))
    
            IrkConfig.conf shouldBe IrkConfig().copy(numOfConnections = testConf.numOfConnections,getRequest = Some("http://test.com:8888"))
        }
        
        it("bad args (-g)") {
            val argsPArser = new ScoptArgsParser
            argsPArser.parseArgs(Array(
                "-g", s"${testConf.sequential.compare(false)}",
                "-c", s"${testConf.numOfConnections}",
                "-t", s"${testConf.numOfThreads}",
                "-d", s"${testConf.duration}"
            )) shouldBe false
        }
        
        it("secondsToString") {
            Runner.secondsToString(4444) shouldBe "00:00:04 (444)"
            Runner.secondsToString(9000000) shouldBe "02:30:00 (0)"
        }
    }
    
}
