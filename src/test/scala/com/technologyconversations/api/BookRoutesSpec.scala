package com.technologyconversations.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{ TestActor, TestProbe }
import com.technologyconversations.api.BookRegistryActor._
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class BookRoutesSpec
    extends WordSpecLike
    with ScalatestRouteTest
    with BookRoutes
    with Matchers {

  val apiUri = "/api/v1/books"
  val bookId = 1234
  val invalidBookId = 1000

  val probe = TestProbe()

  def bookRegistryActor: ActorRef = probe.ref

  def executionContext: ExecutionContext = system.dispatcher

  s"GET $apiUri" should {
    val expected = Seq(
      BookReduced(0, "0", "0"),
      BookReduced(1, "1", "1"),
      BookReduced(2, "2", "2")
    )

    "return OK" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case GetBooks ⇒
          sender ! expected
          TestActor.NoAutoPilot
      })

      Get(apiUri) ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "return all books" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case GetBooks ⇒
          sender ! expected
          TestActor.NoAutoPilot
      })

      Get(apiUri) ~> bookRoutes ~> check {
        response.entity should not be None
        val books = responseAs[Seq[BookReduced]]
        books should have size expected.size
        books shouldBe expected
      }
    }

  }

  s"POST $apiUri" should {

    val expected = Book(Int.MinValue, "POST title", "Post author", "Post description")

    "return Created" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case CreateBook(`expected`) ⇒
          sender ! Some(bookId)
          TestActor.NoAutoPilot
        case CreateBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Post(apiUri, expected) ~> bookRoutes ~> check {
        response.status shouldBe Created
      }
    }

    "return new book url" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case CreateBook(`expected`) ⇒
          sender ! Some(bookId)
          TestActor.NoAutoPilot
        case CreateBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Post(apiUri, expected) ~> bookRoutes ~> check {
        header[Location] should not be None
        val location = header[Location].get
        location.getUri().toString should endWith(s"$apiUri/$bookId")
      }
    }
  }

  s"GET $apiUri/$bookId" should {

    val expected = Book(bookId, "Title", "Author", "Description")

    "return OK" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case GetBook(`bookId`) ⇒
          sender ! Some(expected)
          TestActor.NoAutoPilot
        case GetBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Get(s"$apiUri/$bookId") ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "return book" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case GetBook(`bookId`) ⇒
          sender ! Some(expected)
          TestActor.NoAutoPilot
        case GetBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Get(s"$apiUri/$bookId") ~> bookRoutes ~> check {
        response.entity should not be None
        val book = responseAs[Book]
        book shouldBe expected
      }
    }

  }

  s"GET $apiUri/$invalidBookId" should {

    val expected = Book(bookId, "Title", "Author", "Description")

    "return NotFound" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case GetBook(`bookId`) ⇒
          sender ! Some(expected)
          TestActor.NoAutoPilot
        case GetBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Get(s"$apiUri/$invalidBookId") ~> seal(bookRoutes) ~> check {
        response.status shouldBe NotFound
      }
    }

    "return None" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case GetBook(`bookId`) ⇒
          sender ! Some(expected)
          TestActor.NoAutoPilot
        case GetBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      //      Get(s"$apiUri/$invalidBookId") ~> seal(bookRoutes) ~> check {
      //        response.entity shouldBe None
      //      }
    }

  }

  s"PUT $apiUri/$bookId" should {

    val expected = Book(bookId, "Title", "Author", "Description")

    "return OK" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case UpdateBook(`expected`) ⇒
          sender ! Some(expected)
          TestActor.NoAutoPilot
        case UpdateBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Put(s"$apiUri/$bookId", expected) ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "return book" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case UpdateBook(`expected`) ⇒
          sender ! Some(expected)
          TestActor.NoAutoPilot
        case UpdateBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Put(s"$apiUri/$bookId", expected) ~> bookRoutes ~> check {
        response.entity should not be None
        val book = responseAs[Book]
        book shouldBe expected
      }
    }

  }

  s"PUT $apiUri/$invalidBookId" should {

    val expected = Book(invalidBookId, "Title", "Author", "Description")

    "return NotFound" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case UpdateBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      Put(s"$apiUri/$invalidBookId", expected) ~> seal(bookRoutes) ~> check {
        response.status shouldBe NotFound
      }
    }

    "return None" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case UpdateBook(_) ⇒
          sender ! None
          TestActor.NoAutoPilot
      })

      //      Put(s"$apiUri/$invalidBookId", expected) ~> seal(bookRoutes) ~> check {
      //        response.entity shouldBe None
      //      }
    }

  }

  s"DELETE $apiUri/$bookId" should {

    "return NoContent" in {
      probe.setAutoPilot((sender: ActorRef, msg: Any) => msg match {
        case DeleteBook(_) ⇒
          sender ! Unit
          TestActor.NoAutoPilot
      })

      Delete(s"$apiUri/$bookId") ~> bookRoutes ~> check {
        response.status shouldBe NoContent
      }
    }
  }

}
