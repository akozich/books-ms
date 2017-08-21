package com.technologyconversations.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.{Matchers, WordSpec}
import salat._
import salat.global._

import scala.concurrent.ExecutionContext

class ServiceSpec extends WordSpec 
  with Matchers 
  with ScalatestRouteTest
  with StaticRoute
  with BookRoutes {

  val client = MongoClient("localhost", 27017)
  val db = client("books")
  val collection = db("books")
  val apiUri = "/api/v1/books"
  val staticUri = "/test.html"
  val bookId = 1234

  def actorRefFactory: ActorSystem = system
  def before(): Unit = db.dropDatabase()

  def bookRegistryActor: ActorRef = TestProbe().ref
  def executionContext: ExecutionContext = system.dispatcher


  s"GET $apiUri" should {

    "return OK" in {
      Get(apiUri) ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "return all books" in {
      val expected = insertBooks(3).map { book =>
        BookReduced(book.id, book.title, book.author)
      }
      Get(apiUri) ~> bookRoutes ~> check {
        response.entity should not be None
        val books = responseAs[Seq[BookReduced]]
        books should have size expected.size
        books shouldBe expected
      }
    }

  }

  s"GET $apiUri/_id/$bookId" should {

    val expected = Book(bookId, "Title", "Author", "Description")

    "return OK" in {
      insertBook(expected)
      Get(s"$apiUri/_id/$bookId") ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "return book" in {
      insertBook(expected)
      Get(s"$apiUri/_id/$bookId") ~> bookRoutes ~> check {
        response.entity should not be None
        val book = responseAs[Book]
        book shouldBe expected
      }
    }

  }

  s"PUT $apiUri" should {

    val expected = Book(bookId, "PUT title", "Put author", "Put description")

    "return OK" in {
      Put(apiUri, expected) ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "return Book" in {
      Put(apiUri, expected) ~> bookRoutes ~> check {
        response.entity should not be None
        val book = responseAs[Book]
        book shouldBe expected
      }
    }

    "insert book to the DB" in {
      Put(apiUri, expected) ~> bookRoutes ~> check {
        response.status shouldBe OK
        val book = getBook(bookId)
        book shouldBe expected
      }
    }

    "update book when it exists in the DB" in {
      collection.insert(grater[Book].asDBObject(expected))
      Put(apiUri, expected) ~> bookRoutes ~> check {
        response.status shouldBe OK
        val book = getBook(bookId)
        book shouldBe expected
      }
    }

  }

  s"DELETE $apiUri/_id/$bookId" should {

    val expected = Book(bookId, "Title", "Author", "Description")

    "return OK" in {
      insertBook(expected)
      Delete(s"$apiUri/_id/$bookId") ~> bookRoutes ~> check {
        response.status shouldBe OK
      }
    }

    "remove book from the DB" in {
      insertBook(expected)
      Delete(s"$apiUri/_id/$bookId") ~> bookRoutes ~> check {
        response.status shouldBe OK
        getBooks should have size 0
      }
    }

  }

  def insertBook(book: Book) {
    collection.insert(grater[Book].asDBObject(book))
  }

  def insertBooks(quantity: Int): Seq[Book] = {
    val books = List.tabulate(quantity)(id => Book(id, s"Title $id", s"Author $id", s"Description $id"))
    for (book <- books) {
      collection.insert(grater[Book].asDBObject(book))
    }
    books
  }

  def getBook(id: Int): Book = {
    val dbObject = collection.findOne(MongoDBObject("_id" -> id))
    grater[Book].asObject(dbObject.get)
  }

  def getBooks: Seq[Book] = {
    collection.find().toSeq.map(grater[Book].asObject(_))
  }

}
