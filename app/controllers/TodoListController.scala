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

  def list =
    Action { request =>
      (for {
        userIDStr <- request.session.get("todolist::userID")
      } yield {
        val taskList = tasks.listByUserID(userIDStr.toInt)
        Ok(views.html.list(taskList))
      }).getOrElse[Result](Redirect("/"))
    }

  def signup =
    Action { request =>
      Ok(views.html.signup(request))
    }

  def login =
    Action { request =>
      Ok(views.html.login(request))
    }

  def mypage =
    Action { request =>
      (for {
        userIDStr <- request.session.get("todolist::userID")
      } yield {
        users.findByID(userIDStr.toInt) match {
          case Some(user) => Ok(views.html.mypage(user)(request))
          case _ =>
            InternalServerError(
              views.html
                .internalservererror("faild to get mypage", "/", "トップページ")
            )
        }
      }).getOrElse[Result](Redirect("/"))
    }

  def taskDetail(taskID: Int) =
    Action { request =>
      (for {
        userIDStr <- request.session.get("todolist::userID")
      } yield {
        isMyTask(userIDStr.toInt, taskID) match {
          case true =>
            tasks.findByID(taskID) match {
              case Some(t) => Ok(views.html.taskdetail(t))
              case None =>
                NotFound(
                  views.html
                    .notfound(s"No task for id=${taskID}", "/tasks", "タスク一覧")
                )
            }
          case false =>
            NotFound(
              views.html
                .notfound(s"No task for id=${taskID}", "/tasks", "タスク一覧")
            )
        }
      }).getOrElse[Result](Redirect("/"))
    }

  def taskForm =
    Action { request =>
      Ok(views.html.taskForm(request))
    }

  def taskEdit(taskID: Int) =
    Action { request =>
      (for {
        userIDStr <- request.session.get("todolist::userID")
      } yield {
        isMyTask(userIDStr.toInt, taskID) match {
          case true =>
            tasks.findByID(taskID) match {
              case Some(t) => Ok(views.html.taskedit(t)(request))
              case None =>
                NotFound(
                  views.html
                    .notfound(s"No task for id=${taskID}", "/tasks", "タスク一覧")
                )
            }
          case false =>
            NotFound(
              views.html
                .notfound(s"No task for id=${taskID}", "/tasks", "タスク一覧")
            )
        }
      }).getOrElse[Result](Redirect("/"))
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
            case Some(0) => {
              users.create(User(username, password)) match {
                case Some(id) =>
                  Redirect("/tasks").withSession(
                    "todolist::userID" -> id.toString
                  )
                case None =>
                  InternalServerError(
                    views.html
                      .internalservererror("faild to signup", "/", "トップページ")
                  )
              }
            }
            case _ => BadRequest("this username has already used")
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for singup"))
    }

  def registerLogin =
    Action { request =>
      (
        for {
          param    <- request.body.asFormUrlEncoded
          username <- param.get("username").flatMap(_.headOption)
          password <- param.get("password").flatMap(_.headOption)
        } yield {
          var hashedPassword = Digest(password)
          users.findByName(username) match {
            case Some(user) => {
              if (hashedPassword == user.password)
                Redirect("/tasks").withSession(
                  "todolist::userID" -> user.id.toString
                )
              else BadRequest("password is wrong")
            }
            case _ => BadRequest("user not found")
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for login"))
    }

  def registerUpdatePassword =
    Action { request =>
      (
        for {
          userIDStr   <- request.session.get("todolist::userID")
          param       <- request.body.asFormUrlEncoded
          oldPassword <- param.get("oldPassword").flatMap(_.headOption)
          newPassword <- param.get("newPassword").flatMap(_.headOption)
        } yield {
          val userID            = userIDStr.toInt
          val oldHashedPassword = Digest(oldPassword)
          val newHashedPassword = Digest(newPassword)
          users.findByID(userID) match {
            case Some(user) => {
              if (oldHashedPassword == user.password) {
                users.updatePassword(userID, newHashedPassword) match {
                  case 1 =>
                    Ok(views.html.ok("success to update", "/mypage", "マイページ"))
                  case _ =>
                    InternalServerError(
                      views.html
                        .internalservererror(
                          "faild to update",
                          "/mypage",
                          "マイページ"
                        )
                    )
                }
              } else {
                BadRequest("password is wrong")
              }
            }
            case _ =>
              InternalServerError(
                views.html
                  .internalservererror(
                    "faild to get userdata",
                    "/mypage",
                    "マイページ"
                  )
              )
          }
        }
      ).getOrElse[Result](BadRequest("bad request for password update"))
    }

  def regiseterDeleteUser =
    Action { request =>
      (
        for {
          userIDStr <- request.session.get("todolist::userID")
          param     <- request.body.asFormUrlEncoded
          password  <- param.get("password").flatMap(_.headOption)
        } yield {
          val userID         = userIDStr.toInt
          val hashedPassword = Digest(password)
          users.findByID(userID) match {
            case Some(user) => {
              if (hashedPassword == user.password) {
                users.delete(userID) match {
                  case 1 =>
                    Ok(
                      views.html.ok(
                        "success to delete user",
                        "/signup",
                        "signup"
                      )
                    ).withNewSession
                  case _ =>
                    InternalServerError(
                      views.html
                        .internalservererror(
                          "faild to delete user",
                          "/mypage",
                          "マイページ"
                        )
                    )
                }
              } else {
                BadRequest("password is wrong")
              }
            }
            case _ =>
              InternalServerError(
                views.html
                  .internalservererror(
                    "faild to get userdata",
                    "/mypage",
                    "マイページ"
                  )
              )
          }
        }
      ).getOrElse[Result](BadRequest("bad request for delete user"))
    }

  def registerNewTask =
    Action { request =>
      (
        for {
          userIDStr   <- request.session.get("todolist::userID")
          param       <- request.body.asFormUrlEncoded
          taskName    <- param.get("taskName").flatMap(_.headOption)
          description <- param.get("description").flatMap(_.headOption)
        } yield {
          tasks.create(
            Task(taskName, description, false),
            userIDStr.toInt
          ) match {
            case Some(taskID) => Redirect(s"/tasks/$taskID")
            case _ =>
              InternalServerError(
                views.html
                  .internalservererror(
                    "faild to add task",
                    "/tasks/new",
                    "新規タスク"
                  )
              )
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for update task"))
    }

  def registerUpdateTask(taskID: Int) =
    Action { request =>
      (
        for {
          userIDStr   <- request.session.get("todolist::userID")
          param       <- request.body.asFormUrlEncoded
          taskName    <- param.get("taskName").flatMap(_.headOption)
          description <- param.get("description").flatMap(_.headOption)
          isDoneStr   <- param.get("isDone").flatMap(_.headOption)
        } yield {
          var isDone = if (isDoneStr == "on") true else false
          isMyTask(userIDStr.toInt, taskID) match {
            case true =>
              tasks.update(Task(taskID, taskName, description, isDone)) match {
                case 1 => Redirect(s"/tasks/$taskID")
                case _ =>
                  InternalServerError(
                    views.html
                      .internalservererror(
                        "faild to update task",
                        s"/tasks/$taskID/edit",
                        "タスク編集"
                      )
                  )
              }
            case false => BadRequest(s"bad request for update task")
          }
        }
      ).getOrElse[Result](BadRequest(s"bad request for update task"))
    }

  def registerDeleteTask(taskID: Int) =
    Action { request =>
      (
        for {
          userIDStr <- request.session.get("todolist::userID")
        } yield {
          isMyTask(userIDStr.toInt, taskID) match {
            case true =>
              tasks.findByID(taskID) match {
                case Some(t) => {
                  tasks.delete(t) match {
                    case 1 => Redirect("/tasks")
                    case _ =>
                      InternalServerError(
                        views.html
                          .internalservererror(
                            "faild to delete task",
                            s"/tasks/$taskID/edit",
                            "タスク編集"
                          )
                      )
                  }
                }
                case None => BadRequest("bad request for delete task")
              }
            case false => BadRequest("bad request for delete task")
          }
        }
      ).getOrElse[Result](BadRequest("bad request for dekete task"))
    }

  def isMyTask(userID: Int, taskID: Int): Boolean = {
    tasks.countByUserTask(userID, taskID) match {
      case Some(1) => true
      case _       => false
    }
  }

}
