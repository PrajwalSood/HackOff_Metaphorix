package com.shivam.emotions.tfmodels.valence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValenceNetConfig {

    public static String MODEL_FILENAME = "model_valence.tflite";

    public static final int INPUT_IMG_SIZE_WIDTH = 48;
    public static final int INPUT_IMG_SIZE_HEIGHT = 48;
    public static final int FLOAT_TYPE_SIZE = 4;
    public static final int PIXEL_SIZE = 1;
    public static final int MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE;

    public static final List<Integer> OUTPUT_LABELS = Collections.unmodifiableList(
            Arrays.asList(0, 1));

    public static final int MAX_CLASSIFICATION_RESULTS = 3;
    public static final float CLASSIFICATION_THRESHOLD = 0.5f;

}
