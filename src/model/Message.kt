package com.duyvu.model

import java.io.*

data class Message(val id: Int, val fromUserId: String, val toUserId: String, val content: String, val date: String) : Serializable