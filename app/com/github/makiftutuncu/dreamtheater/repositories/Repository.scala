package com.github.makiftutuncu.dreamtheater.repositories

import java.sql.Connection

import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future}

abstract class Repository(db: Database) {
  def withConnection[A](action: Connection => A)(implicit ec: ExecutionContext): Future[A] = Future(db.withConnection(action))
}
