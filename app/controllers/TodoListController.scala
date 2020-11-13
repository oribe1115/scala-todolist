package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import models.{Task, Tasks}

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(tasks: Tasks)(cc: ControllerComponents) extends AbstractController(cc) {

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

  def list =
    Action { implicit request =>
      val taskList = tasks.list
      Ok(views.html.list(taskList))
    }

  def taskDetail(id: Int) =
    Action { request =>
      {
        tasks.findByID(id) match {
          case Some(t) => Ok(views.html.taskdetail(t))
          case None    => NotFound(s"No task for id=${id}")
        }
      }
    }

  def taskForm =
    Action { request =>
      Ok(views.html.taskForm(request))
    }

  def taskEdit(id: Int) =
    Action { request =>
      {
        tasks.findByID(id) match {
          case Some(t) => Ok(views.html.taskedit(t)(request))
          case None    => NotFound(s"No task for id=${id}")
        }
      }
    }

  def registerNewTask =
    Action { request =>
      (
        for {
          param       <- request.body.asFormUrlEncoded
          taskName    <- param.get("taskName").flatMap(_.headOption)
          description <- param.get("description").flatMap(_.headOption)
        } yield {
          tasks.create(Task(taskName, description, false)) match {
            case Some(id) => Redirect(s"/tasks/$id")
            case None     => InternalServerError(s"faild to add task")
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for add task"))
    }

}
