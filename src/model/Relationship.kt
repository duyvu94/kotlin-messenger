package com.duyvu.model

import java.io.*

data class Relationship(val relationshipId: String, val fromUserId: String, val toUserId: String, val isConfirmed: Boolean) : Serializable