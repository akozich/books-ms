package com.technologyconversations.api.mongodb

import scala.util.Properties.envOrElse

trait MongoDBConfigComponent {
  def mongoDBConfig: MongoDBConfig

  case class MongoDBConfig(host: String, port: Int, dbName: String, collection: String)
}

trait EnvironmentMongoDBConfigComponent extends MongoDBConfigComponent {
  private def host: String = envOrElse("DB_PORT_27017_TCP_ADDR", envOrElse("DB_HOST", "localhost"))
  private def port: Int = envOrElse("DB_PORT_27017_PORT", "27017").toInt
  private def dbName: String = envOrElse("DB_DBNAME", "books")
  private def collection: String = envOrElse("DB_COLLECTION", "books")

  def mongoDBConfig = MongoDBConfig(host, port, dbName, collection)
}
