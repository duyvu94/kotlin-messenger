package com.duyvu.dao

import org.jetbrains.exposed.sql.*

/**
 * Represents the Relationships table using Exposed as DAO.
 */
object Relationships : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val fromUserId = varchar("from_user_id", 128)
    val toUserId = varchar("to_user_id", 256)
    val isConfirmed = bool("is_confirmed")
}
