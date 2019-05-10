package com.github.makiftutuncu.dreamtheater.controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._

class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "HomeController" should {
    "render the home page from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents())
      val home = controller.home.apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include ("Dream Theater is running!")
    }

    "render the home page from the application" in {
      val controller = inject[HomeController]
      val home = controller.home.apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include ("Dream Theater is running!")
    }

    "render the home page from the router" in {
      val request = FakeRequest(GET, "/")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include ("Dream Theater is running!")
    }
  }
}
