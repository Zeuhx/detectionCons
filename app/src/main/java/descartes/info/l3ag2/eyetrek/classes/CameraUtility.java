package descartes.info.l3ag2.eyetrek.classes;

import android.animation.ObjectAnimator;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;

/**
 * Created by Dorian QUABOUL
 * <p>
 * Sources :
 * <p>
 * Android Camera2 API Video App - https://youtu.be/CuvVpsFc77w
 * Chaîne : Mobile Application Tutorials
 * Vidéos PART 1 à PART10
 * ---------------------------------------------
 */

public class CameraUtility {

    /**
     * On construit un tableau qui associe un entier (clé) à un autre entier (valeur)
     * SparseIntArray est l'équivalent d'une HashMap avec un gain au niveau de la mémoire
     *
     * Source -> https://developer.android.com/reference/android/util/SparseIntArray.html
     */

    public static final SparseIntArray ORIENTATIONS_ANGLE_CAMERA = new SparseIntArray();

    /**
     * REQUETES : correspond aux differents numeros des requetes
     */
    public static final int REQUETE_GALERY_ACCESS = 1; // de base c'était 79
    /**
     * Etat: Aperçu de la camera
     */
    public static final int ETAT_APERCU_CAM = 0;

    /**
     * Etat : Attente du blocage du focus
     */
    public static final int ETAT_ATTENTE_LOCK_FOCUS = 1;

    /**
     * Etat : Attente de la precapture
     */
    public static final int ETAT_ATTENTE_PRECAPTURE = 2;

    /**
     * Etat : Attente d'un autre etat que la precapture
     */
    public static final int ETAT_ATTENTE_NO_PRECAPTURE = 3;

    /**
     * Etat : Image capturée
     */
    public static final int ETAT_IMAGE_PRISE = 4;

    /**
     * Initialisation du tableau des angles d'orientations
     */
    static {
        ORIENTATIONS_ANGLE_CAMERA.append(Surface.ROTATION_0, 90);
        ORIENTATIONS_ANGLE_CAMERA.append(Surface.ROTATION_90, 0);
        ORIENTATIONS_ANGLE_CAMERA.append(Surface.ROTATION_180, 270);
        ORIENTATIONS_ANGLE_CAMERA.append(Surface.ROTATION_270, 180);
    }

    /**
     *
     * @param c
     * @param orientationAppareil
     * @return
     */
    /*public static int rotationAppareil(CameraCharacteristics c, int orientationAppareil) {
        int orientationCapteur = c.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int res = (orientationCapteur + ORIENTATIONS_ANGLE_CAMERA.get(orientationAppareil) + 270) % 360;
        return res;
    }*/

    /**
     * Permet de configurer le flash en mode auto dans une capture request qui sera ajoutée dans
     * le constructeur de requête pour la capture d'une image.
     * @param requestBuilder
     * @param isFlashSupported
     */
    public static void setAutoFlash(CaptureRequest.Builder requestBuilder, boolean isFlashSupported) {
        if (isFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Permet d'effectuer une rotation de 180 degrés de la viewToFlip sur l'axe des ordonnées
     *
     * @source : http://www.edumobile.org/android/flip-your-viewsimage-buttontext-etc/
     * @param viewToFlip view à retourner
     */
    public static void flipIt(View viewToFlip) {
        //rotation = 180 degres
        ObjectAnimator flip = ObjectAnimator.ofFloat(viewToFlip, "rotationY", 0f, 180f);
        //duree = 700 millisecondes
        flip.setDuration(700);
        flip.start();
    }

    /**
     *
     * @param choix les choix de tailles disponibles sur l'appareil
     * @return la taille qui respecte les conditions définies c'est à dire une largeur <= 1080
     * et ayant un format 4/3 ("format carré" selon https://fr.wikipedia.org/wiki/Format_4/3)
     */
    public static Size chooseVideoSize(Size[] choix) {
        for(Size size : choix) {
            if(size.getWidth() == size.getHeight() * 4/3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choix[choix.length-1];
    }
}
