package descartes.info.l3ag2.eyetrek.classes;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dorian on 07/03/2018.
 *
 * Source : https://gist.github.com/kphilipp/7271334
 *
 * Brute Force Matcher - https://docs.opencv.org/3.0-beta/doc/py_tutorials/py_feature2d/py_matcher/py_matcher.html#matcher
 * ORB - https://docs.opencv.org/3.0-beta/doc/py_tutorials/py_feature2d/py_orb/py_orb.html#orb
 */

public class ORBDescriptor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final double RATIO = 0.65;

    private static KeyPoint[] keyPoint1;
    private static KeyPoint[] keyPoint2;

    /**
     * Filtrage des matches pour ceux respectant le seuil imposé.
     *
     * @param matches
     * @return
     */
    private static List<DMatch> filterDistance(List<DMatch> matches) {
        double somme = 0;

        for(int i = 0; i<matches.size(); i++) {
            somme += matches.get(i).distance;
        }

        double seuilDistance = (somme / matches.size()) * RATIO;

        List<DMatch> selectMatches = new ArrayList<>();

        for(int i = 0; i<matches.size(); i++) {
            if(matches.get(i).distance < seuilDistance) {
                selectMatches.add(matches.get(i));
            }
        }

        return selectMatches;
    }

    private static List<DMatch> filterMatchesSymetric(List<DMatch> matches1, List<DMatch> matches2) {
        List<DMatch> selectMatches = new ArrayList<>();

        for(int i = 0; i<matches1.size(); i++) {
            for(int j = 0; j<matches2.size(); j++) {
                if(keyPoint1[matches1.get(i).queryIdx].equals(keyPoint1[matches2.get(j).trainIdx]) && keyPoint2[matches1.get(i).trainIdx].equals(keyPoint2[matches2.get(j).queryIdx])) {
                    selectMatches.add(matches1.get(i));
                }
            }
        }

        return selectMatches;
    }


    /**
     *
     * @param matches1
     * @param matches2
     * @return
     */
    private static List<DMatch> filterMatches(List<DMatch> matches1, List<DMatch> matches2) {
        matches1 = filterDistance(matches1);
        matches2 = filterDistance(matches2);

        return filterMatchesSymetric(matches1,matches2);
    }

    /**
     * Trie par ordre croissant les distances des matches.
     * @param list
     * @return
     */
    private static List<DMatch> sortMatches(List<DMatch> list) {
        Collections.sort(list, (DMatch dm1, DMatch dm2) -> {
            if(dm1.distance > dm2.distance) {
                return 1;
            }
            else if(dm1.distance < dm2.distance) {
                return -1;
            }
            else {
                return 0;
            }
        });

        return list;
    }

    public static Mat orbDescriptor(Mat image1, Mat image2) {

        //Initialiser l'ORB detector
        ORB orb = ORB.create();

        //Trouver les points clés avec ORB
        MatOfKeyPoint keyPointsImag1 = new MatOfKeyPoint();
        MatOfKeyPoint keyPointsImag2 = new MatOfKeyPoint();

        orb.detect(image1, keyPointsImag1);
        orb.detect(image2, keyPointsImag2);

        keyPoint1 = keyPointsImag1.toArray();
        keyPoint2 = keyPointsImag2.toArray();

        //Compute the descriptors with ORB
        Mat descriptorsImg1 = new Mat();
        Mat descriptorsImg2 = new Mat();

        orb.compute(image1, keyPointsImag1, descriptorsImg1);
        orb.compute(image2, keyPointsImag2, descriptorsImg2);

        BFMatcher matcher = BFMatcher.create(Core.NORM_HAMMING,true);

        MatOfDMatch matches1 = new MatOfDMatch();
        MatOfDMatch matches2 = new MatOfDMatch();

        matcher.match(descriptorsImg1,descriptorsImg2, matches1);
        matcher.match(descriptorsImg2,descriptorsImg1, matches2);

        List<DMatch> filterMatches = filterMatches(sortMatches(matches1.toList()),sortMatches(matches2.toList()));

        MatOfDMatch selectMatches = new MatOfDMatch();
        selectMatches.fromList(filterMatches);


        Mat dest = new Mat(image1.rows(), image1.cols(), image1.type());
        Features2d.drawMatches(image1, keyPointsImag1, image2, keyPointsImag2, selectMatches, dest);

        return dest;
    }
}
