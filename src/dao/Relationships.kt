package com.duyvu.dao

import org.jetbrains.exposed.sql.*

/**
 * Represents the Relationships table using Exposed as DAO.
 */
object Relationships : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val fromUserId = varchar("from_user_id", 128).uniqueIndex()
    val toUserId = varchar("to_user_id", 256)
    val isConfirmed = bool("is_confirmed")
}
