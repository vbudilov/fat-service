package com.budilov.fat.services

import java.util.*

/**
 * Generated a unique uuid...I don't check if it's unique though so that's a next step
 */
fun getRandomUUID(replaceDashes: Boolean = true): String {
    val uuid = UUID.randomUUID()

    return if (replaceDashes)
        UUID.randomUUID().toString().replace("-", "")
    else
        return uuid.toString()
}
