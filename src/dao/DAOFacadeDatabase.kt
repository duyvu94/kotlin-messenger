package com.duyvu.dao

import com.duyvu.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.*

interface DAOFacade : Closeable {
    fun init()

    fun user(userId: String, hash: String? = null): User?

    fun userByEmail(email: String): User?

    fun createUser(user: User)

    fun friendRequestSentList(userId: String): List<User>

    fun friendRequestWaitingList(userId: String): List<User>

    fun friendList(userId: String): List<User>

    fun createRelationship(fromId: String, toId: String): Boolean

    fun confirmRelationship(fromUserId: String, toUserId: String): Boolean

}

class DAOFacadeDatabase(val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")): DAOFacade {
    constructor(dir: File) : this(
        Database.connect(
            "jdbc:h2:file:${dir.canonicalFile.absolutePath}",
            driver = "org.h2.Driver"
        )
    )

    override fun createRelationship(fromId: String, toId: String): Boolean = transaction(db) {

        val relationship = Relationships.select(Relationships.fromUserId.eq(fromId) and Relationships.toUserId.eq(toId)).firstOrNull()

        when  {
            relationship != null -> false
            else -> {
                Relationships.insert {
                    it[fromUserId] = fromId
                    it[toUserId] = toId
                    it[isConfirmed] = true
                }
                Relationships.insert {
                    it[fromUserId] = toId
                    it[toUserId] = fromId
                    it[isConfirmed] = false
                }
                true
            }
        }


    }

    override fun confirmRelationship(fromUserId: String, toUserId: String) = transaction(db) {
        Relationships.update  ({ Relationships.fromUserId.eq(fromUserId) and Relationships.toUserId.eq(toUserId) }){
            it[isConfirmed] = true;
        }

        Relationships.select { Relationships.fromUserId.eq(fromUserId) and Relationships.toUserId.eq(toUserId) and Relationships.isConfirmed.eq(true) }.singleOrNull() != null
    }

    override fun friendList(userId: String) = transaction(db) {
        val list: MutableList<User> = mutableListOf()

        Relationships.select { Relationships.fromUserId.eq(userId) and Relationships.isConfirmed.eq(true)}.forEach{
            val friendId = it[Relationships.toUserId]

            Relationships.select { Relationships.fromUserId.eq(friendId) }.mapNotNull { it1 ->
                if ( it1[Relationships.isConfirmed] ){
                    user(friendId, null)?.let { it2 -> list.add(it2) }
                }
            }
        }
        list
    }

    override fun friendRequestWaitingList(userId: String) = transaction(db) {
        val list: MutableList<User> = mutableListOf()

        Relationships.select { Relationships.fromUserId.eq(userId) and Relationships.isConfirmed.eq(false)}.forEach{
            val friendId = it[Relationships.toUserId]

            Relationships.select { Relationships.fromUserId.eq(friendId) }.mapNotNull { it1 ->
                if ( it1[Relationships.isConfirmed] ){
                    user(friendId, null)?.let { it2 -> list.add(it2) }
                }
            }
        }
        list
    }

    override fun friendRequestSentList(userId: String) = transaction(db) {
        val list: MutableList<User> = mutableListOf()

        Relationships.select { Relationships.fromUserId.eq(userId) and Relationships.isConfirmed.eq(true)}.forEach{
            val friendId = it[Relationships.toUserId]

            Relationships.select { Relationships.fromUserId.eq(friendId) }.mapNotNull { it1 ->
                if ( !it1[Relationships.isConfirmed] ){
                    user(friendId, null)?.let { it2 -> list.add(it2) }
                }
            }
        }
        list
    }

    override fun init() = transaction(db){
        SchemaUtils.create(Users)
        SchemaUtils.drop(Relationships)
        SchemaUtils.create(Relationships)
    }

    override fun user(userId: String, hash: String?) = transaction(db) {
        Users.select { Users.id.eq(userId) }
            .mapNotNull {
                if (hash == null || it[Users.passwordHash] == hash) {
                    User(userId, it[Users.email], it[Users.displayName], it[Users.passwordHash])
                } else {
                    null
                }
            }
            .singleOrNull()
    }

    override fun userByEmail(email: String) = transaction(db) {
        Users.select { Users.email.eq(email) }
            .map { User(it[Users.id], email, it[Users.displayName], it[Users.passwordHash]) }.singleOrNull()
    }

    override fun createUser(user: User) = transaction(db) {
        Users.insert {
            it[id] = user.userId
            it[displayName] = user.displayName
            it[email] = user.email
            it[passwordHash] = user.passwordHash
        }
        Unit
    }

    override fun close() {
    }


}