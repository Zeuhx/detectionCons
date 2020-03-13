/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

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

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Trace;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;
//import org.tensorflow.Operation;
//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/** A classifier specialized to label images using TensorFlow. */
public abstract class TensorFlowImageClassifier implements Classifier {
  private static final String TAG = "TensorFlowImageClassifier";

  // Only return this many results with at least this confidence.
  private static final int MAX_RESULTS = 3;
  private static final float THRESHOLD = 0.1f;

  // Config values.
  private String inputName;
  private String outputName;
  private int inputSize;
  private float imageMean;
  private float imageStd;

  // Pre-allocated buffers.
  private Vector<String> labels = new Vector<String>();
  private int[] intValues;
  private float[] floatValues;
  private float[] outputs;
  private String[] outputNames;

  private boolean logStats = false;

  //private TensorFlowInferenceInterface inferenceInterface;

  private TensorFlowImageClassifier() {}

  /**
   * Initializes a native TensorFlow session for classifying images.
   *
   * @param assetManager The asset manager to be used to load assets.
   * @param modelFilename The filepath of the model GraphDef protocol buffer.
   * @param labelFilename The filepath of label file for classes.
   * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
   * @param imageMean The assumed mean of the image values.
   * @param imageStd The assumed std of the image values.
   * @param inputName The label of the image input node.
   * @param outputName The label of the output node.
   * @throws IOException
   */

