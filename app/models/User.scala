package models

import java.sql.Timestamp

/**
  * Domain model of user
  * @param id          ID
  * @param name        アカウント名
  * @param password    パスワード
  * @param createdAt   作成日時
  */
case class User(
    id: Int,
    name: String,
    password: String,
    createdAt: Timestamp
)

object User extends DomainModel[User] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[User] =
    GetResult(
      r =>
        User(
          r.nextInt,
          r.nextString,
          r.nextString,
          r.nextTimestamp
        )
    )

  def apply(name: String, password: String): User =
    User(0, name, password, null)
}
