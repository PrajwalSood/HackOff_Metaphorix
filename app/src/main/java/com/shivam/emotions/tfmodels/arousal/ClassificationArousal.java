package com.shivam.emotions.tfmodels.arousal;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

public class ClassificationArousal {
    public final Integer title;
    public final float confidence;

    public ClassificationArousal(Integer title, float confidence) {
        this.title = title;
        this.confidence = confidence;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public @NotNull String toString() {
        return title + " " + String.format("(%.1f%%) ", confidence * 100.0f);
    }
}
