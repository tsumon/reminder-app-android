package com.reminderapp.service

import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 近场分享：同一局域网内互传提醒
 *
 * 发送方：ServerSocket 监听 47823 端口，每次请求返回当前提醒的备份 JSON
 * 接收方：OkHttp GET http://<ip>:47823/reminders.json 拉取并导入
 */
object NearbyShareService {
    const val PORT = 47823
    const val PATH = "/reminders.json"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** 本机局域网 IPv4（wlan/eth 接口） */
    fun localIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (ni in interfaces) {
                if (!ni.isUp || ni.isLoopback) continue
                if (!ni.name.startsWith("wlan") && !ni.name.startsWith("eth") &&
                    !ni.name.startsWith("wifi") && !ni.name.startsWith("ap")) continue
                for (addr in ni.inetAddresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 启动本地服务；jsonProvider 每次请求时调用（保证发送最新数据）。
     * 返回 ServerSocket；返回 null 表示启动失败。
     */
    fun startServer(
        jsonProvider: () -> String,
        onEvent: (String) -> Unit
    ): ServerSocket? {
        return try {
            val server = ServerSocket(PORT)
            onEvent("服务已启动，等待对方连接...")
            Thread {
                while (!server.isClosed) {
                    try {
                        val socket = server.accept()
                        Thread {
                            handleConnection(socket, jsonProvider, onEvent)
                        }.start()
                    } catch (_: Exception) {
                        break
                    }
                }
            }.apply {
                isDaemon = true
                start()
            }
            server
        } catch (e: Exception) {
            onEvent("服务启动失败：${e.message}")
            null
        }
    }

    private fun handleConnection(socket: Socket, jsonProvider: () -> String, onEvent: (String) -> Unit) {
        try {
            socket.use { s ->
                // 读取请求头（忽略内容，等对方把请求发过来）
                val input = s.getInputStream()
                val buf = ByteArray(2048)
                var read = 0
                while (read < buf.size) {
                    val n = input.read(buf, read, buf.size - read)
                    if (n <= 0) break
                    read += n
                    // 请求头以 \r\n\r\n 结束
                    if (String(buf, 0, read).contains("\r\n\r\n")) break
                }

                val body = jsonProvider()
                val bodyBytes = body.toByteArray(Charsets.UTF_8)
                val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json; charset=utf-8\r\n" +
                    "Content-Length: ${bodyBytes.size}\r\n" +
                    "Connection: close\r\n\r\n"
                s.getOutputStream().write(header.toByteArray(Charsets.UTF_8))
                s.getOutputStream().write(bodyBytes)
                s.getOutputStream().flush()
                onEvent("已发送给一台设备（${bodyBytes.size} 字节）")
            }
        } catch (_: Exception) {
            // 连接异常忽略
        }
    }

    /** 从对方 IP 拉取备份 JSON；失败返回 null */
    fun fetchFrom(host: String): String? {
        var h = host.trim()
        // 容忍用户粘贴完整 http://ip:port 或裸 IP
        if (h.startsWith("http://") || h.startsWith("https://")) {
            h = try { URL(h).host ?: h } catch (_: Exception) { h }
        }
        if (h.contains(":")) h = h.substringBefore(":")
        return try {
            val url = "http://$h:$PORT$PATH"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) null else resp.body?.string()
            }
        } catch (_: Exception) {
            null
        }
    }
}
