package com.duyvu.model

import org.joda.time.DateTime
import java.io.*
import java.util.*

data class Message(val id: Int, val fromUserId: String, val toUserId: String, val content: String, val date: String) : Serializable