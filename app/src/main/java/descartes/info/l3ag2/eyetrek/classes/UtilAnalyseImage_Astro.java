package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import descartes.info.l3ag2.eyetrek.fragment.Fragment_AstroAffichagePhoto;

/**
 * Cette classe est une classe permettant l'utilisation des outils d'analyse d'une image
 * Les informations sont detaillees directement dans leurs methodes, mais voici un petit appercu
 * Elle propose :
 *  - binariser : la binarisation d'une etoile
 *  - findConstellationMatch : permettant la recherche d'une image correspondante a travers des donnees fournies
 *  - rotate : rotation d'une image en fonction deux deux etoiles rentree en parametre et appelle la methode " rotateTemplate"
 *  - rotateTemplate : lorsqu'on a fini de calculer l'angle, on fait la rotation
 */
public class UtilAnalyseImage_Astro implements Runnable {

    private static final String TAG = "UtilAnalyseImage_Astro";
    private List<Double> x_arrayDetect;
    private List<Double> y_arrayDetect;
    private List<Double> label_arrayDetect;
    private int [] indiceEtoilelumin;
    public static final int ETOILE_HEMISPHERE_BOREAL = 36;

    private Bitmap bitmap;

    private static UtilAnalyseImage_Astro utilAnalyseImage_astro = null;


    private UtilAnalyseImage_Astro(){ }

    public static UtilAnalyseImage_Astro getInstance(){
        if(utilAnalyseImage_astro == null){
            utilAnalyseImage_astro = new UtilAnalyseImage_Astro();
        }
        return utilAnalyseImage_astro;
    }

