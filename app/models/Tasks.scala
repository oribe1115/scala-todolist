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

  def save(task: Task): Int = {
    var taskName    = task.name
    var description = task.description
    Await.result(
      db.run(
        sqlu"INSERT INTO #$table (name, description) VALUES ('#$taskName', '#$description')"
      )
    )
  }

}
