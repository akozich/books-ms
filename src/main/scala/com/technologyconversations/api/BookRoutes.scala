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
import com.technologyconversations.api.util.RouteUtils

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

//#book-routes-class
trait BookRoutes extends JsonSupport {
  //#book-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[BookRoutes])

  // other dependencies that BookRoutes use
  def bookRegistryActor: ActorRef

  // Needed for the Future and its methods flatMap/onComplete in the end
  implicit def executionContext: ExecutionContext

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-book-routes
  //#books-get-post
  //#book-get-put-delete
  lazy val bookRoutes: Route =
    pathPrefix("api" / "v1" / "books") {
      concat(
        //#book-get-put-delete
        pathEnd {
          concat(
            get {
              //#retrieve-books-logic
              val books: Future[Seq[BookReduced]] =
                (bookRegistryActor ? GetBooks).mapTo[Seq[BookReduced]]
              complete(books)
              //#retrieve-books-logic
            },
            post {
              //#create-book-logic
              entity(as[Book]) { book =>
                val bookCreated: Future[Option[Int]] =
                  (bookRegistryActor ? CreateBook(book)).mapTo[Option[Int]]
                RouteUtils.completeWithLocationHeader(bookCreated, StatusCodes.Created, StatusCodes.Conflict)
              }
              //#create-book-logic
            }
          )
        },
        //#books-get-post
        //#book-get-put-delete
        path(IntNumber) { id =>
          concat(
            get {
              //#retrieve-book-info
              val maybeBook: Future[Option[Book]] =
                (bookRegistryActor ? GetBook(id)).mapTo[Option[Book]]
              rejectEmptyResponse(complete(maybeBook))
              //#retrieve-book-info
            },
            put {
              //#update-book-logic
              entity(as[Book]) { book =>
                val maybeBook: Future[Option[Book]] =
                  (bookRegistryActor ? UpdateBook(book)).mapTo[Option[Book]]
                rejectEmptyResponse(complete(maybeBook))
              }
              //#update-book-logic
            },
            delete {
              //#delete-book-logic
              val bookDeleted: Future[Unit] =
                (bookRegistryActor ? DeleteBook(id)).map(_ => ())
              RouteUtils.complete(bookDeleted)
              //#delete-book-logic
            }
          )
        }
      //#book-get-put-delete
      )
    }
  //#all-book-routes
}
