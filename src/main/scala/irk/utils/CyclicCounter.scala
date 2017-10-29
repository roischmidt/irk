package irk.utils

import java.util.concurrent.atomic.AtomicInteger
import scala.compat.java8.FunctionConverters._

class CyclicCounter(numOfElements: Integer) {
    
    val maxVal: Integer = numOfElements
    
    /**
      * we want to initialize with numOfElements n order to start from 0 on first call for incrementAndGet
      * instead of 1
      */
    val counter = new AtomicInteger(maxVal)
    
    def incrementAndGet: Integer =
        Predef.int2Integer(counter.accumulateAndGet(1,
            asJavaIntBinaryOperator((index, _) =>
                if (index + 1 > maxVal)
                    0
                else
                    index + 1
            )))
    
    
}
