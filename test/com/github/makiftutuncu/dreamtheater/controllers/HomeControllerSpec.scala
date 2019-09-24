package com.github.makiftutuncu.dreamtheater.controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.http.MimeTypes
import play.api.test.Helpers._
import play.api.test._

class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "Using controller instances" should {
    lazy val controller = inject[HomeController]

    "render the home page" in {
      val home = controller.home.apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
    }

    "pong" in {
      val response = controller.ping.apply(FakeRequest(GET, "/"))

      status(response)          mustBe OK
      contentType(response)     mustBe Some(MimeTypes.TEXT)
      contentAsString(response) mustBe "pong"
    }
  }

  "Using router" should {
    "render the home page" in {
      val request = FakeRequest(GET, "/")
      val response = route(app, request).get

      status(response) mustBe OK
    }

    "pong" in {
      val request = FakeRequest(GET, "/ping")
      val response = route(app, request).get

      status(response)          mustBe OK
      contentType(response)     mustBe Some(MimeTypes.TEXT)
      contentAsString(response) mustBe "pong"
    }
  }
}
