package irk.utils

import scala.concurrent.{ExecutionContext, Future}

object FutureUtils {

  /**
    * run a collection of Futures in sequence (one after one)
    * taken from https://groups.google.com/forum/#!topic/scala-user/W9ykW8j3Ybg. thanks to √iktor Klang
    */
  implicit class ForeachAsync[T](iterable: Iterable[T]) {
    def foreachAsync[U](f: T => Future[U])(implicit ec: ExecutionContext): Future[Unit] = {
      def next(i: Iterator[Future[U]]): Future[Unit] =
        if (i.hasNext) i.next().flatMap(_ => next(i)) else Future.successful(())
      Future(iterable.iterator.map(f)).flatMap(next)
    }
  }

}
