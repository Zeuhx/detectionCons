package descartes.info.l3ag2.eyetrek.classes;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dorian Quaboul
 * Updated by Jeremy Meynadier
 */
public class FeatureExtraction {

    static {
        System.loadLibrary("opencv_java3");
    }
    /**
     * distancesMapY - Permet le calcul des distances entre chaque point de l'objet (horizontalement)
     * @param img l'image que l'on veut traiter
     * @param gap l'écart entre les points séléctionnés
     * @return distances, la liste des distances en Y
     */
    public static List <Double> distanceMapsX(Mat img, int gap, boolean reverse){

        //Permet d'éviter une boucle infinie
        if (gap <= 0){
            gap = 1;
        }

        int limite = 14;

        double colorleaf = reverse ? 0 : 255;
        double colorfont = reverse ? 255 : 0;

        double caseNbfont=0;

        double result =0;

        //Liste des distances entre 2 points alignés horizontalements
        List <Double> distances= new ArrayList<>();

        //Booleen permettant de savoir si l'on est dans la feuille
        boolean leaf = false;

        //Coordonnée minimal lorsque l'on rentre dans la feuille
        double coordFirst=0;

        gap = (img.width()/gap);

        //Boucle permettant de circuler dans l'image -> parcours des lignes
        for (int axeY = 0 ; axeY<img.size().width; axeY+=gap ){

            for (int axeX = 0; axeX <img.size().height ; axeX++ ){
                //N'est vrai que lorsque l'on rentre pour la première fois dans la feuille


                if (img.get(axeX, axeY)[0] == colorfont)
                    caseNbfont++;

                if (axeX == (img.height() - 1)) {
                    result = Math.abs((img.size().height - caseNbfont) / img.size().height);
                    distances.add(caseNbfont == 0 ? (double) 0 : result);
                    caseNbfont=0;
                }



               /* if((img.get(axeX,axeY)[0] == colorleaf) && !leaf){
                    leaf=true;
                    //Sauvegarde la coordonnée Y d'entrée
                    coordFirst=axeX;
                }else if(axeX == (img.height())){
                    distances.add((double)(0));
                    break;
                }else if(leaf && (img.get(axeX,axeY)[0] == colorfont)) {
                    leaf = false;
                    //Ajoute à la liste la distance calcule entre min de la feuille et les coordonnées de sortie
                    distances.add(Math.abs(axeX - coordFirst) / img.size().height);
                    break;
                }*/
            }
        }

        for ( int round = distances.size(); round < limite ; round++){
            distances.add((double)0);
        }
        return distances;

    }



    /**
     * distancesMapX - Permet le calcul des distances entre chaque point de l'objet (verticalement)
     * @param img l'image que l'on veut traiter
     * @param gap l'écart entre les points séléctionnés
     * @return distances, la liste des distances en Y
     */
    public static List<Double> distanceMapsY(Mat img, int gap, boolean reverse){

        //Permet d'éviter une boucle infinie
        if (gap <= 0){
            gap = 1;
        }


        double colorleaf = reverse ? 0 : 255;
        double colorfont = reverse ? 255 : 0;



        //Liste des distances entre 2 points alignés horizontalements
        List <Double> distances=new ArrayList<>();


        //Coordonnée minimal lorsque l'on rentre dans la feuille
        double caseNbfont=0;

        double result =0;


        gap = (img.height()/gap);

        //Boucle permettant de circuler dans l'image -> parcours des colonnes
        for (int axeX = 0 ; axeX<img.height() ; axeX+=gap ) {

            for (int axeY = 0; axeY < img.size().width; axeY++) {

                if (img.get(axeX, axeY)[0] == colorfont)
                    caseNbfont++;

                if (axeY == (img.width() - 1)) {
                    result = Math.abs((img.size().width - caseNbfont) / img.size().width);
                    distances.add(caseNbfont == 0 ? (double) 0 : result);
                    caseNbfont=0;
                }
            }

                /*//N'est vrai que lorsque l'on rentre pour la première fois dans la feuille
                if((img.get(axeX,axeY)[0] == colorleaf && !leaf)){
                    leaf=true;
                    //Sauvegarde la coordonnée Y d'entrée
                    coordFirst=axeY;
                }else if(leaf && (img.get(axeX,axeY)[0] == colorfont)){
                    System.out.println("ok");
                    leaf=false;
                    //Ajoute à la liste la distance calcule entre min de la feuille et les coordonnées de sortie
                    distances.add(Math.abs(axeY-coordFirst)/img.size().width);
                    break;
                }else if(axeY == img.width()-1 && !leaf){
                    System.out.println("0");
                    distances.add((double)(0));
                    break;
                }*/
        }

        int limite = 14;
        for (int round = distances.size(); round < limite ; round++){
            distances.add((double)0);
        }

        return distances;
    }

