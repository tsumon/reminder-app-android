package com.reminderapp.service;

/**
 * OpenAI 兼容 API 调用服务（v2.2.0：流式 + token 统计 + 备用降级）
 * 镜像 iOS AIService
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0012\u0018\u00002\u00020\u0001:\u0007&\'()*+,B\u0005\u00a2\u0006\u0002\u0010\u0002JL\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\f2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\u0002JZ\u0010\u0013\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\n2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\f2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\n2\u0016\b\u0002\u0010\u0015\u001a\u0010\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0017\u0018\u00010\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0018JJ\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u001b2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\f2\u0016\b\u0002\u0010\u0015\u001a\u0010\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0017\u0018\u00010\u0016H\u0086@\u00a2\u0006\u0002\u0010\u001cJB\u0010\u001d\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\f2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u001eJ2\u0010\u001f\u001a\u00020\n2\u0006\u0010\u001a\u001a\u00020\u001b2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\fH\u0086@\u00a2\u0006\u0002\u0010 J\u0010\u0010!\u001a\u00020\u00142\u0006\u0010\"\u001a\u00020\nH\u0002J`\u0010#\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\n2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\f2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u00112\u0014\u0010\u0015\u001a\u0010\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0017\u0018\u00010\u0016H\u0082@\u00a2\u0006\u0002\u0010$J^\u0010%\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\n2\u001a\u0010\u000b\u001a\u0016\u0012\u0012\u0012\u0010\u0012\u0004\u0012\u00020\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\r0\f2\u0006\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u00112\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00170\u0016H\u0082@\u00a2\u0006\u0002\u0010$R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/reminderapp/service/AIService;", "", "()V", "client", "Lokhttp3/OkHttpClient;", "gson", "Lcom/google/gson/Gson;", "buildRequest", "Lokhttp3/Request;", "model", "", "messages", "", "", "endpoint", "apiKey", "useTools", "", "stream", "chat", "Lcom/reminderapp/service/AIService$ChatResult;", "onStream", "Lkotlin/Function1;", "", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "chatWithFallback", "settings", "Lcom/reminderapp/service/AISettings;", "(Lcom/reminderapp/service/AISettings;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "complete", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "completeWithFallback", "(Lcom/reminderapp/service/AISettings;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseResponse", "responseBody", "send", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "streamSend", "ChatMessage", "ChatResponse", "ChatResult", "Choice", "FunctionCall", "ToolCall", "Usage", "app_debug"})
public final class AIService {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    
    public AIService() {
        super();
    }
    
    /**
     * 聊天（带工具）：主配置失败且配置了备用时自动降级重试一次。
     * stream 模式下只对纯文本回复做增量输出（工具调用轮次保持非流式，避免 tool_calls 分片累积的复杂性）。
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object chatWithFallback(@org.jetbrains.annotations.NotNull()
    com.reminderapp.service.AISettings settings, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStream, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.service.AIService.ChatResult> $completion) {
        return null;
    }
    
    /**
     * 纯文本补全（不带 tools）——用于周报 AI 解读；同样支持备用降级
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object completeWithFallback(@org.jetbrains.annotations.NotNull()
    com.reminderapp.service.AISettings settings, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object chat(@org.jetbrains.annotations.NotNull()
    java.lang.String model, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, @org.jetbrains.annotations.NotNull()
    java.lang.String endpoint, @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStream, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.reminderapp.service.AIService.ChatResult> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object complete(@org.jetbrains.annotations.NotNull()
    java.lang.String model, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, @org.jetbrains.annotations.NotNull()
    java.lang.String endpoint, @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object send(java.lang.String model, java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, java.lang.String endpoint, java.lang.String apiKey, boolean useTools, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStream, kotlin.coroutines.Continuation<? super com.reminderapp.service.AIService.ChatResult> $completion) {
        return null;
    }
    
    private final java.lang.Object streamSend(java.lang.String model, java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, java.lang.String endpoint, java.lang.String apiKey, boolean useTools, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onStream, kotlin.coroutines.Continuation<? super com.reminderapp.service.AIService.ChatResult> $completion) {
        return null;
    }
    
    private final okhttp3.Request buildRequest(java.lang.String model, java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> messages, java.lang.String endpoint, java.lang.String apiKey, boolean useTools, boolean stream) {
        return null;
    }
    
    private final com.reminderapp.service.AIService.ChatResult parseResponse(java.lang.String responseBody) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0010\b\u0002\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u0012\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010\u0013\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J=\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000bR\u0019\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001b"}, d2 = {"Lcom/reminderapp/service/AIService$ChatMessage;", "", "role", "", "content", "tool_calls", "", "Lcom/reminderapp/service/AIService$ToolCall;", "tool_call_id", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)V", "getContent", "()Ljava/lang/String;", "getRole", "getTool_call_id", "getTool_calls", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class ChatMessage {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String role = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String content = null;
        @org.jetbrains.annotations.Nullable()
        private final java.util.List<com.reminderapp.service.AIService.ToolCall> tool_calls = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String tool_call_id = null;
        
        public ChatMessage(@org.jetbrains.annotations.NotNull()
        java.lang.String role, @org.jetbrains.annotations.Nullable()
        java.lang.String content, @org.jetbrains.annotations.Nullable()
        java.util.List<com.reminderapp.service.AIService.ToolCall> tool_calls, @org.jetbrains.annotations.Nullable()
        java.lang.String tool_call_id) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getRole() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getContent() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<com.reminderapp.service.AIService.ToolCall> getTool_calls() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getTool_call_id() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<com.reminderapp.service.AIService.ToolCall> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.ChatMessage copy(@org.jetbrains.annotations.NotNull()
        java.lang.String role, @org.jetbrains.annotations.Nullable()
        java.lang.String content, @org.jetbrains.annotations.Nullable()
        java.util.List<com.reminderapp.service.AIService.ToolCall> tool_calls, @org.jetbrains.annotations.Nullable()
        java.lang.String tool_call_id) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u001f\u0012\u000e\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007J\u0011\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\r\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\'\u0010\u000e\u001a\u00020\u00002\u0010\b\u0002\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001R\u0019\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0016"}, d2 = {"Lcom/reminderapp/service/AIService$ChatResponse;", "", "choices", "", "Lcom/reminderapp/service/AIService$Choice;", "usage", "Lcom/reminderapp/service/AIService$Usage;", "(Ljava/util/List;Lcom/reminderapp/service/AIService$Usage;)V", "getChoices", "()Ljava/util/List;", "getUsage", "()Lcom/reminderapp/service/AIService$Usage;", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    public static final class ChatResponse {
        @org.jetbrains.annotations.Nullable()
        private final java.util.List<com.reminderapp.service.AIService.Choice> choices = null;
        @org.jetbrains.annotations.Nullable()
        private final com.reminderapp.service.AIService.Usage usage = null;
        
        public ChatResponse(@org.jetbrains.annotations.Nullable()
        java.util.List<com.reminderapp.service.AIService.Choice> choices, @org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.Usage usage) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<com.reminderapp.service.AIService.Choice> getChoices() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.Usage getUsage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<com.reminderapp.service.AIService.Choice> component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.Usage component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.ChatResponse copy(@org.jetbrains.annotations.Nullable()
        java.util.List<com.reminderapp.service.AIService.Choice> choices, @org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.Usage usage) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    /**
     * 一次完整对话的结果（含流式累积的文本、工具调用、token 用量、实际使用的 provider）
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0013\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B;\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\u000e\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\b\u0010\t\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u000b\u0010\u0016\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0011\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0018\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\u000b\u0010\u0019\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u000bH\u00c6\u0003JI\u0010\u001b\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0010\b\u0002\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\n\u001a\u00020\u000bH\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u000b2\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u001fH\u00d6\u0001J\t\u0010 \u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0019\u0010\u0004\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006!"}, d2 = {"Lcom/reminderapp/service/AIService$ChatResult;", "", "content", "", "toolCalls", "", "Lcom/reminderapp/service/AIService$ToolCall;", "usage", "Lcom/reminderapp/service/AIService$Usage;", "finishReason", "usedFallback", "", "(Ljava/lang/String;Ljava/util/List;Lcom/reminderapp/service/AIService$Usage;Ljava/lang/String;Z)V", "getContent", "()Ljava/lang/String;", "getFinishReason", "getToolCalls", "()Ljava/util/List;", "getUsage", "()Lcom/reminderapp/service/AIService$Usage;", "getUsedFallback", "()Z", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
    public static final class ChatResult {
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String content = null;
        @org.jetbrains.annotations.Nullable()
        private final java.util.List<com.reminderapp.service.AIService.ToolCall> toolCalls = null;
        @org.jetbrains.annotations.Nullable()
        private final com.reminderapp.service.AIService.Usage usage = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String finishReason = null;
        private final boolean usedFallback = false;
        
        public ChatResult(@org.jetbrains.annotations.Nullable()
        java.lang.String content, @org.jetbrains.annotations.Nullable()
        java.util.List<com.reminderapp.service.AIService.ToolCall> toolCalls, @org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.Usage usage, @org.jetbrains.annotations.Nullable()
        java.lang.String finishReason, boolean usedFallback) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getContent() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<com.reminderapp.service.AIService.ToolCall> getToolCalls() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.Usage getUsage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getFinishReason() {
            return null;
        }
        
        public final boolean getUsedFallback() {
            return false;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.util.List<com.reminderapp.service.AIService.ToolCall> component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.Usage component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component4() {
            return null;
        }
        
        public final boolean component5() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.ChatResult copy(@org.jetbrains.annotations.Nullable()
        java.lang.String content, @org.jetbrains.annotations.Nullable()
        java.util.List<com.reminderapp.service.AIService.ToolCall> toolCalls, @org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.Usage usage, @org.jetbrains.annotations.Nullable()
        java.lang.String finishReason, boolean usedFallback) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007J\u000b\u0010\r\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u000f\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J-\u0010\u0010\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0006H\u00d6\u0001R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/reminderapp/service/AIService$Choice;", "", "message", "Lcom/reminderapp/service/AIService$ChatMessage;", "delta", "finish_reason", "", "(Lcom/reminderapp/service/AIService$ChatMessage;Lcom/reminderapp/service/AIService$ChatMessage;Ljava/lang/String;)V", "getDelta", "()Lcom/reminderapp/service/AIService$ChatMessage;", "getFinish_reason", "()Ljava/lang/String;", "getMessage", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class Choice {
        @org.jetbrains.annotations.Nullable()
        private final com.reminderapp.service.AIService.ChatMessage message = null;
        @org.jetbrains.annotations.Nullable()
        private final com.reminderapp.service.AIService.ChatMessage delta = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String finish_reason = null;
        
        public Choice(@org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.ChatMessage message, @org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.ChatMessage delta, @org.jetbrains.annotations.Nullable()
        java.lang.String finish_reason) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.ChatMessage getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.ChatMessage getDelta() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getFinish_reason() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.ChatMessage component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.reminderapp.service.AIService.ChatMessage component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.Choice copy(@org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.ChatMessage message, @org.jetbrains.annotations.Nullable()
        com.reminderapp.service.AIService.ChatMessage delta, @org.jetbrains.annotations.Nullable()
        java.lang.String finish_reason) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/reminderapp/service/AIService$FunctionCall;", "", "name", "", "arguments", "(Ljava/lang/String;Ljava/lang/String;)V", "getArguments", "()Ljava/lang/String;", "getName", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class FunctionCall {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String arguments = null;
        
        public FunctionCall(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String arguments) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getArguments() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.FunctionCall copy(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String arguments) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0006H\u00c6\u0003J\'\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000b\u00a8\u0006\u0017"}, d2 = {"Lcom/reminderapp/service/AIService$ToolCall;", "", "id", "", "type", "function", "Lcom/reminderapp/service/AIService$FunctionCall;", "(Ljava/lang/String;Ljava/lang/String;Lcom/reminderapp/service/AIService$FunctionCall;)V", "getFunction", "()Lcom/reminderapp/service/AIService$FunctionCall;", "getId", "()Ljava/lang/String;", "getType", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class ToolCall {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String id = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String type = null;
        @org.jetbrains.annotations.NotNull()
        private final com.reminderapp.service.AIService.FunctionCall function = null;
        
        public ToolCall(@org.jetbrains.annotations.NotNull()
        java.lang.String id, @org.jetbrains.annotations.NotNull()
        java.lang.String type, @org.jetbrains.annotations.NotNull()
        com.reminderapp.service.AIService.FunctionCall function) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.FunctionCall getFunction() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.FunctionCall component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.ToolCall copy(@org.jetbrains.annotations.NotNull()
        java.lang.String id, @org.jetbrains.annotations.NotNull()
        java.lang.String type, @org.jetbrains.annotations.NotNull()
        com.reminderapp.service.AIService.FunctionCall function) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B)\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\bJ\u0010\u0010\r\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\bJ\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\bJ2\u0010\u000f\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0010J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001R\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\t\u001a\u0004\b\u0007\u0010\bR\u0015\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\t\u001a\u0004\b\n\u0010\bR\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\t\u001a\u0004\b\u000b\u0010\b\u00a8\u0006\u0017"}, d2 = {"Lcom/reminderapp/service/AIService$Usage;", "", "prompt_tokens", "", "completion_tokens", "total_tokens", "(Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;)V", "getCompletion_tokens", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getPrompt_tokens", "getTotal_tokens", "component1", "component2", "component3", "copy", "(Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;)Lcom/reminderapp/service/AIService$Usage;", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
    public static final class Usage {
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer prompt_tokens = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer completion_tokens = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Integer total_tokens = null;
        
        public Usage(@org.jetbrains.annotations.Nullable()
        java.lang.Integer prompt_tokens, @org.jetbrains.annotations.Nullable()
        java.lang.Integer completion_tokens, @org.jetbrains.annotations.Nullable()
        java.lang.Integer total_tokens) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getPrompt_tokens() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getCompletion_tokens() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer getTotal_tokens() {
            return null;
        }
        
        public Usage() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Integer component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.reminderapp.service.AIService.Usage copy(@org.jetbrains.annotations.Nullable()
        java.lang.Integer prompt_tokens, @org.jetbrains.annotations.Nullable()
        java.lang.Integer completion_tokens, @org.jetbrains.annotations.Nullable()
        java.lang.Integer total_tokens) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}