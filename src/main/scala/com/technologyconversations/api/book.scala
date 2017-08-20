package com.technologyconversations.api

import salat.annotations.Key

//#book-case-classes
case class BookReduced(@Key("_id") id: Int, title: String, author: String)

case class Book(@Key("_id") id: Int, title: String, author: String, description: String) {
  require(!title.isEmpty)
  require(!author.isEmpty)
}

//#book-case-classes
