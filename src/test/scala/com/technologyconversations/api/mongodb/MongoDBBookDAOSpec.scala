package com.technologyconversations.api.mongodb

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.technologyconversations.api.{Book, BookReduced}
import org.scalatest._
import salat.grater
import salat.global._

class MongoDBBookDAOSpec extends FlatSpec
  with Matchers
  with BeforeAndAfter
  with MongoDBBookDAOComponent
  with EnvironmentMongoDBConfigComponent {

  val client = MongoClient(mongoDBConfig.host, mongoDBConfig.port)
  val db = client(mongoDBConfig.dbName)
  val collection = db(mongoDBConfig.collection)

  after {
    db.dropDatabase()
  }

  "MongoDBBookDAO" should "return books" in {
    val books = Seq(
      Book(
        id = 1,
        title = "Programming in Scala",
        author = "Martin Odersky, Lex Spoon, Bill Benners",
        description =
          """Being co-written by the language’s designer, Martin Odersky,
            |you will find it provides additional depth and clarity to the
            |diverse features of the language.""".stripMargin
      ),
      Book(
        id = 2,
        title = "Functional Programming in Scala",
        author = "Paul Chiusano, Runar Bjarnason",
        description =
          """Functional Programming in Scala is a serious tutorial for
            |programmers looking to learn FP and apply it to the everyday
            |business of coding.""".stripMargin
      ),
      Book(
        id = 3,
        title = "Scala for the Impatient",
        author = "Cay Horstmann",
        description =
          """A rapid introduction to Scala for programmers who are competent
            |in Java, C#, or C++""".stripMargin
      )
    )
    books.foreach(insertBook)

    val result = bookDao.allBooks()

    val expected = books.map(b => BookReduced(id = b.id, title = b.title, author = b.author))
    result shouldBe expected
  }

  it should "return empty sequence if no books found" in {
    val result = bookDao.allBooks()

    result shouldBe Seq[BookReduced]()
  }

  it should "find existing book" in {
    val book = Book(
      id = 1,
      title = "Programming in Scala",
      author = "Martin Odersky, Lex Spoon, Bill Benners",
      description =
        """Being co-written by the language’s designer, Martin Odersky,
          |you will find it provides additional depth and clarity to the
          |diverse features of the language.""".stripMargin
    )
    insertBook(book)

    val result = bookDao.findBook(1)

    result should not be None
    result.get shouldBe book
  }

  it should "return None if no book found" in {
    val result = bookDao.findBook(1)

    result shouldBe None
  }

  it should "create Book" in {
    val book = Book(
      id = 1,
      title = "Programming in Scala",
      author = "Martin Odersky, Lex Spoon, Bill Benners",
      description =
        """Being co-written by the language’s designer, Martin Odersky,
          |you will find it provides additional depth and clarity to the
          |diverse features of the language.""".stripMargin
    )

    val result = bookDao.createBook(book)
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
