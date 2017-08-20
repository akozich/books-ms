package com.technologyconversations.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.technologyconversations.api.BookRegistryActor._
import com.technologyconversations.api.util.RouteUtils._

import scala.concurrent.Future
import scala.concurrent.duration._

//#book-routes-class
trait BookRoutes extends JsonSupport {
  //#book-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[BookRoutes])

  // other dependencies that BookRoutes use
  def bookRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-book-routes
  //#books-get-post-put
  //#book-get-delete
  lazy val bookRoutes: Route =
    pathPrefix("api" / "v1" / "books") {
      //#book-get-delete
      pathEnd {
        get {
          //#retrieve-books-logic
          val books: Future[Seq[BookReduced]] =
            (bookRegistryActor ? GetBooks).mapTo[Seq[BookReduced]]
          complete(books)
          //#retrieve-books-logic
        } ~
          post {
            //#create-post-logic
            entity(as[Book]) { book =>
              val bookCreated: Future[Option[Int]] =
                (bookRegistryActor ? CreateBook(book)).mapTo[Option[Int]]
              completeWithLocationHeader(bookCreated, StatusCodes.Created, StatusCodes.Conflict)
            }
            //#create-post-logic
          } ~
          put {
            //#create-put-logic
            entity(as[Book]) { book =>
              val bookCreated: Future[Option[Int]] =
                (bookRegistryActor ? CreateBook(book)).mapTo[Option[Int]]
              completeWithLocationHeader(bookCreated, StatusCodes.Created, StatusCodes.Conflict)
            }
          }
        //#create-put-logic
      }
    } ~
      //#books-get-post-put
      //#book-get-delete
      path("_id" / IntNumber) { id =>
        get {
          //#retrieve-book-info
          val maybeBook: Future[Option[Book]] =
            (bookRegistryActor ? GetBook(id)).mapTo[Option[Book]]
          complete(maybeBook)
          //#retrieve-book-info
        } ~
          delete {
            //#book-delete-logic
            val bookDeleted: Future[Boolean] =
              (bookRegistryActor ? DeleteBook(id)).mapTo[Boolean]
            onSuccess(bookDeleted) { deleted =>
              if (deleted) {
                log.info("Book [{}]: deleted", id)
                complete(StatusCodes.OK)
              } else {
                log.warning("Book [{}]: not deleted", id)
                complete(StatusCodes.Conflict)
              }
            }
            //#book-delete-logic
          }
      }
  //#book-get-delete
  //#all-book-routes
}
