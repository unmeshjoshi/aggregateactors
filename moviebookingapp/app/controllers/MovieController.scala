package controllers

import javax.inject.Inject
import play.api.mvc._

class MovieController @Inject()(cc: ControllerComponents)(
    implicit assetsFinder: AssetsFinder)
    extends AbstractController(cc) {
  def index() = Action {
    Ok(views.html.main("hello world of Play!"))
  }

  def shows() = Action {
    Ok(views.html.shows("hello shows"))
  }

  def bookSeats() = Action {
    Ok(views.html.bookseats())
  }
}
