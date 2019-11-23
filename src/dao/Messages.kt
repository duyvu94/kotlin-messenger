package com.duyvu.dao

import org.jetbrains.exposed.sql.*

/**
 * Represents the Relationships table using Exposed as DAO.
 */
object Messages : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val mFromUserId = varchar("from_user_id", 256)
    val mToUserId = varchar("to_user_id", 256)
    val content = varchar("content", 1024)
    val datetime = date("date")
}
