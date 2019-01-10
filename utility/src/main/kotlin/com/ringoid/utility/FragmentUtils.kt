package com.ringoid.utility

import android.app.Activity
import androidx.fragment.app.Fragment

interface ICommunicator

/**
 * Generic listener from this [Fragment] to some parent ([Fragment] or [Activity]).
 *
 * @see https://stackoverflow.com/questions/45089645/communication-between-host-fragment-and-viewpagers-fragment-item
 */
fun <T : ICommunicator> Fragment.communicator(communicatorClass: Class<T>): T? =
    communicatorClass
        .takeIf { it.isInstance(parentFragment) }
        ?.let { it.cast(parentFragment) }
        ?: run {
            communicatorClass
                .takeIf { it.isInstance(activity) }
                ?.let { it.cast(activity) }
        }
