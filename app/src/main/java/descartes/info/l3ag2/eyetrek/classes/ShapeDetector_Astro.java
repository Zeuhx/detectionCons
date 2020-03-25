package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

public class ShapeDetector_Astro {

    private static final String TAG = "ShapeDetector_Astro";

    /**
     * Avoir tout de l'image
     * @param img
     * @return
     */
    public static Mat get_all(Mat img){
        Mat result = new Mat();
        Imgproc.cvtColor(img, result, Imgproc.COLOR_BGR2RGB);
        Mat final_result = new Mat();
        Core.inRange(result, new Scalar(0,0,1), new Scalar(255,255,255), final_result);
        return final_result;
    }

    /**
     * Cette methode permet d'avoir seulement les etoiles dans le template
     * @param img l'image source
     * @return une matrice avec seulement l'endroit ou il y a les etoiles
     */
    public static Mat get_stars(Mat img){
        Mat result = new Mat();
        /**
         * https://docs.opencv.org/2.4/modules/imgproc/doc/miscellaneous_transformations.html
         * La methode cvtColor convertit une image d'un espace colorimétrique en un autre.
         * Src : img - l'image d'entree
         * Dst : result - l'image de retour ou de destination
         * Code : code de conversion de l'espace colorimetrique
         */
        Imgproc.cvtColor(img,result,Imgproc.COLOR_BGR2RGB);
        Mat dest = new Mat();
        /**
         * La methode range verifie si les elements du tableau
         * se trouvent entre les elements deux autres tableaux
         * Src : result - img source
         * lowerb : sous forme d'une Scalar - valeur la plus petite
         * upperb : sous forme d'une Scalar - valeur la plus grande
         * Dst : dest - sortie et destination
         */
        Core.inRange(result,new Scalar(0, 0, 240), new Scalar(10, 10, 255),dest);
        return dest;
    }

    /**
     * Cette methodes recupere les lignes d'une image les renvoie
     * sous forme d'une matrice (image)
     * @param img image source
     * @return les lignes
     */
    public static Mat get_lines(Mat img){
        Mat result = new Mat();
        Core.inRange(img, new Scalar(1,0,0), new Scalar(255,255,255), result);
        return result;
    }

    /**
     * Supprime toutes les etoiles d'une image et renvoie le resultat
     * @param img une image dont les etoiles ont ete supprimees
     * @return
     */
    public static Mat remove_stars(Mat img){
        Mat image_finale = img.clone();
        Mat stars = get_stars(img);
        int tresh = 127 ; // le seuillage
        Mat image_dest = new Mat();
        Imgproc.threshold(img, image_dest, tresh, 255, 1);

        // findContours
        List<MatOfPoint> contours = new ArrayList<>() ; // result
        Mat hierarchy = new Mat();
        int mode = Imgproc.RETR_TREE ; // sous forme d'arbre en fonction du contenu
        int method = Imgproc.CHAIN_APPROX_SIMPLE ;
        /**
         * https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html
         * Recherche les contours dans une image binaire.
         * input : img - image source binarise
         * contours : contours - ce sont les contours detectee, chaque contour est stocke sous forme de vecteur de points.
         * hierarchy : hierarchy - vecteur de sortie optionnel, ils comportent
         *              des infos supp comme la topologie, et le nb d'element de contour
         * mode : RETR_TREE : d'apres OpenCV
         *             Recupere tous les contours et reconstruit
         *             une hierarchie complete des contours imbriques. Cette
         *             hierarchie complete est construite
         *             et montree dans la demo OpenCV contours
         *             En gros ca construit sous forme d'un arbre
         * method : CHAIN_APPROX_SIMPLE : a quelle approximation ? laisse que les extremite
         */
        Imgproc.findContours(img, contours, hierarchy, mode, method, new Point(-1,-1));

        for(MatOfPoint cnt : contours){
            /**
             * La methode arcLength calcule et renvoie le perimetre de contour ou une longeur de courbe
             * curve : de type MatOfPoint2f : convertit en tableau puis new - valeur d'entree
             * closed : est-ce que la courbe est ferme ou non ?
             */
            double espilon = 0.01* Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            MatOfPoint2f approxCurve = new MatOfPoint2f() ;
            /**
             * Rapproche une ou des courbes polygonales avec la precision specifiee
             * --> ferme les contours donc
             * Parametre :
             * Une matrice,
             * resultat de l'approximation (sortie)
             * espilon: precision de l'approximation. Il s'agit de la distance maximale entre la courbe d'origine et son approximation
             * recursivite
             */
            Imgproc.approxPolyDP(new MatOfPoint2f(cnt.toArray()),approxCurve, espilon, true);
            if(approxCurve.total() > 8 ){ // l'etoile est reconnue
                /**
                 * Dessine des contours ou des contours remplis.
                 * ImageDestination : image_finales
                 * Contours : contours, Tous les contours d'entree. Chaque contour est stocke en tant que vecteur ponctuel.
                 * ContourIdx : -1 : tous les contours sont dessines
                 */
                Imgproc.drawContours(image_finale, contours, -1, new Scalar(0,0,0));
            }
        }
        return image_finale ;
    }

