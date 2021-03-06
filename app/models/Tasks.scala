package models

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}
import scala.concurrent.Await

/**
  * task テーブルへの Accessor
  */
@Singleton
class Tasks @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table         = "task"
  val userTaskTable = "user_task"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list: Seq[Task] =
    Await.result(
      db.run(
        sql"SELECT id, name, description, is_done, created_at,　updated_at FROM #$table"
          .as[Task]
      )
    )

  def listByUserID(userID: Int): Seq[Task] =
    Await.result(
      db.run(
        sql"SELECT id, name, description, is_done, created_at,　updated_at FROM #$table WHERE id IN (SELECT task_id FROM #$userTaskTable WHERE user_id=#$userID)"
          .as[Task]
      )
    )

  def listByUserIDWithoutFinish(userID: Int): Seq[Task] =
    Await.result(
      db.run(
        sql"SELECT id, name, description, is_done, created_at,　updated_at FROM #$table WHERE id IN (SELECT task_id FROM #$userTaskTable WHERE user_id=#$userID) AND is_done != TRUE"
          .as[Task]
      )
    )

  def findByID(id: Int): Option[Task] =
    Await.result(
      db.run(
        sql"SELECT id, name, description, is_done, created_at, updated_at FROM #$table WHERE id=#$id"
          .as[Task]
          .headOption
      )
    )

  def create(task: Task, userID: Int): Option[Int] = {
    var taskName    = task.name
    var description = task.description
    Await.result(
      db.run(
        sqlu"INSERT INTO #$table (name, description) VALUES ('#$taskName', '#$description')"
      )
    )
    Await.result(
      db.run(
        sqlu"INSERT INTO #$userTaskTable (user_id, task_id) VALUES ('#$userID', LAST_INSERT_ID())"
      )
    )
    Await.result(
      db.run(
        sql"SELECT LAST_INSERT_ID() FROM #$table".as[Int].headOption
      )
    )
  }

  def update(task: Task): Int =
    task match {
      case Task(id, name, description, isDone, _, _) =>
        Await.result(
          db.run(
            sqlu"UPDATE #$table SET name='#$name', description='#$description', is_done=#$isDone WHERE id = #$id"
          )
        )
    }

  def delete(task: Task): Int =
    task match {
      case Task(id, _, _, _, _, _) =>
        Await.result(
          db.run(
            sqlu"DELETE FROM #$table WHERE id = #$id"
          )
        )
    }

  def countByUserTask(userID: Int, taskID: Int): Option[Int] =
    Await
      .result(
        db.run(
          sql"SELECT COUNT(*) FROM #$userTaskTable WHERE user_id=#$userID AND task_id=#$taskID"
            .as[Int]
            .headOption
        )
      )

}
