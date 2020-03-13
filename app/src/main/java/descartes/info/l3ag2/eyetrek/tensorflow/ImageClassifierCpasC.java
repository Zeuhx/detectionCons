/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package descartes.info.l3ag2.eyetrek.tensorflow;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** Classifies images with Tensorflow Lite. */
public class ImageClassifierCpasC {

    /** Tag for the {@link Log}. */
    private static final String TAG = "TfLiteCameraDemo";

    /** Name of the model file stored in Assets. */
    private static final String MODEL_PATH = "model_29_champautre_95p_NASNM.tflite";

    /** Name of the label file stored in Assets. */
    private static final String LABEL_PATH = "labels_champignon_autre.txt";

    /** Boolean qui dit si la feuille est valide ou non */
    private boolean isFeuilleValide = false;

    /** Number of results to show in the UI. */
    private static final int RESULTS_TO_SHOW = 1;

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

    public static final int DIM_IMG_SIZE_X = 224;
    public static final int DIM_IMG_SIZE_Y = 224;

    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;


    /* Preallocated buffers for storing image data in. */
    private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    private Interpreter tflite;

    /** Labels corresponding to the output of the vision model. */
    private List<String> labelList;

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
    private ByteBuffer imgData = null;

    /** An array to hold inference results, to be feed into Tensorflow Lite as outputs. */
    private float[][] labelProbArray = null;
    /** multi-stage low pass filter **/
    private float[][] filterLabelProbArray = null;
    private static final int FILTER_STAGES = 3;
    private static final float FILTER_FACTOR = 0.4f;

    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o2.getValue()).compareTo(o1.getValue());
                        }
                    });

    /** Initializes an {@code ImageClassifier}. */
    public ImageClassifierCpasC(Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity));
        labelList = loadLabelList(activity);
        imgData =
                ByteBuffer.allocateDirect(
                        4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
        labelProbArray = new float[1][labelList.size()];
        filterLabelProbArray = new float[FILTER_STAGES][labelList.size()];
        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
    }

    /** Classifies a frame from the preview stream. */
    public String classifyFrame(Bitmap bitmap) {
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
            return "Uninitialized Classifier.";
        }
        convertBitmapToByteBuffer(bitmap);
        // Here's where the magic happens!!!
        long startTime = SystemClock.uptimeMillis();
        tflite.run(imgData, labelProbArray);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));

        // smooth the results
        //applyFilter();

        // print the results
        String textToShow = printTopKLabels();
        //textToShow = Long.toString(endTime - startTime) + "ms" + textToShow;
        return textToShow;
    }

    public boolean getIsFeuilleValide(){
        return isFeuilleValide;
    }

    /** Prints top-K labels, to be shown in UI as the results. */
    private String printTopKLabels() {
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }


        StringBuffer texte = new StringBuffer();
        for (int i = 0; i < labelProbArray[0].length; ++i) {
            texte.append(" | " + labelProbArray[0][i] + " | ");
        }
        Log.e("setTopKLabels()", "toutes les probas : " + texte.toString());

        texte = new StringBuffer();
        for (int i = 0; i < labelList.size(); ++i) {
            texte.append(" | " + labelList.get(i) + " | ");
        }
        Log.e("setTopKLabels()", "tous les labels : " + texte.toString());


        int position_resultat = 0;
        float temp_resultat = 0;
        for (int i = 0; i < labelProbArray[0].length; i++) {
            if(labelProbArray[0][i] > temp_resultat){
                position_resultat = i;
                temp_resultat = labelProbArray[0][i];
            }
        }

        Log.e("setTopKLabels()", "position_resultat : " + position_resultat);

        String textToShow = "";
        isFeuilleValide = false;

        switch(position_resultat){
            case 0:
                textToShow = "Je ne vois aucun champignon";
                break;
            case 1:
                textToShow = "C'est bon allez y !";
                isFeuilleValide = true;
                break;
            case 2:
                textToShow = "Je ne vois aucun champignon";
                break;
            case 3:
                textToShow = "j'ai du mal à distinguer un champignon";
                break;
            case 4:
                textToShow = "Wouahou quelle belle foret !";
                break;
            case 5:
                textToShow = "Je vois juste de l'herbe";
                break;
            default:
                textToShow = "Erreur !";
        }

    /*
    final int size = sortedLabels.size();
    for (int i = 0; i < size; ++i) {
      Map.Entry<String, Float> label = sortedLabels.poll();
      isFeuilleValide = false;
      switch(label.getKey()){
        case "autre":
          textToShow = "Je ne vois aucune feuille";
          break;
        case "feuille_valide":
          textToShow = "C'est bon allez y !";
          isFeuilleValide = true;
          break;
        case "feuilles_sol_foret":
          textToShow = "j'ai du mal à distinguer une feuille";
          break;
        case "foret":
          textToShow = "Wouahou quelle belle foret !";
          break;
        case "herbe":
          textToShow = "Je vois juste de l'herbe";
          break;
        default:
          textToShow = "Erreur !";
      }
      //textToShow = String.format("\n%s: %4.2f",label.getKey(),label.getValue()) + textToShow;
    }
    */
        return textToShow;
    }
/*

 >>> Servait à biaiser les résultats en faisant une moyenne des différentes analyses au cours du temps pour donner l'impression que l'algo marchais mieux

  void applyFilter(){
    int num_labels =  labelList.size();

    // Low pass filter `labelProbArray` into the first stage of the filter.
    for(int j=0; j<num_labels; ++j){
      filterLabelProbArray[0][j] += FILTER_FACTOR*(labelProbArray[0][j] -
                                                   filterLabelProbArray[0][j]);
    }
    // Low pass filter each stage into the next.
    for (int i=1; i<FILTER_STAGES; ++i){
      for(int j=0; j<num_labels; ++j){
        filterLabelProbArray[i][j] += FILTER_FACTOR*(
                filterLabelProbArray[i-1][j] -
                filterLabelProbArray[i][j]);

      }
    }

    // Copy the last stage filter output back to `labelProbArray`.
    for(int j=0; j<num_labels; ++j){
      labelProbArray[0][j] = filterLabelProbArray[FILTER_STAGES-1][j];
    }
  }
*/
    /** Closes tflite to release resources. */
    public void close() {
        tflite.close();
        tflite = null;
    }

    /** Reads label list from Assets. */
    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(LABEL_PATH)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Writes Image data into a {@code ByteBuffer}. */
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }

    /** Prints top-K labels, to be shown in UI as the results. */
    private void setTopKLabels(ArrayList<String> resultats) {
        StringBuffer texte = new StringBuffer();
        for (int i = 0; i < labelProbArray[0].length; ++i) {
            texte.append(" | " + labelProbArray[0][i] + " | ");
        }
        Log.e("setTopKLabels()", "toutes les probas : " + texte.toString());

        texte = new StringBuffer();
        for (int i = 0; i < labelList.size(); ++i) {
            texte.append(" | " + labelList.get(i) + " | ");
        }
        Log.e("setTopKLabels()", "tous les labels : " + texte.toString());


        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(
                    new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }
        String textToShow = "";
        Log.e("setTopKLabels()", sortedLabels.toString());
        final int size = sortedLabels.size();
        for (int i = size; i > 0; --i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            Log.e("setTopKLabels()", label.toString());
            resultats.add(String.format("\n%s: %4.2f",label.getKey(),label.getValue()));
        }
    }
}
