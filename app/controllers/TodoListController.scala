package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * インデックスページを表示
    */
  def index =
    Action { implicit request =>
      // 200 OK ステータスで app/views/index.scala.html をレンダリングする
      Ok(views.html.index("Welcome to Play application!"))
    }

  def helloWorld =
    Action { implicit request =>
      // 200 OK ステータスで app/views/index.scala.html をレンダリングする
      Ok(views.html.index("Hello world!"))
    }

  def helloToAny(name: String) =
    Action { implicit request =>
      Ok(views.html.index(s"Hello ${name}!"))
    }

}