    public int trouver_constellation(Constellation_Astro constellation_astro, List<Double> x_array, List<Double> y_array,
                                       int position1, int position2, double test_scale, List<Double> x_arrayTemp,List<Double> y_arrayTemp){
        double x0 = x_array.get(position1);
        double x1 = x_array.get(position2);

        double y0 = y_array.get(position1);
        double y1 = y_array.get(position2);

        double dx = x1 - x0;
        double dy = y1 - y0;

//      Log.d(TAG, "trouver_constellation: dx : " + dx);
//      Log.d(TAG, "trouver_constellation: dy : " + dy);

        //Calcul du vecteur entre les deux points les plus lumineux du templates
        double dxt = constellation_astro.getEtoile_x_array().get(constellation_astro.getIndice_luminosite()[1]) - constellation_astro.getEtoile_x_array().get(constellation_astro.getIndice_luminosite()[0]);
        double dyt = constellation_astro.getEtoile_y_array().get(constellation_astro.getIndice_luminosite()[1]) - constellation_astro.getEtoile_y_array().get(constellation_astro.getIndice_luminosite()[0]);

        //Calcule la différence d'angle entre deux vecteurs, la paire d'étoile choisi et le vecteur des deux étoiles les pluse lumineuses du Template
        double angle = -Math.atan2(dyt,dxt);
        //Log.i(TAG, "Rotation angle: " + angle);

//        List<Double> x_arrayTemp = new ArrayList<>();
//        List<Double> y_arrayTemp = new ArrayList<>();

        initTemp(constellation_astro.getNbOfStars(),x_arrayTemp,y_arrayTemp);

        rotateTemplates(constellation_astro,angle,x_arrayTemp,y_arrayTemp);

        angle = calculeAngle(dx,dy,x_arrayTemp.get(constellation_astro.getIndice_luminosite()[1]) - x_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]),y_arrayTemp.get(constellation_astro.getIndice_luminosite()[1]) - y_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]));

        rotateTemplates(constellation_astro.getNbOfStars(),angle,x_arrayTemp,y_arrayTemp);

        double cdx = x_arrayTemp.get(constellation_astro.getIndice_luminosite()[1]) - x_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]);
        test_scale = cdx/dx;

        toMemeEchelle(x_arrayTemp,y_arrayTemp,test_scale);
        alignerToCoordonnes(x_arrayTemp,y_arrayTemp,x0,y0,x_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]),y_arrayTemp.get(constellation_astro.getIndice_luminosite()[0]));

        int nbEtoile = checkEtoileMatch(x_arrayTemp,y_arrayTemp,x_array,y_array,test_scale);
        Log.d(TAG, "trouver_constellation: nbEtoile : " + nbEtoile);
        return nbEtoile;
    }

    /**
     * Cette methode est appellee pour chaque photo prise ou image importée depuis le provider.
     * Cette methode permet de detecter la position
     * des étoiles ainsi que sa taille
     * on execute un algorithme de detection de blob sur la frame et on stock les données dans un tableau trier.
     */
    public Bitmap starAnalyse() {

        Mat imagefinal = new Mat();
        Utils.bitmapToMat(bitmap,imagefinal);

        Mat image = binariser(imagefinal,100);

        Mat temp_image = ShapeDetector_Astro.get_all(image);

        x_arrayDetect = new ArrayList<>();
        y_arrayDetect = new ArrayList<>();
        label_arrayDetect = new ArrayList<>();
        indiceEtoilelumin = new int[label_arrayDetect.size()];

        Mat starDetected = new Mat();
        ShapeDetector_Astro.detect_stars(temp_image,starDetected,x_arrayDetect,y_arrayDetect,label_arrayDetect);

        indiceEtoilelumin = ArraysUtils.argsort(ArraysUtils.convertDoublePrimitiveArray(label_arrayDetect),false);


        Log.d(TAG, "starAnalyze: x_array : " + x_arrayDetect);
        Log.d(TAG, "starAnalyze: y_array : " + y_arrayDetect);
        Log.d(TAG, "starAnalyze: label_array : " + label_arrayDetect);
        Log.d(TAG, "starAnalyze: indice : " + ArraysUtils.StringArray(indiceEtoilelumin));
        Log.d(TAG, "starAnalyze: Size : " + x_arrayDetect.size());


        Drawing_Astro.draw_stars(x_arrayDetect,y_arrayDetect,30,imagefinal,3);

        Bitmap bmp = Bitmap.createBitmap(imagefinal.cols(),imagefinal.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imagefinal, bmp);

        return bmp;

    }

    public List<Constellation_Candidat> compareConstellation() {
        List<Constellation_Astro> list_template = BuildConstellation_Astro.getInstance().getConstellation_astros();

        List<Constellation_Candidat> constellation_candidat = new ArrayList<>();


        for(int j = 0; j < list_template.size();j++) {
            int trouve = 0;
            int indiceEtoileLum0 = 0;
            int indiceEtoileLum1 = 0;
            double echelle = 0;
            List<Double> x_arrayTempRot = null;
            List<Double> y_arrayTempRot = null;
            //Log.d(TAG, "compareConstellation: name : " + list_template.get(j).getName());
            for (int i = 0; i < indiceEtoilelumin.length; i++) {
                for (int k = 0; k < indiceEtoilelumin.length; k++){
                    List<Double> x_arrayTemp = new ArrayList<>();
                    List<Double> y_arrayTemp = new ArrayList<>();
                    int temptrouve = trouver_constellation(list_template.get(j), x_arrayDetect, y_arrayDetect, indiceEtoilelumin[i], indiceEtoilelumin[k],echelle, x_arrayTemp,y_arrayTemp);
                    //Log.d(TAG, "compareConstellation: nbStar trouve : " + temptrouve);
                    if(temptrouve >= list_template.get(j).getNbOfStars()/2){
                        if(temptrouve > trouve){
                            trouve = temptrouve;
                            indiceEtoileLum0 = indiceEtoilelumin[i];
                            indiceEtoileLum1 = indiceEtoilelumin[k];
                            x_arrayTempRot = x_arrayTemp;
                            y_arrayTempRot = y_arrayTemp;
                        }
                    }
                }
            }

            if(trouve >= list_template.get(j).getNbOfStars()/2){
                constellation_candidat.add(new Constellation_Candidat(list_template.get(j),indiceEtoileLum0,indiceEtoileLum1,trouve,echelle,x_arrayTempRot,y_arrayTempRot));
            }
        }

        constellation_candidat.sort((c1,c2)->{
            if(c1.getNombreEtoile_detecté() > c2.getNombreEtoile_detecté()) return -1;
            else if (c1.getNombreEtoile_detecté() < c2.getNombreEtoile_detecté()) return 1;
            else return 0;
        });

        for(int i = 0 ; i < constellation_candidat.size();i++){
            Constellation_Candidat cc = constellation_candidat.get(i);
            Log.d(TAG, "compareConstellation: " + cc.getName() + " nombre d'étoile trouvé : " + cc.getNombreEtoile_detecté() + " i : " + cc.getIndiceEtoile0() + "k : " + cc.getIndiceEtoile1());
        }
        return constellation_candidat;
    }

    /**
     * Cette fonction est appelée une fois que l'angle de rotation a été calculé, on fait pivoter les données des templates.
     * @param angle angle de rotation déterminé par la fonction de rotation
     * @revenir
     */
    public static void rotateTemplates(Constellation_Astro constellation_astro, double angle, List<Double> x_arrayTemp,List<Double> y_arrayTemp) {
        double xprime ;
        double yprime ;

        for (int i = 0; i < constellation_astro.getNbOfStars(); i++) {
            // Calcul de la matrice de rotation : calcul des valeurs de rotation pour chaque coordonnées d'étoile de template
            xprime = ((constellation_astro.getEtoile_x_array().get(i) * Math.cos(angle)) - (constellation_astro.getEtoile_y_array().get(i)  * Math.sin(angle)));
            yprime = ((constellation_astro.getEtoile_x_array().get(i) * (Math.sin(angle))) + (constellation_astro.getEtoile_y_array().get(i)   * Math.cos(angle)));
            x_arrayTemp.set(i,xprime);
            y_arrayTemp.set(i,yprime);
        }
    }

    /**
     * Cette fonction permet d'effectuer une rotation des points dans un plan
     * @param nbStars
     * @param angle
     * @param x_arrayTemp
     * @param y_arrayTemp
     */
    public static void rotateTemplates(int nbStars,double angle, List<Double> x_arrayTemp,List<Double> y_arrayTemp){
        double xprime ;
        double yprime ;

        for (int i = 0; i < nbStars; i++) {
            // Calcul de la matrice de rotation : calcul des valeurs de rotation pour chaque coordonnées d'étoile de template
            xprime = ((x_arrayTemp.get(i) * Math.cos(angle)) - (y_arrayTemp.get(i) * Math.sin(angle)));
            yprime = ((x_arrayTemp.get(i) * (Math.sin(angle))) + (y_arrayTemp.get(i) * Math.cos(angle)));
            x_arrayTemp.set(i,xprime);
            y_arrayTemp.set(i,yprime);
        }
    }

    public Mat binariser(Mat mat,int tresh){
        Mat image_dest = new Mat();
        Imgproc.threshold(mat, image_dest, tresh, 255, Imgproc.THRESH_BINARY);
        return image_dest;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void toMemeEchelle(List<Double> x_array, List<Double> y_array, double echelle){
        //Log.d(TAG, "toMemeEchelle: xArrayA" + x_array.toString());

        for (int i = 0; i <x_array.size() ; i++) {
            x_array.set(i, x_array.get(i)/echelle);
            y_array.set(i, y_array.get(i)/echelle);
        }

        //Log.d(TAG, "toMemeEchelle: xArrayApres : " + x_array.toString());
    }

    public static void alignerToCoordonnes(List<Double> x_array, List<Double> y_array, double xUser, double yUser,double xTempRot, double yTempRot){
        double xVec = xUser - xTempRot;
        double yVec = yUser - yTempRot;

        //Log.d(TAG, "alignerToCoordonnes: xVec : " + xVec + ", yVec : " + yVec);

        for(int i = 0; i < x_array.size(); i++){
            x_array.set(i, (x_array.get(i) + xVec));
            y_array.set(i, (y_array.get(i)) + yVec);
        }

        //Log.d(TAG, "rotateTemplates: xArrayTempRot : " + x_array.toString() + "Size : " + x_array.size());
        //Log.d(TAG, "rotateTemplates: yArrayTempRot : " + y_array.toString() + "Size : " + y_array.size());
    }

    public int checkEtoileMatch(List<Double> x_arrayTemp,List<Double> y_arrayTemp,List<Double> x_array,List<Double> y_array,double test_scale){
        int match = 0;
        double distance = 0;
        double check = 0.155/test_scale;

        List<Double> x_arrayTempltem = new ArrayList<>(x_arrayTemp);
        List<Double> y_arrayTempltem = new ArrayList<>(y_arrayTemp);

        List<Double> x_arraytem = new ArrayList<>(x_array);
        List<Double> y_arraytem = new ArrayList<>(y_array);

        int size = x_arrayTempltem.size();
        int size1 = x_arraytem.size();

        //Log.d(TAG, "checkEtoileMatch: size xArrayTempltemSize : " + size + " : xArraytemSize : " + size1);

        for(int i = 0; i < size; i++){
            outerloop:
            for(int j = 0; j < size1; j++){
                distance = Math.sqrt(Math.pow(x_arraytem.get(j) - x_arrayTempltem.get(i),2) + Math.pow(y_arraytem.get(j) - y_arrayTempltem.get(i),2));

                if(distance <= check){
                    //Imgproc.line(image,new Point(x_arraytem.get(j)*100,-y_arraytem.get(j)*100),new Point(x_arrayTempltem.get(i)*100,-y_arrayTempltem.get(i)*100),new Scalar(255,0,0),2);
                    match++;
                    x_arraytem.remove(j);
                    y_arraytem.remove(j);
                    size1 = x_arraytem.size();
                    break outerloop;
                }
            }
        }
        return match;

    }

    public double calculeAngle(double udx, double udy,double vdx, double vdy){
        double uv = udx * vdx + udy * vdy;

        double normeU = Math.sqrt(Math.pow(udx,2) + Math.pow(udy,2));
        double normeV = Math.sqrt(Math.pow(vdx,2) + Math.pow(vdy,2));

        double cosT = uv / (normeU * normeV);

        double angle = Math.acos(cosT);

//      Log.d(TAG, "calculeAngle: udx :" + udx);
//      Log.d(TAG, "calculeAngle: udy :" + udy);
//
//      Log.d(TAG, "calculeAngle: vdx :" + vdx);
//      Log.d(TAG, "calculeAngle: vdy :" + vdy);
//
//      Log.d(TAG, "calculeAngle: udy - vdy " + (udy - vdy));

        if((udx > 0 && udy < 0) || (udx < 0 && udy < 0)){
            angle = angle*-1;
        }

//      Log.d(TAG, "calculeAngle: angle : " + Math.toDegrees(angle));

        return angle;
    }

    @Override
    public void run() {
        List<Constellation_Candidat> listConsCan = compareConstellation();
        if (!listConsCan.isEmpty()) Fragment_AstroAffichagePhoto.detectConstellation = compareConstellation().get(0);
        else Fragment_AstroAffichagePhoto.detectConstellation = null;
    }

    public static void initTemp(int nbStar,List<Double> x_arrayTempRot,List<Double> y_arrayTempRot){
        for (int i = 0; i < nbStar; i++) {
            x_arrayTempRot.add(i,0.0);
            y_arrayTempRot.add(i,0.0);
        }
    }
}