    /**
     * Cette methode permet
     * la detection des etoiles et renvoie la liste des points sous formes
     * d'une liste de x, y, et label c'est a dire la taille du rond
     * @param image_src img d'entree
     * @param image_dest img de sortie
     * @param x_array le vecteur de x de retour, represetant les coordonnees x de l'etoile sur l'image
     * @param y_array le vecteur de y de retour ...
     * @param label_array le vecteur de label de retour, representant la taille des cercle des etoiles
     */
    public static void detect_stars(Mat image_src, Mat image_dest, List<Double> x_array, List<Double> y_array, List<Double> label_array){
        Imgproc.threshold(image_src, image_dest, 100,255,Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>() ; // result
        Mat hierarchy = new Mat();
        int mode = Imgproc.RETR_LIST ; // sous forme d'arbre en fonction du contenu
        int method = Imgproc.CHAIN_APPROX_SIMPLE ;
        /**
         * L'utilisation de la methode findContours est explique dans la methode " remove_stars "
         */
        Imgproc.findContours(image_src, contours, hierarchy, mode, method, new Point(0,0));
        //Log.d(TAG, "detect_stars: find contour " + contours.size());
        // From :  contours, h = cv2.findContours(thresh, 1, 2)

        /*ArrayList<Double> x_array = new ArrayList<>() ;  // Contient la liste de coordonees x
        ArrayList<Double> y_array = new ArrayList<>() ;
        ArrayList<Double> label_array = new ArrayList<>() ;*/

        Log.d(TAG, "detect_stars: find Contours :" + contours.size());
        /**
         * On parcours l'ensemble des contours
         * @TODO a completer par la suite
         */
        for (MatOfPoint cnt: contours) {
            double espilon = 0.01 * Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            MatOfPoint2f approxCurve = new MatOfPoint2f();

            /**
             * L'utilisation de cette methode est preciser dans les methodes precedentes
             * Parametre :
             * Une matrice,
             * resultat de l'approximation (sortie)
             * espilon: precision de l'approximation. Il s'agit de la distance maximale entre la courbe d'origine et son approximation
             * recursivite
             */
            Imgproc.approxPolyDP(new MatOfPoint2f(cnt.toArray()),approxCurve, espilon, true);
            // Si il y a une etoie qui est trouve, pas sur ...
            //Log.d(TAG, "detect_stars: approxCurve.total() > 2 " + (approxCurve.total() > 2) );
            if(approxCurve.total() >= 2){
                // Calcule tous les moments jusqu'au troisième ordre d'un polygone ou d'une forme trame
                Moments m = Imgproc.moments(cnt);
                double x_etoile = (m.m10/m.m00)/100 ;
                double y_etoile = ((m.m01/m.m00)*-1)/100 ;

                /*Log.d(TAG, "detect_stars: x " + x_etoile);
                Log.d(TAG, "detect_stars: y " + y_etoile);*/

                //Log.d(TAG, "detect_stars: contoursArea < 100000 " + (Imgproc.contourArea(cnt)<100000));
                if(Imgproc.contourArea(cnt)<100000){
                    x_array.add(x_etoile);
                    y_array.add(y_etoile);

                    // utiliser la plus petite longueur de rectangle englobant comme taille relative
                    Rect rect = Imgproc.boundingRect(cnt);
                    int h = rect.height ;
                    int w = rect.width ;
                    label_array.add((double) Math.round(Math.min(w,h)*100d)/100d);
                }
            }
        }

/*      Log.d(TAG,"MainActivity  x_array : " + x_array.size());
        Log.d(TAG,"MainActivity  y_array : " + y_array.size());
        Log.d(TAG,"MainActivity  label : " + label_array.size());*/
    }

    public static void detect_lines(Mat img){
        Mat lignes = get_lines(img);
        Mat ligne_vecteur = new Mat();
        double rho = 1 ;
        double theta = Math.PI/180 ; // 1 degre = pi/180
        int threshold = 8 ; // Seuillage a 8px
        double minLineLength = 0 ;
        double maxLineGap = 30 ;
        /**
         * Img : image sources
         * lines : les vecteurs des lignes representer par (x1,y1,x2,y2)
         *         ou (x1, y1) et (x2, y2) les points de fin de chaque segment de ligne detectee
         * rho : Resolution de distance de l'accumulateur en pixel ???
         * theta : Resolution angulaire de l'accumulateur en radians ???
         * threashold : le parametre de seuillage
         * minLineLength : Longueur de ligne minimale.
         * maxLineGap : L'ecart maximum entre le point et la ligne
         **/
        Imgproc.HoughLinesP(img, ligne_vecteur, rho, theta, threshold, minLineLength, maxLineGap);

        // Convertir le tableau int en float pour conserver la forme apres la mise a l'échelle (scaling)

        Mat newLigne_vecteur = new Mat();
        newLigne_vecteur.convertTo(ligne_vecteur, CvType.CV_32FC(1));       // @TODO Je sais pas ???? C'est bizarre la conversion... comprends pas
        /**
         *
         * Developper la partie ou on
         * relie les points
         * grace a la database
         * qui contient les informations du voisins
         *
         */
    }

    /**
     * Cette fonction permet de supprimer le texte issue de nos template afin de pouvoir les manipuler par la suite
     * @param templ
     * @return
     */
    public static Mat deleteName(Bitmap templ){
        Mat template = new Mat();
        Utils.bitmapToMat(templ,template);

        //Conversion des couleurs du template avec des nuances de gris
        Mat mGray = new Mat(templ.getHeight(),templ.getWidth(),CvType.CV_8UC1);
        Imgproc.cvtColor(template, mGray, Imgproc.COLOR_BGR2GRAY);


        Mat tresh = new Mat();
        Mat text = new Mat();

        // seuille du template afin d'obtenir que le text et non les différentes formes
        Imgproc.threshold(mGray,text,170,255,Imgproc.THRESH_BINARY_INV);
        Imgproc.threshold(template,tresh,100,255,Imgproc.THRESH_BINARY);


        Size kernelSize = new Size(11, 11);
        final Point anchor = new Point(-1, -1);
        final int iterations = 1;

        kernelSize = new Size(11, 11);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);

        //On erode (car le seuillage a été inversé) afin d'augmenter la taille de l'écriture texte
        Imgproc.erode(text, text, kernel, anchor, 1);

        Mat textColor = new Mat();
        Imgproc.cvtColor(text,textColor,Imgproc.COLOR_GRAY2BGRA);

        Mat image_finale = new Mat();
        Core.bitwise_and(tresh,textColor,image_finale);

        return image_finale;
    }
}
