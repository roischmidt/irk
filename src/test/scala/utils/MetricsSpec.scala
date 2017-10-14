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
    }
    
}