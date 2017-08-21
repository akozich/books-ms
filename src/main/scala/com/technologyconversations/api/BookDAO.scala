package com.technologyconversations.api

trait BookDAO {
  def allBooks(): Seq[BookReduced]

  def findBook(id: Int): Option[Book]

  def createBook(book: Book): Option[Int]

  def updateBook(book: Book): Option[Book]

  def deleteBook(id: Int): Unit
}

trait BookDAOComponent {
  def bookDao: BookDAO
}

trait InMemoryBookDAOComponent extends BookDAOComponent {
  lazy val bookDao: BookDAO = new InMemoryBookDAO

  class InMemoryBookDAO extends BookDAO {
    private var books = Set.empty[Book]
    private var nextId = 0

    def allBooks(): Seq[BookReduced] =
      books.toSeq.map(b => BookReduced(id = b.id, title = b.title, author = b.author))

    def findBook(id: Int): Option[Book] =
      books.find(_.id == id)

    def createBook(book: Book): Option[Int] = {
      books += book.copy(id = nextId)
      nextId += 1
      Some(nextId)
    }

    def updateBook(book: Book): Option[Book] =
      books.find(_.id == book.id).map { oldBook =>
        books -= oldBook
        books += book
        book
      }

    def deleteBook(id: Int): Unit =
      books.find(_.id == id) foreach { book => books -= book }
  }

}
