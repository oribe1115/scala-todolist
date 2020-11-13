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

  val table = "task"

  /**
    * DB上に保存されている全てのタスクを取得する
    * @return
    */
  def list: Seq[Task] =
    Await.result(
      db.run(
        sql"SELECT id, name, description, is_done, created_at FROM #$table"
          .as[Task]
      )
    )

  def findByID(id: Int): Option[Task] =
    Await.result(
      db.run(
        sql"SELECT id, name, description, is_done, created_at FROM #$table WHERE id=#$id"
          .as[Task]
          .headOption
      )
    )

  def create(task: Task): Option[Int] = {
    var taskName    = task.name
    var description = task.description
    Await.result(
      db.run(
        sqlu"INSERT INTO #$table (name, description) VALUES ('#$taskName', '#$description')"
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
      case Task(id, name, description, isDone, _) =>
        Await.result(
          db.run(
            sqlu"UPDATE #$table SET name='#$name', description='#$description', is_done=#$isDone WHERE id = #$id"
          )
        )
    }

  def delete(task: Task): Int =
    task match {
      case Task(id, _, _, _, _) =>
        Await.result(
          db.run(
            sqlu"DELETE FROM #$table WHERE id = #$id"
          )
        )
    }

}
