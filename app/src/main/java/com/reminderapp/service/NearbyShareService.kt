package com.reminderapp.service

import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.util.Collections
import kotlin.concurrent.thread
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

    /** 已接受的连接集合（停止/销毁时统一关闭，防泄漏） */
    private val sockets = Collections.synchronizedList(mutableListOf<Socket>())

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** 本机局域网 IPv4：wlan/eth 等前缀优先，其次任意非回环 IPv4（热点/USB 共享兜底） */
    fun localIpAddress(): String? {
        var fallback: String? = null
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (ni in interfaces) {
                if (!ni.isUp || ni.isLoopback) continue
                for (addr in ni.inetAddresses) {
                    if (addr !is Inet4Address || addr.isLoopbackAddress) continue
                    val name = ni.name
                    // 跳过 VPN/隧道接口
                    if (name.startsWith("tun") || name.startsWith("ppp") || name.startsWith("rmnet")) continue
                    if (fallback == null) fallback = addr.hostAddress
                    if (name.startsWith("wlan") || name.startsWith("eth") ||
                        name.startsWith("wifi") || name.startsWith("ap") ||
                        name.startsWith("swlan") || name.startsWith("usb") ||
                        name.startsWith("rndis") || name.startsWith("bt")) {
                        return addr.hostAddress
                    }
                }
            }
            fallback
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * 启动本地服务；jsonProvider 每次请求时调用（返回最新 JSON）。
     * 返回 ServerSocket；返回 null 表示启动失败。
     */
    fun startServer(
        jsonProvider: () -> String,
        onEvent: (String, Boolean) -> Unit
    ): ServerSocket? {
        return try {
            val server = ServerSocket(PORT)
            onEvent("服务已启动，等待对方连接...", false)
            thread(isDaemon = true, name = "nearby-share-accept") {
                while (!server.isClosed) {
                    try {
                        val socket = server.accept()
                        socket.soTimeout = 30_000 // 30s 无请求自动断开，防半开连接泄漏
                        sockets.add(socket)
                        thread(isDaemon = true, name = "nearby-share-conn") {
                            try {
                                handleConnection(socket, jsonProvider, onEvent)
                            } finally {
                                sockets.remove(socket)
                            }
                        }
                    } catch (_: Exception) {
                        break
                    }
                }
            }
            server
        } catch (e: Exception) {
            onEvent("服务启动失败：${e.message}", true)
            null
        }
    }

    /** 关闭监听并清理所有已接受的连接 */
    fun stopServer(server: ServerSocket?) {
        try { server?.close() } catch (_: Exception) {}
        synchronized(sockets) {
            sockets.forEach { s -> try { s.close() } catch (_: Exception) {} }
            sockets.clear()
        }
    }

    private fun handleConnection(socket: Socket, jsonProvider: () -> String, onEvent: (String, Boolean) -> Unit) {
        try {
            socket.use { s ->
                // 读取请求头（忽略内容，等对方把请求发过来；soTimeout 兜底）
                val input = s.getInputStream()
                val buf = ByteArray(2048)
                var read = 0
                while (read < buf.size) {
                    val n = input.read(buf, read, buf.size - read)
                    if (n <= 0) break
                    read += n
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
                onEvent("已发送给一台设备（${bodyBytes.size} 字节）", false)
            }
        } catch (_: Exception) {
            // 连接异常/超时忽略
        }
    }

    /** 从对方 IP 拉取备份 JSON；失败返回 null */
    fun fetchFrom(host: String): String? {
        var h = host.trim()
        // 完整 URL：取 host（兼容 http://ip:port / http://[ipv6]:port）
        if (h.startsWith("http://") || h.startsWith("https://")) {
            h = try { URL(h).host ?: h } catch (_: Exception) { h }
        } else if (h.contains(":") && !h.contains("/")) {
            // 裸 ip:port → 只取 IP 段
            h = h.substringBefore(":")
        }
        // IPv6 需要方括号
        if (h.contains(":") && !h.startsWith("[")) {
            h = "[$h]"
        }
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
