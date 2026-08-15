package com.reminderapp.service;

/**
 * 帧分析器：zxing 解码 QR
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\rH\u0016R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/reminderapp/service/QrAnalyzer;", "Landroidx/camera/core/ImageAnalysis$Analyzer;", "onQr", "Lkotlin/Function1;", "", "", "(Lkotlin/jvm/functions/Function1;)V", "lastScanAt", "", "reader", "Lcom/google/zxing/qrcode/QRCodeReader;", "analyze", "image", "Landroidx/camera/core/ImageProxy;", "app_debug"})
final class QrAnalyzer implements androidx.camera.core.ImageAnalysis.Analyzer {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onQr = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.zxing.qrcode.QRCodeReader reader = null;
    private long lastScanAt = 0L;
    
    public QrAnalyzer(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQr) {
        super();
    }
    
    @java.lang.Override()
    @kotlin.OptIn(markerClass = {androidx.camera.core.ExperimentalGetImage.class})
    public void analyze(@org.jetbrains.annotations.NotNull()
    androidx.camera.core.ImageProxy image) {
    }
}