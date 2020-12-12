package com.shivam.emotions.tfmodels.arousal;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class ArousalNetClassifier {
    private Interpreter interpreter;

    private ArousalNetClassifier(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public static ArousalNetClassifier classifier(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath);
        Interpreter interpreter = new Interpreter(byteBuffer);
        return new ArousalNetClassifier(interpreter);
    }

    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<ClassificationArousal> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        float[][] result = new float[1][ArousalNetConfig.OUTPUT_LABELS.size()];
        interpreter.run(byteBuffer, result);
        return getSortedResult(result);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(ArousalNetConfig.MODEL_INPUT_SIZE);

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[ArousalNetConfig.INPUT_IMG_SIZE_WIDTH * ArousalNetConfig.INPUT_IMG_SIZE_HEIGHT];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixel : pixels) {
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            float pixelValue = (rChannel + gChannel + bChannel) / 3 / 255.f;
            byteBuffer.putFloat(pixelValue);
        }
        return byteBuffer;
    }

    private List<ClassificationArousal> getSortedResult(float[][] resultsArray) {
        PriorityQueue<ClassificationArousal> sortedResults = new PriorityQueue<>(
                ArousalNetConfig.MAX_CLASSIFICATION_RESULTS,
                (lhs, rhs) -> Float.compare(rhs.confidence, lhs.confidence)
        );

        for (int i = 0; i < ArousalNetConfig.OUTPUT_LABELS.size(); ++i) {
            float confidence = resultsArray[0][i];
            if (confidence >= ArousalNetConfig.CLASSIFICATION_THRESHOLD) {
                ArousalNetConfig.OUTPUT_LABELS.size();
                sortedResults.add(new ClassificationArousal(ArousalNetConfig.OUTPUT_LABELS.get(i), confidence));
            }
        }

        return new ArrayList<>(sortedResults);
    }

    public void close(){
        interpreter.close();
        interpreter=null;
    }

}
