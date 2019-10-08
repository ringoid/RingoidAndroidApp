package com.ringoid.domain.model.essence.action

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.actions.OriginActionObject

data class CommitActionsEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_ACTIONS) val actions: Collection<OriginActionObject>) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_ACTIONS = "actions"
    }

    fun copyWith(actions: Collection<OriginActionObject>): CommitActionsEssence =
        CommitActionsEssence(accessToken, actions = actions)

    // ------------------------------------------
    fun toContentString(): List<Pair<String, String>> {
        fun put(map: MutableMap<String, Int>, key: String) {
            if (!map.containsKey(key)) {
                map[key] = 0  // create entry by key and initial value
            }
            map[key]?.plus(1)
        }

        val map = mutableMapOf<String, Int>()
        actions.forEach { put(map, key = it.actionType) }

        val size = actions.size
        val list = mutableListOf<Pair<String, String>>()
        map.entries.forEach { (key, count) ->
            list.add(key to "${count * 100 / size}% [$count]")
        }
        return list
    }

    override fun toDebugPayload(): String = actions.joinToString("\n\t\t", "\n\t\t", transform = { it.toDebugPayload() })
    override fun toSentryPayload(): String = actions.joinToString(", ", "[", "]", transform = { it.toSentryPayload() })
}
