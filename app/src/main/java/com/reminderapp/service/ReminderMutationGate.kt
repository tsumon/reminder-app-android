package com.reminderapp.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Orders persisted state, generation accounting and post-commit effects across actions, edits and sync. */
object ReminderMutationGate {
    private val mutex=Mutex()
    suspend fun <T> serialized(block: suspend () -> T): T = mutex.withLock { block() }
}
