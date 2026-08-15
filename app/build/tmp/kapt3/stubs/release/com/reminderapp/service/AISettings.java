package com.reminderapp.service;

/**
 * 用户自定义 AI API 配置（v2.2.0：多 provider + 备用降级 + 本地模型）。
 * apiKey 以 AES/GCM（AndroidKeyStore，见 CryptoHelper）静态加密后存入 SharedPreferences，
 * 其余字段明文存 SharedPreferences。
 *
 * - 主配置：endpoint/apiKey/model（原三件套）
 * - 备用配置（可选）：fallbackEndpoint/fallbackKey/fallbackModel —— 主配置失败时自动切换
 * - 本地模型：isLocal=true 时 apiKey 可留空（如 Ollama http://localhost:11434/v1）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0014\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R$\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR$\u0010\f\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\r\u0010\t\"\u0004\b\u000e\u0010\u000bR$\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0010\u0010\t\"\u0004\b\u0011\u0010\u000bR$\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00128F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R$\u0010\u0018\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0019\u0010\t\"\u0004\b\u001a\u0010\u000bR$\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001c\u0010\t\"\u0004\b\u001d\u0010\u000bR\u0011\u0010\u001e\u001a\u00020\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010\u0015R\u0011\u0010 \u001a\u00020\u00128F\u00a2\u0006\u0006\u001a\u0004\b \u0010\u0015R$\u0010!\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u00128F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b!\u0010\u0015\"\u0004\b\"\u0010\u0017R$\u0010#\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b$\u0010\t\"\u0004\b%\u0010\u000bR\u000e\u0010&\u001a\u00020\'X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/reminderapp/service/AISettings;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "value", "", "apiEndpoint", "getApiEndpoint", "()Ljava/lang/String;", "setApiEndpoint", "(Ljava/lang/String;)V", "apiKey", "getApiKey", "setApiKey", "fallbackApiKey", "getFallbackApiKey", "setFallbackApiKey", "", "fallbackEnabled", "getFallbackEnabled", "()Z", "setFallbackEnabled", "(Z)V", "fallbackEndpoint", "getFallbackEndpoint", "setFallbackEndpoint", "fallbackModel", "getFallbackModel", "setFallbackModel", "hasFallback", "getHasFallback", "isConfigured", "isLocal", "setLocal", "model", "getModel", "setModel", "prefs", "Landroid/content/SharedPreferences;", "app_release"})
public final class AISettings {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    
    public AISettings(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getApiEndpoint() {
        return null;
    }
    
    public final void setApiEndpoint(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getApiKey() {
        return null;
    }
    
    public final void setApiKey(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getModel() {
        return null;
    }
    
    public final void setModel(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean isLocal() {
        return false;
    }
    
    public final void setLocal(boolean value) {
    }
    
    public final boolean getFallbackEnabled() {
        return false;
    }
    
    public final void setFallbackEnabled(boolean value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFallbackEndpoint() {
        return null;
    }
    
    public final void setFallbackEndpoint(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFallbackApiKey() {
        return null;
    }
    
    public final void setFallbackApiKey(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFallbackModel() {
        return null;
    }
    
    public final void setFallbackModel(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final boolean isConfigured() {
        return false;
    }
    
    public final boolean getHasFallback() {
        return false;
    }
}