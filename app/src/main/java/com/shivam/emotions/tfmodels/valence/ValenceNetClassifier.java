package com.shivam.emotions.tfmodels.valence;

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

public class ValenceNetClassifier {
    private Interpreter interpreter;

    private ValenceNetClassifier(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public static ValenceNetClassifier classifier(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath);
        Interpreter interpreter = new Interpreter(byteBuffer);
        return new ValenceNetClassifier(interpreter);
    }

    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<ClassificationValence> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        float[][] result = new float[1][ValenceNetConfig.OUTPUT_LABELS.size()];
        interpreter.run(byteBuffer, result);
        return getSortedResult(result);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(ValenceNetConfig.MODEL_INPUT_SIZE);

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[ValenceNetConfig.INPUT_IMG_SIZE_WIDTH * ValenceNetConfig.INPUT_IMG_SIZE_HEIGHT];
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

    private List<ClassificationValence> getSortedResult(float[][] resultsArray) {
        PriorityQueue<ClassificationValence> sortedResults = new PriorityQueue<>(
                ValenceNetConfig.MAX_CLASSIFICATION_RESULTS,
                (lhs, rhs) -> Float.compare(rhs.confidence, lhs.confidence)
        );

        for (int i = 0; i < ValenceNetConfig.OUTPUT_LABELS.size(); ++i) {
            float confidence = resultsArray[0][i];
            if (confidence >= ValenceNetConfig.CLASSIFICATION_THRESHOLD) {
                ValenceNetConfig.OUTPUT_LABELS.size();
                sortedResults.add(new ClassificationValence(ValenceNetConfig.OUTPUT_LABELS.get(i), confidence));
            }
        }

        return new ArrayList<>(sortedResults);
    }

    public void close(){
        interpreter.close();
        interpreter=null;
    }

}
