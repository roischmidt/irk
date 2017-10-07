package com.softwaremill.sttp
import java.nio.file.{Files, Paths}
import java.{io, util}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.exceptions.TestFailedException
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.higherKinds

trait TestHttpServer
        extends BeforeAndAfterAll
                with ScalaFutures
                with TestingPatience {
    this: Suite =>
    protected implicit val actorSystem: ActorSystem = ActorSystem("sttp-test")
    import actorSystem.dispatcher
    
    protected implicit val materializer = ActorMaterializer()
    protected val endpoint = uri"http://localhost:$port"
    
    override protected def beforeAll(): Unit = {
        Http().bindAndHandle(serverRoutes, "localhost", port).futureValue
    }
    
    override protected def afterAll(): Unit = {
        actorSystem.terminate().futureValue
    }
    
    def serverRoutes: Route
    def port: Int
}

trait ForceWrappedValue[R[_]] {
    def force[T](wrapped: R[T]): T
}

object ForceWrappedValue extends ScalaFutures with TestingPatience {
    
    val id = new ForceWrappedValue[Id] {
        override def force[T](wrapped: Id[T]): T =
            wrapped
    }
    val future = new ForceWrappedValue[Future] {
        
        override def force[T](wrapped: Future[T]): T =
            try {
                wrapped.futureValue
            } catch {
                case e: TestFailedException if e.getCause != null => throw e.getCause
            }
    }
   
}

trait ForceWrapped extends ScalaFutures with TestingPatience { this: Suite =>
    type ForceWrappedValue[R[_]] = com.softwaremill.sttp.ForceWrappedValue[R]
    val ForceWrappedValue: com.softwaremill.sttp.ForceWrappedValue.type =
        com.softwaremill.sttp.ForceWrappedValue
    
    implicit class ForceDecorator[R[_], T](wrapped: R[T]) {
        def force()(implicit fwv: ForceWrappedValue[R]): T = fwv.force(wrapped)
    }
}


object CustomMatchers {
    class FileContentsMatch(file: java.io.File) extends Matcher[java.io.File] {
        override def apply(left: io.File): MatchResult = {
            val inBA = Files.readAllBytes(Paths.get(left.getAbsolutePath))
            val expectedBA = Files.readAllBytes(Paths.get(file.getAbsolutePath))
            MatchResult(
                util.Arrays.equals(inBA, expectedBA),
                "The files' contents are not the same",
                "The files' contents are the same"
            )
        }
    }
    
    def haveSameContentAs(file: io.File) = new FileContentsMatch(file)
}

trait TestingPatience extends PatienceConfiguration {
    override implicit val patienceConfig: PatienceConfig =
        PatienceConfig(timeout = 5.seconds, interval = 150.milliseconds)
}