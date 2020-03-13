package descartes.info.l3ag2.eyetrek.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.UtilAnalyseImage_Astro;
import descartes.info.l3ag2.eyetrek.interfaces.IOnBackPressed;

public class Fragment_AstroAffichagePhoto extends Fragment implements IOnBackPressed {
    private ImageView mImageView = null ;
    private static final String TAG = "Fragment_AstroAffichage";

    /**
     * Creer une nouvelle instance de Fragment_AstroAffichagePhoto
     * initialise a l'affichage du texte "index"
     */
    public static Fragment_AstroAffichagePhoto newInstance(Bitmap bitmap) {
        Fragment_AstroAffichagePhoto fragment = new Fragment_AstroAffichagePhoto();

        // Convertit en byte array pour pouvoir le passer en parametre dans le fragment
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Bundle b = new Bundle();
        b.putByteArray("image",byteArray);

        // Fournir une entrée d'index comme argument.
        fragment.setArguments(b);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Affichage de la vue
        View view = inflater.inflate(R.layout.mode_astro_display_image, container, false);

        // Flux de sortie du byteArray
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Recupere le byte array --> pour reconvertir en bitmap
        byte[] byteArray = getArguments().getByteArray("image");
        long bitmapLength = (byteArray.length)/1024;

        // Pour transformer en bitmap depuis l'image
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        Log.d(TAG, "onCreateView: avant compression de l'image " + bitmapLength);
        if(bitmapLength > 10){
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
            Log.d(TAG, "onCreateView: apres compression 1 " + bitmap.getByteCount());
            if(bitmapLength > 50){
                bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
               // bitmapLength = bitmap.getDensity();
                Log.d(TAG, "onCreateView: apres compression 2 " + bitmapLength);
                if(bitmapLength > 100) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
                    //bitmapLength = bitmap.getDensity();
                    Log.d(TAG, "onCreateView: apres compression 3 " + bitmapLength);
                }
            }
            byteArray = stream.toByteArray();
        }

        // On dit que c'est une instance (unique)
        UtilAnalyseImage_Astro utilAnalyseImage_astro = UtilAnalyseImage_Astro.getInstance();

        // On affecte la bitmap (donc la photo) et on analyse
        utilAnalyseImage_astro.setBitmap(bitmap);
        utilAnalyseImage_astro.starAnalyse();

        // Recupere la ressource depuis le FragmentMenuAstro
        mImageView = view.findViewById(R.id.affichage_image_astro);

        mImageView.setImageBitmap(bitmap);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
