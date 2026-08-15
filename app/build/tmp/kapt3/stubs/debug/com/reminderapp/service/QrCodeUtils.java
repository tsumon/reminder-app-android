package com.reminderapp.service;

/**
 * 二维码工具：生成（zxing core）+ 扫码（CameraX 预览 + zxing 解码）
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b\u00a8\u0006\t"}, d2 = {"Lcom/reminderapp/service/QrCodeUtils;", "", "()V", "generateQrBitmap", "Landroid/graphics/Bitmap;", "text", "", "size", "", "app_debug"})
public final class QrCodeUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.reminderapp.service.QrCodeUtils INSTANCE = null;
    
    private QrCodeUtils() {
        super();
    }
    
    /**
     * 生成二维码 Bitmap（黑底白点阵，白边留白）
     */
    @org.jetbrains.annotations.NotNull()
    public final android.graphics.Bitmap generateQrBitmap(@org.jetbrains.annotations.NotNull()
    java.lang.String text, int size) {
        return null;
    }
}