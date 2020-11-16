package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import models.{Task, Tasks, User, Users}
import utility.Digest

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(tasks: Tasks)(users: Users)(
    cc: ControllerComponents
) extends AbstractController(cc) {

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

  def signup =
    Action { request =>
      Ok(views.html.signup(request))
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

  def registerSignup =
    Action { request =>
      (
        for {
          param    <- request.body.asFormUrlEncoded
          username <- param.get("username").flatMap(_.headOption)
          password <- param.get("password").flatMap(_.headOption)
        } yield {
          var hashedPassword = Digest(password)
          users.countByName(username) match {
            case 0 => {
              users.create(User(username, password)) match {
                case Some(id) => Ok("ok")
                case None     => InternalServerError("faild to signup")
              }
            }
            case _ => BadRequest("this username has already used")
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for singup"))
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

  def registerUpdateTask(id: Int) =
    Action { request =>
      (
        for {
          param       <- request.body.asFormUrlEncoded
          taskName    <- param.get("taskName").flatMap(_.headOption)
          description <- param.get("description").flatMap(_.headOption)
          isDoneStr   <- param.get("isDone").flatMap(_.headOption)
        } yield {
          var isDone = if (isDoneStr == "on") true else false
          tasks.update(Task(id, taskName, description, isDone)) match {
            case 1 => Redirect(s"/tasks/$id")
            case _ => InternalServerError(s"faild to update task")
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for update task"))
    }

  def registerDeleteTask(id: Int) =
    Action { request =>
      (
        tasks.findByID(id) match {
          case Some(t) => {
            tasks.delete(t) match {
              case 1 => Redirect(s"/tasks")
              case _ => InternalServerError(s"faild to delete task")
            }
          }
          case None => BadRequest(s"bad request for delete task")
        }
      )
    }

}
