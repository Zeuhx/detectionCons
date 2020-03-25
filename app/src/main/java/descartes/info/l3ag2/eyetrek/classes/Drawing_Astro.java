package descartes.info.l3ag2.eyetrek.classes;

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
            Imgproc.circle(img, new Point(Math.round(x_array.get(i) * 100),-Math.round(y_array.get(i) * 100)), echelle, new Scalar(255, 255,0), 3);
        }
    }

}
