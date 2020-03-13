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
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import descartes.info.l3ag2.eyetrek.classes.UtilitaireModels;

/** Classifies images with Tensorflow Lite. */
public class ImageClassifier {

  /** Tag for the {@link Log}. */
  private static final String TAG = "TfLiteCameraDemo";

  /** Name of the label file stored in Assets. */
  private static final String LABEL_PATH = "labelstest.txt";

  /** Number of results to show in the UI. */
  private static final int RESULTS_TO_SHOW = 3;

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
  public List<String> labelList;

  /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
  private ByteBuffer imgData = null;

  /** An array to hold inference results, to be feed into Tensorflow Lite as outputs. */
  public float[][] labelProbArray = null;
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
  public ImageClassifier(Activity activity, String model_path, String labels_path) throws IOException {
    try{ // On fait en sorte que le non chargement du modele ne stoppe pas l'instantiation de l'objet pou qu'on puisse quand meme fairer une analyse en réseau
      tflite = new Interpreter(loadModelFile(activity, model_path));
    } catch (Throwable t){
      Log.e("ImageClassifier()", "impossible de charger le modele");
      t.printStackTrace();
    }

    labelList = loadLabelList(activity, labels_path);
    imgData =
        ByteBuffer.allocateDirect(
            4 * DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);
    imgData.order(ByteOrder.nativeOrder());
    labelProbArray = new float[1][labelList.size()];
    Log.e(TAG, "labelList.size() : " + labelList.size());
    filterLabelProbArray = new float[FILTER_STAGES][labelList.size()];
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  /** Classifies a frame from the preview stream. */
  public ArrayList<ArrayList<Object>> classifyFrame(Bitmap bitmap) {
    ArrayList<ArrayList<Object>> resultats = new ArrayList<>();
    if (tflite == null) {
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");
      //resultats.add("Le classifieur n'a pas été initialisé");
      return resultats;
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

    //textToShow = Long.toString(endTime - startTime) + "ms" + textToShow;
    return setTopKLabels(resultats);
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
  private List<String> loadLabelList(Activity activity, String labels_path) throws IOException {
    List<String> labelList = new ArrayList<String>();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(labels_path)));
    String line;
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    reader.close();
    Log.e("loadLabelList()", "Taille de la liste des labels : " + labelList.size());
    return labelList;
  }

  /** Memory-map the model file in Assets. */
  private MappedByteBuffer loadModelFile(Activity activity, String model_path) throws IOException {
    try {
      File fichier_modele = new File(model_path);
      fichier_modele.setReadable(true, false);
      fichier_modele.setWritable(true, false);
      fichier_modele.setExecutable(true, false);
      Log.e("loadModelFile()", "\n" +
              "Path : " + fichier_modele.getPath() + "\n" +
              "Existe : " + fichier_modele.exists() + "\n" +
              "Lire : " + fichier_modele.canRead() + "\n" +
              "Ecrire : " + fichier_modele.canWrite() + "\n" +
              "absolute path : " + fichier_modele.getAbsolutePath() + "\n" +
              "canonical path : " + fichier_modele.getCanonicalPath());
      AssetFileDescriptor fileDescriptor;
      try{
        synchronized (this) {
          ParcelFileDescriptor parcelFileDescriptor = activity.getContentResolver().openFileDescriptor(Uri.fromFile(fichier_modele), "rw");
          fileDescriptor = new AssetFileDescriptor(parcelFileDescriptor, 0, fichier_modele.length());
        }
      } catch (Throwable t){
        t.printStackTrace();
        try{
          fileDescriptor = activity.getApplicationContext().getAssets().openNonAssetFd(fichier_modele.getAbsoluteFile().getAbsolutePath());
        } catch (Throwable tt){
          tt.printStackTrace();
          try{
            fileDescriptor = activity.getApplicationContext().getAssets().openNonAssetFd(fichier_modele.getCanonicalFile().getCanonicalPath());
          } catch (Throwable ttt){
            fileDescriptor = activity.getApplicationContext().getAssets().openNonAssetFd(0, fichier_modele.getPath());
          }
        }

      }
      FileInputStream inputStream = new FileInputStream(new File(model_path));
      FileChannel fileChannel = inputStream.getChannel();
      long startOffset = fileDescriptor.getStartOffset();
      long declaredLength = fileDescriptor.getDeclaredLength();
      return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    } catch(FileNotFoundException e){
      Log.e("loadModelFile()", "Path : " + new File(model_path).getPath() + "\n" +
              "Existe : " + new File(model_path).exists() + "\n" +
              "Lire : " + new File(model_path).canRead() + "\n" +
              "Ecrire : " + new File(model_path).canWrite());
      e.printStackTrace();
    }
    return null;
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
  public ArrayList<ArrayList<Object>> setTopKLabels(ArrayList<ArrayList<Object>> resultats) {

    Log.e("setTopKLabels()", "labelProbArray.length : " + labelProbArray.length);
    int position_resultat = 0;
    float temp_resultat = 0;
    for (int i = 0; i < labelProbArray[0].length; i++) {
      if(labelProbArray[0][i] > temp_resultat){
        position_resultat = i;
        temp_resultat = labelProbArray[0][i];
      }
    }
    Log.e("setTopKLabels()", "position_resultat : " + position_resultat);


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

    /*
    for (int i = 0; i < labelList.size(); ++i) {
      sortedLabels.add(
          new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));
      if (sortedLabels.size() > RESULTS_TO_SHOW) {
        sortedLabels.poll();
      }
    }
    */


    ArrayList<Object> especeETproba = new ArrayList<>();
    int position_1e = 0;
    float valeur_1e = 0;
    for (int i = 0; i < labelProbArray[0].length; i++) {
      if(labelProbArray[0][i] > valeur_1e){
        position_1e = i;
        valeur_1e = labelProbArray[0][i];
      }
    }
    especeETproba.add(labelList.get(position_1e));
    especeETproba.add(new Double(valeur_1e));
    resultats.add(especeETproba);

    especeETproba = new ArrayList<>();
    int position_2e = 0;
    float valeur_2e = 0;
    for (int i = 0; i < labelProbArray[0].length; i++) {
      if(labelProbArray[0][i] > valeur_2e && labelProbArray[0][i] < valeur_1e){
        position_2e = i;
        valeur_2e = labelProbArray[0][i];
      }
    }
    especeETproba.add(labelList.get(position_2e));
    especeETproba.add(new Double(valeur_2e));
    resultats.add(especeETproba);

    especeETproba = new ArrayList<>();
    int position_3e = 0;
    float valeur_3e = 0;
    for (int i = 0; i < labelProbArray[0].length; i++) {
      if(labelProbArray[0][i] > valeur_3e && labelProbArray[0][i] < valeur_2e){
        position_3e = i;
        valeur_3e = labelProbArray[0][i];
      }
    }

    especeETproba.add(labelList.get(position_3e));
    especeETproba.add(new Double(valeur_3e));
    resultats.add(especeETproba);
    /*
    String textToShow = "";
    Log.e("setTopKLabels()", sortedLabels.toString());
    final int size = 3;//sortedLabels.size();
    for (int i = size; i > 0; --i) {
      Map.Entry<String, Float> label = sortedLabels.poll();
      Log.e("setTopKLabels()", label.toString());
      especeETproba.add(label.getKey());
      especeETproba.add(new Double(label.getValue()));
      resultats.add(especeETproba);
      //resultats.add(String.format("\n%s: %4.2f",label.getKey(),label.getValue()));
    }
    */

    return resultats;
  }
}
