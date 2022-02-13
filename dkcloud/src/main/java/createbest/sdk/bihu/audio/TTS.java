package createbest.sdk.bihu.audio;

import android.content.Context;
import android.speech.tts.TextToSpeech;

public class TTS {
    private static final TTS INSTANCE = new TTS();
    private TextToSpeech textToSpeak;

    private TTS() {
    }

    /**
     *播报
     * @param context
     * @param text 文本内容
     */
    public static void speak(Context context, String text) {
        INSTANCE.speakNow(context, text, 1.0f, 1.0f);
    }

    /**
     * 播报
     * @param context
     * @param text 文本内容
     * @param rate 语速 1.0为正常
     * @param pitch 语调 1.0为正常
     */
    public static void speak(Context context, String text, float rate, float pitch) {
        INSTANCE.speakNow(context, text, rate, pitch);
    }

    private void speakNow(Context context, String text, float rate, float pitch) {
        if (textToSpeak == null) {
            textToSpeak = new TextToSpeech(context, status -> {
                if (status == 0) {
                    speakNow(context, text, rate, pitch);
                } else {
                    textToSpeak = null;
                }
            });
        } else {
            textToSpeak.setSpeechRate(rate);
            textToSpeak.setPitch(pitch);
            textToSpeak.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts");
        }
    }
}
