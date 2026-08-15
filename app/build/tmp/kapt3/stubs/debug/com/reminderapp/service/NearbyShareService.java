package com.reminderapp.service;

/**
 * 近场分享：同一局域网内互传提醒
 *
 * 发送方：ServerSocket 监听 47823 端口，每次请求返回当前提醒的备份 JSON
 * 接收方：OkHttp GET http://<ip>:47823/reminders.json 拉取并导入
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0013\u001a\u00020\u0004J8\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00040\u00182\u0018\u0010\u0019\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u001b\u0012\u0004\u0012\u00020\u00150\u001aH\u0002J\b\u0010\u001c\u001a\u0004\u0018\u00010\u0004J0\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00040\u00182\u0018\u0010\u0019\u001a\u0014\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u001b\u0012\u0004\u0012\u00020\u00150\u001aJ\u0010\u0010\u001f\u001a\u00020\u00152\b\u0010 \u001a\u0004\u0018\u00010\u001eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\nR2\u0010\r\u001a&\u0012\f\u0012\n \u0010*\u0004\u0018\u00010\u000f0\u000f \u0010*\u0012\u0012\f\u0012\n \u0010*\u0004\u0018\u00010\u000f0\u000f\u0018\u00010\u00110\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/reminderapp/service/NearbyShareService;", "", "()V", "PATH", "", "PORT", "", "client", "Lokhttp3/OkHttpClient;", "getClient", "()Lokhttp3/OkHttpClient;", "client$delegate", "Lkotlin/Lazy;", "sockets", "", "Ljava/net/Socket;", "kotlin.jvm.PlatformType", "", "fetchFrom", "host", "handleConnection", "", "socket", "jsonProvider", "Lkotlin/Function0;", "onEvent", "Lkotlin/Function2;", "", "localIpAddress", "startServer", "Ljava/net/ServerSocket;", "stopServer", "server", "app_debug"})
public final class NearbyShareService {
    public static final int PORT = 47823;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PATH = "/reminders.json";
    
    /**
     * 已接受的连接集合（停止/销毁时统一关闭，防泄漏）
     */
    private static final java.util.List<java.net.Socket> sockets = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy client$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.NearbyShareService INSTANCE = null;
    
    private NearbyShareService() {
        super();
    }
    
    private final okhttp3.OkHttpClient getClient() {
        return null;
    }
    
    /**
     * 本机局域网 IPv4：wlan/eth 等前缀优先，其次任意非回环 IPv4（热点/USB 共享兜底）
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String localIpAddress() {
        return null;
    }
    
    /**
     * 启动本地服务；jsonProvider 每次请求时调用（返回最新 JSON）。
     * 返回 ServerSocket；返回 null 表示启动失败。
     */
    @org.jetbrains.annotations.Nullable()
    public final java.net.ServerSocket startServer(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<java.lang.String> jsonProvider, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Boolean, kotlin.Unit> onEvent) {
        return null;
    }
    
    /**
     * 关闭监听并清理所有已接受的连接
     */
    public final void stopServer(@org.jetbrains.annotations.Nullable()
    java.net.ServerSocket server) {
    }
    
    private final void handleConnection(java.net.Socket socket, kotlin.jvm.functions.Function0<java.lang.String> jsonProvider, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Boolean, kotlin.Unit> onEvent) {
    }
    
    /**
     * 从对方 IP 拉取备份 JSON；失败返回 null
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String fetchFrom(@org.jetbrains.annotations.NotNull()
    java.lang.String host) {
        return null;
    }
}