package com.shivam.emotions.tfmodels.emotion;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

public class ClassificationEmotion {
    public final String title;
    public final float confidence;

    public ClassificationEmotion(String title, float confidence) {
        this.title = title;
        this.confidence = confidence;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public @NotNull String toString() {
        return title + " " + String.format("(%.1f%%) ", confidence * 100.0f);
    }
}
