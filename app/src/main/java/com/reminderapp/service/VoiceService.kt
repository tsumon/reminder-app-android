package com.reminderapp.service

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import com.reminderapp.i18n.zh
import com.reminderapp.i18n.zhf

/**
 * 语音识别服务（Android 原生 SpeechRecognizer）
 * 镜像 iOS VoiceRecognizer
 */
class VoiceService(private val context: Context) {

    /**
     * 启动语音识别，返回识别文本
     */
    suspend fun recognize(): Result<String> = suspendCancellableCoroutine { cont ->
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            cont.resume(Result.failure(Exception(zh("语音识别不可用"))))
            return@suspendCancellableCoroutine
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                recognizer.destroy()
                if (!cont.isCancelled) cont.resume(Result.success(text))
            }

            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> zh("未能识别语音，请再说一次")
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> zh("语音超时")
                    SpeechRecognizer.ERROR_NETWORK -> zh("网络不可用")
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> zh("缺少录音权限")
                    else -> zhf("语音识别错误(%s)", error)
                }
                recognizer.destroy()
                if (!cont.isCancelled) cont.resume(Result.failure(Exception(msg)))
            }

            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer.startListening(intent)

        cont.invokeOnCancellation { recognizer.destroy() }
    }
}
