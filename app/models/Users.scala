package models

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * user テーブルへの Accessor
  */
@Singleton
class Users @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "user"

  /**
    * DB上に保存されている全てのユーザーを取得する
    * @return
    */
  def list: Seq[User] =
    Await.result(
      db.run(
        sql"SELECT id, name, password, created_at, updated_at FROM #$table"
          .as[User]
      )
    )

  def findByID(id: Int): Option[User] =
    Await.result(
      db.run(
        sql"SELECT id, name, password, created_at, updated_at FROM #$table WHERE id=#$id"
          .as[User]
          .headOption
      )
    )

  def findByName(name: String): Option[User] =
    Await.result(
      db.run(
        sql"SELECT id, name, password, created_at, updated_at FROM #$table WHERE name='#$name'"
          .as[User]
          .headOption
      )
    )

  def countByName(name: String): Option[Int] =
    Await
      .result(
        db.run(
          sql"SELECT COUNT(*) FROM #$table WHERE name='#$name'"
            .as[Int]
            .headOption
        )
      )

  def create(user: User): Option[Int] = {
    var name     = user.name
    var password = user.password
    Await.result(
      db.run(
        sqlu"INSERT INTO #$table (name, password) VALUES ('#$name', '#$password')"
      )
    )
    Await.result(
      db.run(
        sql"SELECT LAST_INSERT_ID() FROM #$table".as[Int].headOption
      )
    )
  }

  def updatePassword(id: Int, password: String): Int =
    Await.result(
      db.run(
        sqlu"UPDATE #$table SET password='#$password' WHERE id=#$id"
      )
    )

}
