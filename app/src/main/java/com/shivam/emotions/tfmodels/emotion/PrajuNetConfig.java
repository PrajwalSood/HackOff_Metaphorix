package com.shivam.emotions.tfmodels.emotion;

import org.amlohapps.expression.util.EmotionConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrajuNetConfig {

    public static String MODEL_FILENAME = "model_prajuNet.tflite";

    public static final int INPUT_IMG_SIZE_WIDTH = 48;
    public static final int INPUT_IMG_SIZE_HEIGHT = 48;
    public static final int FLOAT_TYPE_SIZE = 4;
    public static final int PIXEL_SIZE = 1;
    public static final int MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE;

    public static final List<String> OUTPUT_LABELS = Collections.unmodifiableList(
            Arrays.asList(EmotionConstants.ANGRY, EmotionConstants.CONTEMPT, EmotionConstants.DISGUSTED, EmotionConstants.FEARFUL, EmotionConstants.HAPPY, EmotionConstants.NEUTRAL, EmotionConstants.SAD, EmotionConstants.SURPRISED));

    public static final int MAX_CLASSIFICATION_RESULTS = 3;
    public static final float CLASSIFICATION_THRESHOLD = 0.4f;

}
