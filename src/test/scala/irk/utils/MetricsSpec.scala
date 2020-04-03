package utils

import irk.utils.{Instrumented, Metrics}
import org.scalatest.{FunSpec, Matchers}

class MetricsSpec extends FunSpec with Matchers with Instrumented {
    
    describe("test metrics utils") {
        it("sumMeters") {
            val previousSum = Metrics.sumMeters() // in case we have some previous meters
            metrics.meter("test1").mark()
            metrics.meter("test2").mark()
            metrics.meter("test3").mark(10)
            Metrics.sumMeters() - previousSum shouldBe 12
    
        }
        
        it("metersToSimpleNameStringList") {
            metrics.meter("non.simple.name").mark(10)
            metrics.meter("non.simple.name2").mark(20)
            Metrics.metersToSimpleNameStringList shouldBe List(("name",10),("name2",20))
        }
        
        it("getMeterBySimpleName") {
            metrics.meter("non.simple.name").mark(10)
            metrics.meter("non.simple.name2").mark(20)
            Metrics.getMeterBySimpleName("name").get.count shouldBe 10
            Metrics.getMeterBySimpleName("name2").get.count shouldBe 20
        }
    }
    
}