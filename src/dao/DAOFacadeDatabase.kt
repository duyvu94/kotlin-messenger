package com.duyvu.dao

import com.duyvu.model.Message
import com.duyvu.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
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

    fun sendMessage(fromUserId: String, toUserId: String, message: String, date: String): Boolean

    fun messageList(fromUserId: String, toUserId: String): List<Message>

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

            Relationships.select { Relationships.fromUserId.eq(friendId) and Relationships.toUserId.eq(userId)}.mapNotNull { it1 ->
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

            Relationships.select { Relationships.fromUserId.eq(friendId) and Relationships.toUserId.eq(userId)}.mapNotNull { it1 ->
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

            Relationships.select { Relationships.fromUserId.eq(friendId) and Relationships.toUserId.eq(userId)}.mapNotNull { it1 ->
                if ( !it1[Relationships.isConfirmed] ){
                    user(friendId, null)?.let { it2 -> list.add(it2) }
                }
            }
        }
        list
    }

    override fun init() = transaction(db){
        /*
        SchemaUtils.drop(Users)
        SchemaUtils.drop(Relationships)
        SchemaUtils.drop(Messages) */
        SchemaUtils.create(Users)
        SchemaUtils.create(Relationships)
        SchemaUtils.create(Messages)
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

    override fun sendMessage(fromUserId: String, toUserId: String, message: String, date: String) = transaction(db) {
        val res1 = Relationships.select { Relationships.fromUserId.eq(fromUserId) and Relationships.toUserId.eq(toUserId) and Relationships.isConfirmed.eq(true) }.singleOrNull()
        val res2 = Relationships.select { Relationships.fromUserId.eq(toUserId) and Relationships.toUserId.eq(fromUserId) and Relationships.isConfirmed.eq(true) }.singleOrNull()

        when {
            res1 == null || res2 == null -> false
            else -> {
                Messages.insert {
                    it[mFromUserId] = fromUserId
                    it[mToUserId] = toUserId
                    it[content] = message
                    it[datetime] = date
                }
                true
            }
        }
    }

    override fun messageList(fromUserId: String, toUserId: String) = transaction(db) {
        val list: MutableList<Message> = mutableListOf()

        Messages.select { (Messages.mFromUserId.eq(fromUserId) and Messages.mToUserId.eq(toUserId)) or
                (Messages.mFromUserId.eq(toUserId) and Messages.mToUserId.eq(fromUserId))
        }.forEach{
            val message = Message(
                it[Messages.id],
                it[Messages.mFromUserId],
                it[Messages.mToUserId],
                it[Messages.content],
                it[Messages.datetime])
            list.add(message)
        }
        list
    }

    override fun close() {
    }


}