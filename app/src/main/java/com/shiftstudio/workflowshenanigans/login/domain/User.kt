package com.shiftstudio.workflowshenanigans.login.domain

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.parse
import com.squareup.workflow1.readUtf8WithLength
import com.squareup.workflow1.writeUtf8WithLength
import okio.ByteString

data class User(
    val uid: String,
    val name: String = "",
    val email: String = "",
) {

    fun toSnapshot(): Snapshot = Snapshot.write { sink ->
        sink.writeUtf8WithLength(uid)
        sink.writeUtf8WithLength(name)
        sink.writeUtf8WithLength(email)
    }

    companion object {
        fun fromSnapshot(byteString: ByteString): User = byteString.parse { source ->
            User(
                uid = source.readUtf8WithLength(),
                name = source.readUtf8WithLength(),
                email = source.readUtf8WithLength(),
            )
        }
    }
}
