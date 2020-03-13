package descartes.info.l3ag2.eyetrek.classes;

import android.media.Image;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Dorian QUABOUL.
 *
 * Cette classe permet de sauvegarder une image dans un fichier.
 * Cette tâche de sauvegarde est faite en arrière plan.
 *
 * Source : https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java
 */

public class ImageSauvegarde implements Runnable{

    //Une image au format JPEG
    private Image image;

    //Le chemin dans lequel se trouve l'image
    private File cheminFichier;

    public ImageSauvegarde(Image image, File cheminFichier) {
        Log.e("ImageSauvegarde : ", "Appel du constructeur de ImageSauvegarde");
        // Est appelé seulement lorsqu'on prend une photo
        this.image = image;
        this.cheminFichier = cheminFichier;
    }

    @Override
    public void run() {
        //on récupère les bytes contenus dans l'image et on les insère dans le buffer
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        //on crée un tableau de taille égale au nombre de bytes contenu dans le buffer
        byte[] tabImage = new byte[buffer.remaining()];

        //On copie le contenu du buffer dans le tableau
        buffer.get(tabImage);

        try (FileOutputStream fos = new FileOutputStream(cheminFichier)) {
            //on écrit les bytes de l'image dans le fichier
            Log.e("ImageSauvegarde : ", "[path to new image] " + cheminFichier.getPath());
            // Est appelé seulement lorsqu'on prend une photo
            fos.write(tabImage);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            image.close();
        }
    }
}