    /**
     * distancesMapCentroid - Calcule de la distance radial (centre)
     * @param img l'image que l'on veut traiter
     * @param gap l'écart entre les points séléctionnés
     * @return distances, la liste des distances en X
     */
    /**
     * Retourne les 16 points du rectangle de délimitation.
     * Les points sont espacés avec un certain ratio (à équi-distance).
     *
     * source : https://brilliant.org/wiki/section-formula/
     *
     * @param rect rectangle de délimitation
     * @return liste des 16 points sur le rectangle de délimitation
     */
    private static List<Point> getPointArroundBox(RotatedRect rect) {
        //On récupère les 4 sommets du rectangle de délimitation
        Point[] sommetsRect = new Point[4];
        rect.points(sommetsRect);

        //on créé une liste qui va contenir les 16 points autour du rectangle
        List<Point> listPointBox = new ArrayList<>();

        Point A = sommetsRect[0];
        Point B = sommetsRect[1];
        Point C = sommetsRect[2];
        Point D = sommetsRect[3];

        //On ajoute les 4 sommets du rectangle à la liste
        listPointBox.add(A);
        listPointBox.add(B);
        listPointBox.add(C);
        listPointBox.add(D);

        /**
         * On veut que les points respectent un espacement régulier :
         *
         *     m        n
         *  A+----|----------+B
         *
         * pour le 1er point : on veut qu'il occupe 1/4 de la place (pour m) et 3/4 pour le reste (pour n)
         * pour le 2e point : on veut qu'il occupe 2/4 de la place (pour m) et 2/4 pour le reste (pour n)
         * pour le 3e point : on veut qu'il occupe 3/4 de la place (pour m)et 1/4 pour le reste (pour n)
         *
         * On fait ça pour chaque arête du rectangle de délimitation.
         */
        for(int m = 1, n = 3; m<=3 && n>=1; m++, n--) {
            // Points sur l'arête AB (au total 3)
            double xAB = ((m * B.x) + (n * A.x)) / (m+n);
            double yAB = ((m * B.y) + (n * A.y)) / (m+n);

            listPointBox.add(new Point(xAB,yAB));

            //Points sur l'arête BC (au total 3)
            double xBC = ((m * C.x) + (n * B.x)) / (m+n);
            double yBC = ((m * C.y) + (n * B.y)) / (m+n);

            listPointBox.add(new Point(xBC,yBC));

            //Points sur l'arête DC (au total 3)
            double xDC = ((m * C.x) + (n * D.x)) / (m+n);
            double yDC = ((m * C.y) + (n * D.y)) / (m+n);

            listPointBox.add(new Point(xDC,yDC));

            //Points sur l'arête AD (au total 3)
            double xAD = ((m * D.x) + (n * A.x)) / (m+n);
            double yAD = ((m * D.y) + (n * A.y)) / (m+n);

            listPointBox.add(new Point(xAD,yAD));
        }

        return listPointBox;
    }

    /**
     *
     * Détermine l'angle d'une ligne droite entre 2 points.
     * L'angle retourné est en degrés
     *
     * Source : http://wikicode.wikidot.com/get-angle-of-line-between-two-points
     *
     * @param p1 1er point appartenant à la ligne
     * @param p2 2e point appartenant à la ligne
     * @return
     */
    private static double getAngleOfLineBetweenTwoPoints(Point p1, Point p2) {
        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;

        return Math.toDegrees(Math.atan2(yDiff, xDiff));
    }


