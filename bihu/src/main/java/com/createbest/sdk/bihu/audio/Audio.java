package com.createbest.sdk.bihu.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Audio {
    private static Audio INSTANCE = new Audio();
    private SoundPool soundPool;
    private Map<String, AudioInfo> soundBufferMap = new HashMap<>();

    private Audio() {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
    }

    public static void play(Context context, String audioPath) {
        Audio.INSTANCE.playAudio(context, audioPath);
    }

    private void playAudio(Context context, String audioPath) {
        try {
            File file = new File(audioPath);
            if (file.exists()) {
                String fileCanonicalPath = file.getCanonicalPath();
                if (soundBufferMap.containsKey(fileCanonicalPath)) {
                    AudioInfo audioInfo = soundBufferMap.get(fileCanonicalPath);
                    if (file.lastModified() == audioInfo.lastModified) {
                        soundPool.play(audioInfo.soundId, 1, 1, 1, 0, 1);
                        return;
                    } else {
                        soundPool.unload(audioInfo.soundId);
                    }
                }
                soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                    if (status == 0) {
                        AudioInfo audioInfo2 = new AudioInfo();
                        audioInfo2.soundId = sampleId;
                        audioInfo2.lastModified = file.lastModified();
                        soundBufferMap.put(fileCanonicalPath, audioInfo2);
                        soundPool.play(audioInfo2.soundId, 1, 1, 1, 0, 1);
                    }
                });
                soundPool.load(fileCanonicalPath, 1);
            } else {
                AssetFileDescriptor fd = context.getAssets().openFd(audioPath);
                if (fd != null) {
                    if (soundBufferMap.containsKey(audioPath)) {
                        AudioInfo audioInfo = soundBufferMap.get(audioPath);
                        soundPool.play(audioInfo.soundId, 1, 1, 1, 0, 1);
                    } else {
                        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                            if (status == 0) {
                                AudioInfo audioInfo = new AudioInfo();
                                audioInfo.soundId = sampleId;
                                soundBufferMap.put(audioPath, audioInfo);
                                soundPool.play(audioInfo.soundId, 1, 1, 1, 0, 1);
                            }
                        });
                        soundPool.load(fd, 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AudioInfo {
        public long lastModified;
        public int soundId;
    }
}
