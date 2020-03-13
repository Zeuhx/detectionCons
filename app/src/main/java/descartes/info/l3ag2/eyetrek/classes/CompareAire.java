package descartes.info.l3ag2.eyetrek.classes;

import android.util.Size;

import java.util.Comparator;

/**
 * Created by Dorian QUABOUL
 */

public class CompareAire implements Comparator<Size> {

    /**
     * Une classe qui implémente l'interface Comparator. Elle permet de comparer deux tailles
     * à partir de leur aire (longueur*hauteur).
     * - si aire_s1 - aire_s2 < 0 alors s1 est plus petit que s2
     * - si aire_s1 - aire_s2 > 0 alors s1 est plus grand que s2
     * - si aire_s1 - aire_s2 = 0 alors s1 est égale à s2
     */
    @Override
    public int compare(Size s1, Size s2) {
            /*
                Si la valeur de la différence des 2 aires est négative, on renvoie -1
                Si la valeur de la différence des 2 aires est positive, on renvoie +1
                Si la valeur de la différence des 2 aires est nulle, on renvoie 0
             */
        return Long.signum((long) s1.getWidth() * s1.getHeight() - (long) s2.getWidth() * s2.getHeight());
    }
}
