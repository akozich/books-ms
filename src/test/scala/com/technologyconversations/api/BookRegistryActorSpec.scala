package com.technologyconversations.api

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

class BookRegistryActorSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with FlatSpecLike
    with BeforeAndAfter
    with BeforeAndAfterAll
    with InMemoryBookDAOComponent {

  def this() = this(ActorSystem("BookRegistryActorSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  before {
    bookDao.allBooks().foreach(b => bookDao.deleteBook(b.id))
  }

  "A book registry actor" should "be able to list books" in {
    val probe = TestProbe()
    val books = Seq(
      Book(
        id = Int.MinValue,
        title = "Programming in Scala",
        author = "Martin Odersky, Lex Spoon, Bill Benners",
        description =
          """Being co-written by the language’s designer, Martin Odersky,
            |you will find it provides additional depth and clarity to the
            |diverse features of the language.""".stripMargin
      ),
      Book(
        id = Int.MinValue,
        title = "Functional Programming in Scala",
        author = "Paul Chiusano, Runar Bjarnason",
        description =
          """Functional Programming in Scala is a serious tutorial for
            |programmers looking to learn FP and apply it to the everyday
            |business of coding.""".stripMargin
      ),
      Book(
        id = Int.MinValue,
        title = "Scala for the Impatient",
        author = "Cay Horstmann",
        description =
          """A rapid introduction to Scala for programmers who are competent
            |in Java, C#, or C++""".stripMargin
      )
    )

    val expected = for {
      b <- books
      id <- bookDao.createBook(b)
    } yield BookReduced(id = id, title = b.title, author = b.author)

    val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao))

    bookRegistryActor.tell(BookRegistryActor.GetBooks, probe.ref)

    probe.expectMsg(500 millis, expected)
  }

  it should "reply with empty book list if no book is known" in {
    val probe = TestProbe()
    val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao))

    bookRegistryActor.tell(BookRegistryActor.GetBooks, probe.ref)

    probe.expectMsg(500 millis, Seq[BookReduced]())
  }

  it should "store new book" in {
    val probe = TestProbe()
    val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao))
    val newBook = Book(
      id = Int.MinValue,
      title = "Scala in Action",
      author = "Nilanjan Raychaudhuri",
      description =
        """Scala in Action is a comprehensive tutorial that introduces
          |Scala through clear explanations and numerous hands-on examples.""".stripMargin
    )

    bookRegistryActor.tell(BookRegistryActor.CreateBook(newBook), probe.ref)

    val response = probe.expectMsgType[Option[Int]]
    response shouldBe defined
    val expected = response.map(id => newBook.copy(id = id))
    bookDao.findBook(response.get) shouldBe expected
  }

  it should "reply with known book" in {
    val probe = TestProbe()
    val book = Book(
      id = Int.MinValue,
      title = "Scala in Action",
      author = "Nilanjan Raychaudhuri",
      description =
        """Scala in Action is a comprehensive tutorial that introduces
          |Scala through clear explanations and numerous hands-on examples.""".stripMargin
    )
    val bookId = bookDao.createBook(book)

    val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao))

    bookRegistryActor.tell(BookRegistryActor.GetBook(bookId.get), probe.ref)

    val response = probe.expectMsgType[Option[Book]]
    response shouldBe defined
    val expected = bookId.map(id => book.copy(id = id))
    response shouldBe expected
  }

  it should "reply with empty result if book is unknown" in {
    val probe = TestProbe()
    val book = Book(
      id = Int.MinValue,
      title = "Scala in Action",
      author = "Nilanjan Raychaudhuri",
      description =
        """Scala in Action is a comprehensive tutorial that introduces
          |Scala through clear explanations and numerous hands-on examples.""".stripMargin
    )
    bookDao.createBook(book)

    val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao))

    val invalidBookId = -1
    bookRegistryActor.tell(BookRegistryActor.GetBook(invalidBookId), probe.ref)

    val response = probe.expectMsgType[Option[Book]]
    response shouldBe None
  }

  it should "delete known book" in {
    val probe = TestProbe()
    val book = Book(
      id = Int.MinValue,
      title = "Scala in Action",
      author = "Nilanjan Raychaudhuri",
      description =
        """Scala in Action is a comprehensive tutorial that introduces
          |Scala through clear explanations and numerous hands-on examples.""".stripMargin
    )
    val bookId = bookDao.createBook(book)

    val bookRegistryActor = system.actorOf(BookRegistryActor.props(bookDao))

    bookRegistryActor.tell(BookRegistryActor.DeleteBook(bookId.get), probe.ref)
    probe.receiveN(1, 500 millis)

    bookDao.findBook(bookId.get) shouldBe None
  }
}


