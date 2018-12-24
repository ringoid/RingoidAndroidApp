package com.ringoid.domain.model.essence.action

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.actions.ActionObject

data class CommitActionsEssence(
    @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @SerializedName(COLUMN_ACTIONS) val actions: List<ActionObject>) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_ACTIONS = "actions"
    }
}
