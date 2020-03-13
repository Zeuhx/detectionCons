package descartes.info.l3ag2.eyetrek.classes;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


/**
 * Created by Dorian on 22/02/2018.
 */
public class PreProcessing {

    static {
        System.loadLibrary("opencv_java3");
    }

    //image source
    private static Mat sourceImage;
    //image temporaire qui va subir les traitements
    private static Mat tempMat;

    /**
     * initImageProcessing - Initialise le traitement d'image en chargeant l'image source
     * @param imageChemin chemin de l'image source
     */
    public static void initImageProcessing(String imageChemin) {
        sourceImage = Imgcodecs.imread(imageChemin);
    }


    /**
     * resize - Redimensionne l'image selon une taille prédéfinie (Largeur et hauteur).
     * tempMat devient l'image redimensionnée.
     * @param image
     * @return
     */
    private static Mat resize(Mat image) {

        double widthImg = image.size().width;
        double heightImg = image.size().height;

        double aspectRatio = widthImg / heightImg;

        double widthModePaysage = 256;
        double heightModePortrait = 256;

        Mat dest = new Mat(image.rows(),image.cols(),image.type());

        if(modePaysage(image)) {
            double newHeight = widthModePaysage / aspectRatio;

            Imgproc.resize(image,dest,new Size(widthModePaysage,newHeight));
        }
		else if(widthImg == heightImg) {
			Imgproc.resize(image,dest,new Size(256,256));
		}
        else {
            double newWidth = heightModePortrait * aspectRatio;

            Imgproc.resize(image,dest,new Size(newWidth,heightModePortrait));
        }

        return dest;
    }


    /**
     * modePaysage - Fonction permettant de savoir si une image est en mode Paysage ou Portrait
     * @param image l'image dont on veut tirer l'information
     * @return Vrai si l'image est en paysage, faux si elle est en portrait
     */
    private static boolean modePaysage(Mat image) {
        return image.size().width > image.size().height;
    }

    /**
     * 3.2.2 Greyscaling :
     * Convertit l'image en nuances de gris.
     * tempMat devient l'image traitée avec la conversion en nuiances de gris.
     */
    private static Mat greyScale(Mat mat) {
        Mat dest = new Mat(mat.rows(),mat.cols(),mat.type());

        Imgproc.cvtColor(mat,dest, Imgproc.COLOR_BGR2GRAY);

        return dest;
    }


    private static void greyScale() {
        Mat dest = new Mat(tempMat.rows(),tempMat.cols(),tempMat.type());

        Imgproc.cvtColor(tempMat,dest, Imgproc.COLOR_BGR2GRAY);

        tempMat = dest;
    }


    /**
     * 3.2.3 Thresholding
     * Seuillage avec la méthode d'Otsu qui permet d'obtenir une image binaire.
     * tempMat devient l'image traitée avec le seuillage d'otsu.
     */
    private static Mat thresholdingOtsu(Mat mat) {
        Mat dest = new Mat(mat.rows(),mat.cols(),mat.type());

        Imgproc.GaussianBlur(mat,dest,new Size(5,5),0);

        Imgproc.threshold(mat,dest,0,255, Imgproc.THRESH_OTSU);

        return dest;
    }



    private static void thresholdingOtsu() {
        Mat dest = new Mat(tempMat.rows(),tempMat.cols(),tempMat.type());

        Imgproc.GaussianBlur(tempMat,dest,new Size(5,5),0);
        tempMat = dest;

        Imgproc.threshold(tempMat,dest,0,255, Imgproc.THRESH_OTSU);
        tempMat = dest;
    }


    /**
     * 3.2.4 Opening operations
     * Opérations qui permettent de supprimer les bruits.
     * On applique 3 fois une série d'érosion suivie de dilatation.
     */

    private static Mat openingOperations(Mat img){

        Mat dest;

        dest = erosion(img);
        dest = dilation(dest);

        return dest;
    }



    /**
     * 3.2.4 Opening operations : erosion
     * tempMat devient l'image traitée avec l'érosion.
     */
    private static Mat erosion(Mat img){

     Mat dest = new Mat(img.rows(), img.cols(), img.type());

     Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(7,7), new Point());

     Imgproc.erode(img, dest, element);

     return dest;

    }

    private static void erosion() {
        Mat dest = new Mat(tempMat.rows(),tempMat.cols(),tempMat.type());
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(7,7),new Point());

        Imgproc.erode(tempMat, dest, element);

        tempMat = dest;
    }

    /**
     * 3.2.4 Opening operations : dilation
     * tempMat devient l'image traitée avec la dilatation.
     */
    private static Mat dilation(Mat img){

        Mat dest = new Mat(img.rows(), img.cols(), img.type());

       Imgproc.dilate(img, dest, Mat.ones(7,7, img.type()));

       return dest;

    }

    private static void dilation() {
        Mat dest = new Mat(tempMat.rows(),tempMat.cols(),tempMat.type());
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(7,7),new Point());


        Imgproc.dilate(tempMat,dest, element);

        tempMat = dest;
    }

    /**
     * thresholding - Seuillage permettant d'avoir une image binaire
     * @param mat image que l'on veut binariser
     * @return l'image binarisée
     */
    private static Mat thresholding(Mat mat) {
        Mat dest = new Mat(mat.rows(),mat.cols(),mat.type());

        Imgproc.threshold(mat,dest,127,255, Imgproc.THRESH_BINARY);

        return dest;
    }


    /**
     * 3.2.5 Inverse threshold
     * Seuillage avec la méthode d'Otsu qui permet d'obtenir une image binaire.
     * tempMat devient l'image traitée avec le seuillage d'otsu.
     */

    private static Mat thresholdingInverse(Mat mat){

        Mat dest = new Mat(mat.rows(),mat.cols(),mat.type());

        Imgproc.threshold(mat,dest,127,255, Imgproc.THRESH_BINARY_INV);
        //Imgproc.adaptiveThreshold(tempMat,dest,255.0,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY_INV,11,2);

        return dest;
    }



    private static void thresholdingInverse() {
        Mat dest = new Mat(tempMat.rows(),tempMat.cols(),tempMat.type());

        Imgproc.threshold(tempMat,dest,127,255, Imgproc.THRESH_BINARY_INV);
        //Imgproc.adaptiveThreshold(tempMat,dest,255.0,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY_INV,11,2);

        tempMat = dest;
    }


    /**
     * rotation - Fonction pour tourner une image de 90°
     * @param image l'image que l'on veut tourner
     * @return l'image tournée
     */
    private static Mat rotation(Mat image) {
        Mat dest = new Mat();

        double widthImg = image.size().width;
        double heightImg = image.size().height;

        //si la largeur est supérieure à la hauteur, on effectue une rotation de 90 deg
        //dans le sens des aiguilles d'une montre
        if(widthImg > heightImg) {
            Core.rotate(image,dest,Core.ROTATE_90_CLOCKWISE);
        }

        return dest;
    }

    /**
     * preProcess - Fonction permettant l'application de resize, greyScale, threshold et threshold sur une image.
     * @param roadName chemin d'accès à l'image
     * @return retourne l'image traité
     */
    public static Mat preProcess(String roadName){
        return openingOperations(PreProcessing.thresholdingInverse(PreProcessing.thresholdingOtsu(PreProcessing.greyScale(PreProcessing.resize(Imgcodecs.imread(roadName))))));
    }

    /**
     * rotationProcess - Fonction permettant de lancer une rotation
     * @param image l'image que l'on veut tourner
     * @return L'image retournée
     */
    public static Mat rotationProcess(Mat image){
        return rotation(image);
    }
}