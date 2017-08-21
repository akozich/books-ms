package com.technologyconversations.api.mongodb

import com.mongodb.casbah.Imports._
import com.technologyconversations.api.{ Book, BookDAO, BookDAOComponent, BookReduced }
import salat._
import salat.dao.SalatDAO
import salat.global._

trait MongoDBBookDAOComponent extends BookDAOComponent {
  this: MongoDBConfigComponent =>

  lazy val bookDao: BookDAO = new MongoDBBookDAO

  class MongoDBBookDAO extends BookDAO {

    private def innerDao = new InnerBookDAO

    def allBooks(): Seq[BookReduced] =
      innerDao.collection.find().toList.map(grater[BookReduced].asObject(_))

    def findBook(id: Int): Option[Book] =
      innerDao.findOneById(id)

    def createBook(book: Book): Option[Int] =
      innerDao.insert(book)

    def updateBook(book: Book): Option[Book] = {
      val result = innerDao.update(
        MongoDBObject("_id" -> book.id),
        book,
        upsert = false,
        multi = false,
        innerDao.defaultWriteConcern
      )
      if (result.wasAcknowledged()) Some(book) else None
    }

    def deleteBook(id: Int): Boolean =
      innerDao.removeById(id).wasAcknowledged()

    private class InnerBookDAO extends SalatDAO[Book, Int](
      collection =
        MongoClient(mongoDBConfig.host, mongoDBConfig.port)(mongoDBConfig.dbName)(mongoDBConfig.collection)
    )
  }
}