    /**
     * distance - https://stackoverflow.com/questions/13318733/get-closest-value-to-a-number-in-array
     * raisonnment - https://fr.mathworks.com/matlabcentral/answers/105607-radially-divide-binary-image-from-centroid-at-equal-angles-and-find-the-radial-distance?requestedDomain=true
     * @param rectBoundary rectangle de délimitation
     * @param contour points appartenant au contour
     * @param centroid
     * @return liste des points qui ont le même angle que les lignes des 16 points
     */
    private static List<Point> getPointIntersectContour(List<Point> rectBoundary, MatOfPoint contour, Point centroid) {
        //Points correspondant au contour
        Point[] pointContour = contour.toArray();

        //Angles des lignes passant par les 16 points du rectangle de délimitation et le centroide.
        List<Double> angleLineRectToCenter = new ArrayList<>();
        //Angles des lignes passant par les points du contour et le centroide.
        List<Double> angleLineContourToCenter = new ArrayList<>();
        /*
            Liste des points du contour pour lesquels la ligne passant par ces points et le centroide
            a le meme angle qu'une des lignes passant par l'un des 16 points du rectangle et le centroide
         */
        List<Point> intersectPoint = new ArrayList<>();

        //on récupère les angles des lignes passant par les 16 points et le centroide
        for(Point pt : rectBoundary) {
            double angle = getAngleOfLineBetweenTwoPoints(centroid,pt);
            angleLineRectToCenter.add(angle);
        }

        //On récupère les angles des lignes passant par les points du contour et le centroide
        for(Point pt : pointContour) {
            double angle = getAngleOfLineBetweenTwoPoints(centroid,pt);
            angleLineContourToCenter.add(angle);
        }

        /*
            On cherche les points du contour qui ont l'angle qui se rapprochent le
            plus d'une des lignes reliant les points du rectangle de délimitation
            et le centroide.
         */
        for(int i = 0; i<angleLineRectToCenter.size(); i++) {
            //L'angle à rechercher
            double val = angleLineRectToCenter.get(i);
            //On cherche l'écart minimal entre les angles des contours et l'angle recherché.
            double distMin = Math.abs(angleLineContourToCenter.get(0)-val);
            int id = 0;

            for(int j = 0; j<angleLineContourToCenter.size(); j++) {
                double distCourante = Math.abs(angleLineContourToCenter.get(j)-val);

                if(distCourante < distMin) {
                    //on stocke l'indice de l'angle qui a l'écart minimal entre son angle et l'angle recherché.
                    id = j;
                    //on actualise l'écart minimal
                    distMin = distCourante;
                }
            }

            intersectPoint.add(pointContour[id]);
        }

        return intersectPoint;
    }

    /**
     * Retourne la liste des distances entre les points de la liste et le centroide
     *
     * @param listPoint liste des points
     * @param centroid point correspondant au centroide
     * @return liste contenant les distances entre les points listPoint et le centroid
     */
    private static List<Double> getDistanceFromPointToCentroid(List<Point> listPoint, Point centroid) {
        List<Double> distances = new ArrayList<>();

        for(Point pt : listPoint) {
            distances.add(distanceEuclidienne(pt, centroid));
        }

        return distances;
    }

    /**
     * Calcul de la distance euclidienne entre 2 points
     */
    private static double distanceEuclidienne(Point p1, Point p2) {
        return (Math.sqrt(Math.pow(p2.x - p1.x,2)+ Math.pow(p2.y - p1.y,2)));
    }

