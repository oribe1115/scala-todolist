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
        sql"SELECT id, name, password, created_at FROM #$table"
          .as[User]
      )
    )

  def findByID(id: Int): Option[User] =
    Await.result(
      db.run(
        sql"SELECT id, name, password, created_at FROM #$table WHERE id=#$id"
          .as[User]
          .headOption
      )
    )

  def create(user: User) =
    Await.result(
      db.run(
        sqlu"INSERT INTO #$table (name, password) VALUES ('#$user.name', '#$user.password')"
      )
    )

}
