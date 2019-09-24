package com.github.makiftutuncu.dreamtheater.utilities

import com.github.makiftutuncu.dreamtheater.errors.APIError

import scala.concurrent.{ExecutionContext, Future}

object Maybe {
  type M[A]  = Either[APIError, A]
  type FM[A] = Future[M[A]]

  def value[A](a: A): M[A]   = Right(a)
  def valueF[A](a: A): FM[A] = Future.successful(value(a))

  def error[A](e: APIError): M[A]   = Left(e)
  def errorF[A](e: APIError): FM[A] = Future.successful(error(e))

  def map[A, B](m: M[A])(f: A => B): M[B]                                    = m.map(f)
  def mapF[A, B](fm: FM[A])(f: A => B)(implicit ec: ExecutionContext): FM[B] = fm.map(m => map(m)(f))

  def flatMap[A, B](m: M[A])(f: A => M[B]): M[B]                                     = m.flatMap(f)
  def flatMapF[A, B](fm: FM[A])(f: A => FM[B])(implicit ec: ExecutionContext): FM[B] = fm.flatMap(m => m.fold(e => errorF(e), f))

  def transformF[A, B](fm: FM[A])(f: A => M[B])(implicit ec: ExecutionContext): FM[B] =
    fm.map {
      case Left(e)  => error(e)
      case Right(a) => f(a)
    }
}