    /**
     * 3.2.6 Edge extraction : Suzuki's Algorithm
     * Source - https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html
     *
     * @param image
     * @return
     */
    public static List<MatOfPoint> edgeExtraction(Mat image) {
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(image,contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours;
    }

    /**
     * 3.2.7 Edge filtering
     * Les contours avec des petites longueurs (comparés avec le plus grand contour) sont éliminés.
     *
     * @param contours
     * @return le plus grand contour
     */
    public static MatOfPoint edgeFiltering(List<MatOfPoint> contours) {
        MatOfPoint maxPointCoutours = contours.get(0);

        for(MatOfPoint contour : contours) {
            if(contour.rows() > maxPointCoutours.rows()) {
                maxPointCoutours = contour;
            }
        }

        return maxPointCoutours;
    }

    /**
     * Détermine les indices des points correspondant au contour qui forment l'enveloppe convexe
     * (Convex Hull)
     *
     * @param contour
     * @return
     */
    public static MatOfInt convexHull(MatOfPoint contour) {
        MatOfInt convexHull = new MatOfInt();

        Imgproc.convexHull(contour,convexHull);

        return convexHull;
    }

    /**
     * Permet d'extraire les contours inclus dans l'enveloppe convexe (Convex Hull)
     *
     * Source : https://stackoverflow.com/questions/22207322/convert-matofint-to-matofpoint
     *
     * @param contour
     * @param indexes
     * @return
     */
    public static MatOfPoint convertIndexesToPoints(MatOfPoint contour, MatOfInt indexes) {
        int[] arrIndex = indexes.toArray();
        Point[] arrContour = contour.toArray();
        Point[] arrPoints = new Point[arrIndex.length];

        for (int i=0;i<arrIndex.length;i++) {
            arrPoints[i] = arrContour[arrIndex[i]];
        }

        MatOfPoint hull = new MatOfPoint();
        hull.fromArray(arrPoints);
        return hull;
    }

    /**
     * Calcul de l'aire de l'enveloppe convexe (Convex Hull)
     * @param convexHullPoint
     * @return
     */
    public static double calculateConvexHullArea(MatOfPoint convexHullPoint) {
        return Imgproc.contourArea(convexHullPoint);
    }

    /**
     * Calcul du périmètre de l'enveloppe convexe (Convex Hull)
     * @param convexHullPoint
     * @return
     */
    public static double calculateConvexHullPerimeter(MatOfPoint convexHullPoint) {
        return Imgproc.arcLength(new MatOfPoint2f(convexHullPoint.toArray()),true);
    }

    /**
     * Calcul de l'aire en fonction des contours
     * @param contour
     * @return
     */
    public static double calculateContourArea(MatOfPoint contour) {
        return Imgproc.contourArea(contour);
    }

    /**
     * Calcul du périmètre en fonction des contours
     * @param contour
     * @return
     */
    public static double calculateContourPerimeter(MatOfPoint contour) {
        return Imgproc.arcLength(new MatOfPoint2f(contour.toArray()),true);
    }


    /**
     *  Cela donne une indication sur la forme de l'objet.
     *  Les cercles ont le plus grand ratio (aire/perimetre)
     *  et cette formule atteint une valeur de 1 pour un cercle parfait.
     *  Les carrés ont une valeur de 0.78 environ.
     *  Pour les objets les plus fins la valeur est proche de 0.
     *
     * @param contour
     * @return le ratio aire périmètre
     */

    public static double calculateShapeFactor(MatOfPoint contour) {
        double aire = calculateContourArea(contour);
        double perimetre = calculateContourPerimeter(contour);

        return (4 * Math.PI * aire) / (Math.pow(perimetre,2));
    }


    /**
     *  La réciproque du shape factor
     *
     * @param contour
     * @return le ratio aire périmètre
     */

    public static double calculateRoundness(MatOfPoint contour) {
        double aire = calculateContourArea(contour);
        double perimetre = calculateContourPerimeter(contour);

        return (Math.pow(perimetre,2)) / (4 * Math.PI * aire);
    }

    /**
     * Calcul la dispersion ou irrégularité qui est une caractéristique permettant
     * de savoir si un objet a une forme irrégulière.
     * @param centroid
     * @param contour
     * @return
     */
    public static double calculateDispersion(Point centroid, MatOfPoint contour) {
        Point[] tabContour = contour.toArray();

        double xCentroid = centroid.x;
        double yCentroid = centroid.y;

        double max = Math.sqrt(Math.pow(tabContour[0].x - xCentroid,2) + Math.pow(tabContour[0].y - yCentroid,2));
        double min = Math.sqrt(Math.pow(tabContour[0].x - xCentroid,2) + Math.pow(tabContour[0].y - yCentroid,2));

        for(int i = 1; i<tabContour.length; i++) {
            double diff = Math.sqrt(Math.pow(tabContour[i].x - xCentroid,2) + Math.pow(tabContour[i].y - yCentroid,2));

            if(diff > max) {
                max = diff;
            }

            if(diff < min) {
                min = diff;
            }
        }

        return max/min;
    }

    /**
     * Permet de retourner les moments d'une image qui sont des poids particuliers dans une
     * image.
     * https://docs.opencv.org/3.4.0/dd/d49/tutorial_py_contour_features.html
     * @param contour
     * @return moments d'une image
     */
    public static Moments getMomentsFromContours(MatOfPoint contour) {
        Moments moments = Imgproc.moments(contour);

        return moments;
    }

    /**
     * Détermine le point centroide d'un objet.
     * @param moments
     * @return le point correspondant au centroïde de l'objet
     */
    public static Point getCentroid(Moments moments) {

        double centroidX = moments.get_m10()/moments.get_m00();
        double centroidY = moments.get_m01()/moments.get_m00();

        //new Point(double x, double y)
        Point centroid = new Point(centroidX,centroidY);

        return centroid;
    }

    /**
     * Détermine le rectangle de délimitation encerclant l'objet et possédant une aire minimale.
     *
     * @param contour de la feuille
     * @return la liste des points correspondant au rectangle de délimitation
     */
    public static RotatedRect getBoundaryBox(MatOfPoint contour) {
        RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));

