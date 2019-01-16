package com.ringoid.domain.model.essence.action

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.actions.ActionObject

data class CommitActionsEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_ACTIONS) val actions: Collection<ActionObject>) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_ACTIONS = "actions"
    }
}