  /*
  public static Classifier create(
      AssetManager assetManager,
      String modelFilename,
      String labelFilename,
      int inputSize,
      int imageMean,
      float imageStd,
      String inputName,
      String outputName) {
    TensorFlowImageClassifier c = new TensorFlowImageClassifier();
    c.inputName = inputName;
    c.outputName = outputName;

    // Read the label names into memory.
    // TODO(andrewharp): make this handle non-assets.
    String actualFilename = labelFilename.split("file:///assets/")[1];
    Log.i(TAG, "Reading labels from: " + actualFilename);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
      String line;
      while ((line = br.readLine()) != null) {
        c.labels.add(line);
      }
      br.close();
    } catch (IOException e) {
      throw new RuntimeException("Problem reading label file!" , e);
    }

    c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

    // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
    final Operation operation = c.inferenceInterface.graphOperation(outputName);
    final int numClasses = (int) operation.output(0).shape().size(1);
    Log.i(TAG, "Read " + c.labels.size() + " labels, output layer size is " + numClasses);

    // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
    // the placeholder node for input in the graphdef typically used does not specify a shape, so it
    // must be passed in as a parameter.
    c.inputSize = inputSize;
    c.imageMean = (float) imageMean;
    c.imageStd = imageStd;

    // Pre-allocate buffers.
    c.outputNames = new String[] {outputName};
    c.intValues = new int[inputSize * inputSize];
    c.floatValues = new float[inputSize * inputSize * 3];
    c.outputs = new float[numClasses];

    return c;
  }

  public static float[] getBitmapPixels(Bitmap bitmap, int x, int y, int width, int height) {
    Log.e("getBitmapPixels()", "entrée dans la méthode getBitmapPixels()");
    Log.e("getBitmapPixels()", "création du tableau qui contiendra les pixels");
    int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
    Log.e("getBitmapPixels()", "bitmap.getPixels(...)");
    bitmap.getPixels(pixels, 0, bitmap.getWidth(), x, y,
            width, height);
    Log.e("getBitmapPixels()", "création du tableau final qui contiendra les pixels qui seront retournés");
    final float[] subsetPixels = new float[width * height];
    Log.e("getBitmapPixels()", "copie pixels par pixels");
    for (int row = 0; row < height; row++) {
      System.arraycopy(pixels, (row * bitmap.getWidth()),
              subsetPixels, row * width, width);
    }
    return subsetPixels;
  }



  @Override
  public List<Recognition> recognizeImage(Bitmap bitmap) throws Exception {
    try {
      // Log this method so that it can be analyzed with systrace.
      Trace.beginSection("recognizeImage");
      Log.e("recognizeImage()", "appel de la méthode recognizeImage()");

      Trace.beginSection("preprocessBitmap");
      //Log.e("recognizeImage()", "lecture des pixels du bitmap");
      // Preprocess the image data from 0-255 int to normalized float based
      // on the provided parameters.

      bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

      Log.e("recognizeImage()", "placement des pixels dans un tableau de floats");
      for (int i = 0; i < intValues.length; ++i) {
        final int val = intValues[i];
        floatValues[i * 3 + 0] = /*-1 * * / (((val >> 16) & 0xFF) - imageMean) / imageStd;
        floatValues[i * 3 + 1] = /*-1 * * / (((val >> 8) & 0xFF) - imageMean) / imageStd;
        floatValues[i * 3 + 2] = /*-1 * */ // ((val & 0xFF) - imageMean) / imageStd;
      //}
/*

      //Log.e("recognizeImage()", "appel de la méthode de stackOverflow pour creer un tableau de floats");
      //floatValues = getBitmapPixels(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());

      Trace.endSection();

      StringBuffer rawImage = new StringBuffer();
      for(int i = 0; i<floatValues.length; i++){
          rawImage.append(" | " + floatValues[i] + " | ");
      }
      Log.e("recognizeImage()", "rawImage : " + rawImage.toString());

      // Copy the input data into TensorFlow.
      Trace.beginSection("feed");
      Log.e("recognizeImage()", "envoie du tableau de floats à TensorFlow");
      //inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
      inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
      Trace.endSection();

      // Run the inference call.
      Trace.beginSection("run");
      Log.e("recognizeImage()", "demarrage de l'inférence");
      inferenceInterface.run(outputNames, logStats);
      // >> Provoque parfois des crashes (aucune Exception émise)
      Trace.endSection();

      // Copy the output Tensor back into the output array.
      Trace.beginSection("fetch");
      Log.e("recognizeImage()", "copie des résultats de l'inférence dans un tableau");
      inferenceInterface.fetch(outputName, outputs);
      Trace.endSection();

      String rawResults = "";
      for(int i = 0; i<outputs.length; i++){
          rawResults += " | " + outputs[i] + " | ";
      }
      Log.e("recognizeImage()", "rawResults : " + rawResults);

      // Find the best classifications.
      Log.e("recognizeImage()", "détermination des meilleurs résultats");
      PriorityQueue<Recognition> pq =
              new PriorityQueue<Recognition>(
                      3,
                      new Comparator<Recognition>() {
                        @Override
                        public int compare(Recognition lhs, Recognition rhs) {
                          // Intentionally reversed to put high confidence at the head of the queue.
                          return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                        }
                      });
      for (int i = 0; i < outputs.length; ++i) {
        if (outputs[i] > THRESHOLD) {
          pq.add(
                  new Recognition(
                          "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i], null));
        }
      }
      Log.e("recognizeImage()", "composition de la liste finale des résultats");
      final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
      int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
      for (int i = 0; i < recognitionsSize; ++i) {
        recognitions.add(pq.poll());
      }
      Trace.endSection(); // "recognizeImage"
      Log.e("recognizeImage()", "retour de la liste des résultats");
      return recognitions;
    } catch(Throwable t){
      Log.e("recognizeImage()", "Erreur lors de l'execution de recognizeImage()");
      t.printStackTrace();
    }
    Log.e("recognizeImage()", "Erreur lors de l'execution de recognizeImage() sans envoie d'un Throwable");
    throw new Exception("Erreur sans envoie d'un Throwable !");

  }

  @Override
  public void enableStatLogging(boolean logStats) {
    this.logStats = logStats;
  }

  @Override
  public String getStatString() {
    return inferenceInterface.getStatString();
  }

  @Override
  public void close() {
    inferenceInterface.close();
  }
  */
}
