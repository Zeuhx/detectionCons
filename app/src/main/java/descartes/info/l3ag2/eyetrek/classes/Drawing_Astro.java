package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class Drawing_Astro {

    public static void draw_lines(List<Ligne> lignes, Mat img){
        for(Ligne ligne : lignes){
            Imgproc.line(img, ligne.getPoint_a(), ligne.getPoint_b(), new Scalar(255, 255,255));
        }
    }

    public static void draw_stars(List<Double> x_array, List<Double> y_array, int echelle, Mat img){
        for(int i = 0 ; i < x_array.size() ; i++){
            /**
             * Img en Mat
             * Centre en Point
             * Radius en int
             * Color en Scalar (jaune)
             * Epaisseur : negative signifie qu'un cercle plein.
             */
            Imgproc.circle(img, new Point(Math.round(x_array.get(i) * 100),-Math.round(y_array.get(i) * 100)), echelle, new Scalar(255, 0,0), 3);
        }
    }

    public static void draw_stars(List<Double> x_array, List<Double> y_array, int echelle, Mat img, int taille_trait){
        for(int i = 0 ; i < x_array.size() ; i++){
            Imgproc.circle(img, new Point(Math.round(x_array.get(i) * 100),-Math.round(y_array.get(i) * 100)), echelle, new Scalar(255, 255,0), taille_trait);
        }
    }

    /**
     * Cette methode trace l'ensemble des lignes
     * d'une constellation
     * @param connexion
     * @return
     */
    public static void traceLineEnsemble(Mat image, String connexion, List<Double> etoile_x_array,List<Double> etoile_y_array,int indice_luminosite[]){
        String[] result = connexion.split(",");
        for(int i_nombreEtoile = 0 ; i_nombreEtoile < result.length ; i_nombreEtoile++){
            //Log.d(TAG, "traceLineEnsemble: result[" + i_nombreEtoile + "] : " + result[i_nombreEtoile] + "contains . ? : " + result[i_nombreEtoile].contains("."));
            if(result[i_nombreEtoile].contains(".")){

                String[] tableOrigineArrive = result[i_nombreEtoile].split("\\.");
                //Log.d(TAG, "traceLineEnsemble: tableOrigineArrive length : " + tableOrigineArrive.length);
                for(int i_nombreLigneParEtoile = 0 ; i_nombreLigneParEtoile < tableOrigineArrive.length ; i_nombreLigneParEtoile++){
                    //Log.d(TAG, "traceLineEnsemble: tableOrigineArrive[" + i_nombreLigneParEtoile + "] : " + tableOrigineArrive[i_nombreLigneParEtoile]);
                    int indice = Integer.parseInt(tableOrigineArrive[i_nombreLigneParEtoile]);
                    traceUneLigne(indice_luminosite[i_nombreEtoile], indice_luminosite[indice], image,etoile_x_array,etoile_y_array);
                }
            }
            else {
                int indice = Integer.parseInt(result[i_nombreEtoile]);
                traceUneLigne(indice_luminosite[i_nombreEtoile], indice_luminosite[indice], image, etoile_x_array, etoile_y_array);
            }
        }
    }

    /**
     * À partir d'une image, cette méthode trace une ligne en plus
     * @param indiceTracageOrigine
     * @param indiceTraceArrivee
     * @param image
     * @return
     */
    public static void traceUneLigne(int indiceTracageOrigine, int indiceTraceArrivee, Mat image, List<Double> etoile_x_array,List<Double> etoile_y_array){
        Point pointOrigine = new Point(etoile_x_array.get(indiceTracageOrigine)*100, etoile_y_array.get(indiceTracageOrigine)*-100);
        Point pointArrivee = new Point(etoile_x_array.get(indiceTraceArrivee)*100, etoile_y_array.get(indiceTraceArrivee)*-100);
        Imgproc.line(image, pointOrigine, pointArrivee, new Scalar(255,255,255), 15);
    }

    public static Bitmap dessineAsterisme(Bitmap image, Constellation_Candidat constellation_candidat){
        Mat imagedisp = new Mat();
        Utils.bitmapToMat(image,imagedisp);
        Imgproc.cvtColor(imagedisp,imagedisp,Imgproc.COLOR_RGB2BGR);
        Imgproc.cvtColor(imagedisp,imagedisp,Imgproc.COLOR_BGR2RGB);

        traceLineEnsemble(imagedisp,constellation_candidat.getConnexion(),constellation_candidat.getX_arrayTemp(),constellation_candidat.getY_arrayTemp(),constellation_candidat.getIndice_luminosite());
        draw_stars(constellation_candidat.getX_arrayTemp(),constellation_candidat.getY_arrayTemp(),30,imagedisp,-1);

        Bitmap bmp = Bitmap.createBitmap(imagedisp.cols(),imagedisp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imagedisp, bmp);

        return bmp;
    }

}
