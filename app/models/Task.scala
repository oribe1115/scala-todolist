package models

import java.sql.Timestamp

/**
  * Domain model of task
  * @param id          ID
  * @param name        タスク名
  * @param description タスクの説明
  * @param isDone      完了状態
  * @param createdAt   作成日時
  * @param updatedAt   更新日時
  */
case class Task(
    id: Int,
    name: String,
    description: String,
    isDone: Boolean,
    createdAt: Timestamp,
    updatedAt: Timestamp
)

object Task extends DomainModel[Task] {
  import slick.jdbc.GetResult
  override implicit def getResult: GetResult[Task] =
    GetResult(
      r =>
        Task(
          r.nextInt,
          r.nextString,
          r.nextString,
          r.nextBoolean,
          r.nextTimestamp,
          r.nextTimestamp
        )
    )

  def apply(name: String, description: String, isDone: Boolean): Task =
    Task(0, name, description, isDone, null, null)

  def apply(id: Int, name: String, description: String, isDone: Boolean): Task =
    Task(id, name, description, isDone, null, null)
}
