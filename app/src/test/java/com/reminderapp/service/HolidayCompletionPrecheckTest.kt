package com.reminderapp.service

import android.content.ContextWrapper
import android.content.SharedPreferences
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.LocalDateTime
import java.time.ZoneId
import com.reminderapp.data.entity.ReminderEntity

class HolidayCompletionPrecheckTest {
    private fun time(value: String)=LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val now=time("2026-09-24T09:00")
    private val next=ReminderEntity(title="Weekly",cycle="weekly",firstTriggerAt=now,nextTriggerAt=time("2026-10-01T09:00"))
    private fun context(cache: MutableMap<String,String>) = object: ContextWrapper(null) {
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,arrayOf(SharedPreferences::class.java)) { _, method, args ->
            when(method.name) { "getAll" -> cache; "getString" -> cache[args!![0]] ?: args[1]; else -> null }
        } as SharedPreferences
    }
    @Test fun normalDayInCachedYearRequiresNoNetworkRefresh() = runBlocking {
        var refreshes=0
        val c=context(mutableMapOf("2026-01-01" to "true|New Year"))
        assertEquals(next,HolidayPreCheck.adjusted(c,next,now) { refreshes++ })
        assertEquals(0,refreshes)
    }
    @Test fun cachedHolidayKeepsFixedAnchorAndAnnotation() = runBlocking {
        val c=context(mutableMapOf("2026-10-01" to "true|National Day","2026-09-30" to "false|Workday"))
        val adjusted=HolidayPreCheck.adjusted(c,next,now) { fail("Cached holiday must complete offline") }
        assertEquals(time("2026-09-30T09:00"),adjusted.nextTriggerAt)
        assertEquals(next.firstTriggerAt,adjusted.firstTriggerAt)
        assertTrue(adjusted.holidayAdjustNote!!.contains("National Day"))
    }
    @Test fun unavailableOrDelayedRefreshFallsBackWithinActionBudgetAndCancelsWork() = runBlocking {
        val c=context(mutableMapOf())
        assertEquals(next,HolidayPreCheck.adjusted(c,next,now) { throw java.io.IOException("offline") })
        var finished=false;var cancelled=false
        val start=System.nanoTime()
        val adjusted=HolidayPreCheck.adjusted(c,next,now) {
            try { delay(5000);finished=true } finally { cancelled=true }
        }
        assertEquals(next,adjusted)
        assertTrue("Optional refresh must not occupy a receiver for network timeouts",(System.nanoTime()-start)/1_000_000 < 3000)
        assertTrue(cancelled);assertFalse(finished)
    }
    @Test fun callerCancellationStillCancelsCompletion() = runBlocking {
        val entered=CompletableDeferred<Unit>()
        val job=launch { HolidayPreCheck.adjusted(context(mutableMapOf()),next,now) { entered.complete(Unit);awaitCancellation() } }
        entered.await();job.cancelAndJoin();assertTrue(job.isCancelled)
    }
    @Test fun cancellingRealHttpRefreshCancelsTheOutstandingOkHttpCall() = runBlocking {
        val accepted=CompletableDeferred<Unit>()
        val release=java.util.concurrent.CountDownLatch(1)
        val cancelled=java.util.concurrent.atomic.AtomicBoolean(false)
        java.net.ServerSocket(0,1,java.net.InetAddress.getLoopbackAddress()).use { server ->
            val thread=Thread {
                server.accept().use { accepted.complete(Unit);release.await(5,java.util.concurrent.TimeUnit.SECONDS) }
            }.apply { isDaemon=true;start() }
            val client=okhttp3.OkHttpClient.Builder().proxy(java.net.Proxy.NO_PROXY)
                .eventListener(object: okhttp3.EventListener() {
                    override fun canceled(call: okhttp3.Call) { cancelled.set(true) }
                }).build()
            try {
                val request=okhttp3.Request.Builder().url("http://127.0.0.1:${server.localPort}/2026.json").build()
                val fetch=launch { HolidayRemoteService.download(client,request) }
                withTimeout(2000) { accepted.await() }
                val start=System.nanoTime();fetch.cancelAndJoin()
                assertTrue(cancelled.get())
                assertTrue((System.nanoTime()-start)/1_000_000 < 1000)
            } finally {
                release.countDown();thread.join(2000)
                client.dispatcher.executorService.shutdown();client.connectionPool.evictAll()
            }
        }
    }
}
