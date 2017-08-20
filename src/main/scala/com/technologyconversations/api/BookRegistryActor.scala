package com.technologyconversations.api

import akka.actor.{ Actor, ActorLogging, Props }

object BookRegistryActor {

  final case object GetBooks

  final case class CreateBook(book: Book)

  final case class GetBook(id: Int)

  final case class DeleteBook(id: Int)

  def props(booksDao: BookDAO): Props = Props(new BookRegistryActor(booksDao))
}

class BookRegistryActor(bookDao: BookDAO) extends Actor with ActorLogging {

  import BookRegistryActor._

  def receive: Receive = {
    case GetBooks =>
      sender() ! bookDao.allBooks()
    case CreateBook(book) =>
      sender() ! bookDao.createBook(book)
    case GetBook(id) =>
      sender() ! bookDao.findBook(id)
    case DeleteBook(id) =>
      sender() ! bookDao.deleteBook(id)
  }
}