        return box;
    }

    /**
     * Retourne la hauteur du rectangle de délimitation entourant l'objet.
     * @param box
     * @return hauteur du rectangle
     */
    public static double getHeightBox(RotatedRect box) {
        return box.size.height;
    }

    /**
     * Retourne la largeur du rectangle de délimitation entourant l'objet.
     * @param box
     * @return largeur du rectangle
     */
    public static double getWidthBox(RotatedRect box) {
        return box.size.width;
    }


    /**
     * Normalisation des données pour éviter qu'il y ait trop de donénes qui se dispersent.
     * Les nouvelles valeurs seront comprises entre 0 et 1.
     *
     * @return liste des valeurs normalisées correspondant aux caractéristiques
     */
    public static double normalization(double min, double max, double val) {

       return (val - min)/(max - min);

    }

    /**
     *
     * On détermine le centre et le rayon du cercle qui entoure le contour
     * de la feuille.
     * On détermine la liste des  points situés tout autour du cercle et
     * espacés de 10 degrés chacun.
     * On part du point situé à l'angle 0, c'est a dire celui situé en bas verticalement
     * sur le cercle.
     */
    private static List<Point> getPointsCercle(MatOfPoint contour) {
        //le rayon du cercle
        float[] radius = new float[1];
        //centre du cercle
        Point center = new Point();

        Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()),center,radius);

        List<Point> pointCircle = new ArrayList<>();

        //on commence à l'angle 0, c'est à dire en bas verticalement.
        //on récupère 36 points autour du cercle
        for(int deg = 0; deg<360; deg += 10) {

            double sin = Math.sin((double)deg * Math.PI/(double)180);
            double cos = Math.cos((double)deg * Math.PI/(double)180);

            Point p = new Point(center.x+sin*radius[0], center.y+cos*radius[0]);
            pointCircle.add(p);
        }

        return  pointCircle;
    }

    /**
     *
     * On détermine la liste des angles entre un point et une liste de
     * de points.
     *
     */
    private static List<Double> getListAngle(Point point, List<Point> list) {
        List<Double> listAngles = new ArrayList<>();

        for (Point pt : list) {
            double angle = getAngleOfLineBetweenTwoPoints(point, pt);

            listAngles.add(angle);
        }

        return listAngles;
    }

    public static List<Point> getPointContourCentroidRadial(MatOfPoint contour, Point centroid) {
        float[] radius = new float[1];
        Point center = new Point();

        Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);

        List<Point> pointCircle = getPointsCercle(contour);

        Point[] pointContour = contour.toArray();

        //Angles des lignes passant par les 36 points du cercle de délimitation et le centre du cercle.
        List<Double> angleLineCircleToCenter = getListAngle(center,pointCircle);
        //Angles des lignes passant par les points du contour et le centroide.
        List<Double> angleLineContourToCenter = getListAngle(centroid,contour.toList());
        /*
            Liste des points du contour pour lesquels la ligne passant par ces points et le centroide
            a le meme angle qu'une des lignes passant par l'un des 36 points du cercle et le centre du cercle
         */
        List<Point> intersectPoint = new ArrayList<>();

        /*
            On cherche les points du contour qui ont l'angle qui se rapprochent le
            plus d'une des lignes reliant les points du cercle de délimitation
            et le centre du cercle.
         */
        for (int i = 0; i < angleLineCircleToCenter.size(); i++) {
            //L'angle à rechercher
            double val = angleLineCircleToCenter.get(i);
            //On cherche l'écart minimal entre les angles des contours et l'angle recherché.
            double distMin = Math.abs(angleLineContourToCenter.get(0) - val);
            int id = 0;

            for (int j = 0; j < angleLineContourToCenter.size(); j++) {
                double distCourante = Math.abs(angleLineContourToCenter.get(j) - val);

                if (distCourante < distMin) {
                    //on stocke l'indice de l'angle qui a l'écart minimal entre son angle et l'angle recherché.
                    id = j;
                    //on actualise l'écart minimal
                    distMin = distCourante;
                }
            }
            intersectPoint.add(pointContour[id]);
        }

        return intersectPoint;
    }

    /**
     *
     * Retourne la liste normalisée des distances entre les points du
     * contour et le centroid.
     *
     */
    public static List<Double> preProcessCentroid(List<Point> intersectPoint, Point centroid) {
        List<Double> distance = new ArrayList<>();
        List<Double> normDistance = new ArrayList<>();

        for(Point pt : intersectPoint) {
            distance.add(distanceEuclidienne(pt, centroid));
        }

        //pour normaliser la liste des distances, on récupère la distance max
        // et on divise toutes les distances par cette distance max
        double maxdist = Collections.max(distance);
        for(Double d : distance) {
            normDistance.add(d/maxdist);
        }

        return normDistance;
    }


    public static double aspectRatio( double length, double width, Mat mat) {
        if(mat.size().height > mat.size().width)
            return width / length;
        else
            return length / width;
    }

    public static double whiteAreaRatio(double area, double length, double width) {
        return area / (length * width);
    }

    public static double perimeterToArea(double perimeter, double area) {
        return perimeter / area;
    }

    public static double perimeterToHull(double perimeterHull, double perimeterLeaf) {
        return perimeterHull / perimeterLeaf;
    }

    public static double hullAreaRatio(double areaHull, double areaLeaf) {
        return areaHull / areaLeaf;
    }

    public static double distanceMapX(double distanceX, double length) {
        return distanceX / length;
    }

    public static double distanceMapY(double distanceY, double width) {
        return distanceY / width;
    }

    public static double centroidRadialDistance(double distanceIntersection, double distanceBoundingBox) {
        return distanceIntersection / distanceBoundingBox;
    }

    public static FeaturesVect getFeaturesVect(String cheminImage) {
        FeaturesVect features = new FeaturesVect();

        Mat imageTreated = PreProcessing.preProcess(cheminImage);

        List<MatOfPoint> contours = edgeExtraction(imageTreated);
        MatOfPoint max = edgeFiltering(contours);

        MatOfInt convexHull = convexHull(max);
        MatOfPoint convexHullPoint = convertIndexesToPoints(max,convexHull);

        RotatedRect box = getBoundaryBox(max);

        Moments m = getMomentsFromContours(max);
        Point centroid = getCentroid(m);

        double aire = calculateContourArea(max);
        double perimeter = calculateContourPerimeter(max);
        double perimeterHull = calculateConvexHullPerimeter(convexHullPoint);
        double aireHull = calculateConvexHullArea(convexHullPoint);
        double width = getWidthBox(box);
        double height = getHeightBox(box);

        features.setAspectRatio(aspectRatio(height,width, imageTreated));
        features.setWhiteAreaRatio(whiteAreaRatio(aire,height,width));
        features.setPerimeterToArea(perimeterToArea(perimeter,aire));
        features.setPerimeterToHull(perimeterToHull(perimeterHull,perimeter));
        features.setHullAreaRatio(hullAreaRatio(aireHull,aire));
        features.setDispersion(calculateDispersion(centroid,max));
		features.setShapeFactor(calculateShapeFactor(max));
        features.setRoundness(calculateRoundness(max));
        features.setCentroidRadialDistance(preProcessCentroid(getPointContourCentroidRadial(max, centroid),centroid));

        return features;
    }

    public static void setFeaturesVect(FeaturesVect vect) {
        vect.setAspectRatio(normalization(AlgoKNN.min_aspectRatio, AlgoKNN.max_aspectRatio, vect.getAspectRatio()));
        vect.setWhiteAreaRatio(normalization(AlgoKNN.min_whiteAreaRatio, AlgoKNN.max_whiteAreaRatio, vect.getWhiteAreaRatio()));
        vect.setPerimeterToArea(normalization(AlgoKNN.min_perimeterToArea, AlgoKNN.max_perimeterToArea, vect.getPerimeterToArea()));
        vect.setPerimeterToHull(normalization(AlgoKNN.min_perimeterToHull, AlgoKNN.max_perimeterToHull, vect.getPerimeterToHull()));
        vect.setHullAreaRatio(normalization(AlgoKNN.min_hullAreaRatio, AlgoKNN.max_hullAreaRatio, vect.getHullAreaRatio()));
        vect.setDispersion(normalization(AlgoKNN.min_dispersion, AlgoKNN.max_dispersion, vect.getDispersion()));
        vect.setRoundness(normalization(AlgoKNN.min_roundness, AlgoKNN.max_roundness, vect.getRoundness()));
    }
}
