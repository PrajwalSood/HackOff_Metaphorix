package com.shivam.emotions.tfmodels.valence;

import android.annotation.SuppressLint;

import org.jetbrains.annotations.NotNull;

public class ClassificationValence {
    public final Integer title;
    public final float confidence;

    public ClassificationValence(Integer title, float confidence) {
        this.title = title;
        this.confidence = confidence;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public @NotNull String toString() {
        return title + " " + String.format("(%.1f%%) ", confidence * 100.0f);
    }
}
