package descartes.info.l3ag2.eyetrek.fragment;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.AstroActivity;
import descartes.info.l3ag2.eyetrek.classes.Constellation_Candidat;
import descartes.info.l3ag2.eyetrek.classes.Drawing_Astro;
import descartes.info.l3ag2.eyetrek.classes.UtilAnalyseImage_Astro;

public class Fragment_AstroAffichagePhoto extends Fragment implements Runnable {

    /**
     * Chaque champ du bottom sheet
     */
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private int idCons = 0;

    boolean isDetected = false ;
    private ImageView mImageView;
    private Toolbar toolbar;
    private Bitmap bitmap;
    private static final String TAG = "Fragment_AstroAffichage";
    private static final String KEY_URI = "URI";

    private CoordinatorLayout coordinatorLayoutdetected_astro;
    private CoordinatorLayout coordinatorLayoutnondetected_astro;
    private Thread analyseEtoile;
    public static Constellation_Candidat detectConstellation;

    /**
     * Creer une nouvelle instance de Fragment_AstroAffichagePhoto
     * initialise a l'affichage du texte "index"
     */ 
    public static Fragment_AstroAffichagePhoto newInstance(Uri bitmap) {
        Fragment_AstroAffichagePhoto fragment = new Fragment_AstroAffichagePhoto();

        Bundle b = new Bundle();
        b.putParcelable(KEY_URI,bitmap);

        fragment.setArguments(b);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Affichage de la vue
        View view = inflater.inflate(R.layout.mode_astro_display_image, container, false);

        toolbar = getActivity().findViewById(R.id.toolBarAstro);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getContext(),R.drawable.ic_arrow_back_black_32dp));
        toolbar.setNavigationOnClickListener((viewl) -> {
            getActivity().onBackPressed();
        });

        /**
         * Si une constellation n'est pas reconnu, on cache le " detected "
         * Sinon on cache le non detected
         */

        Uri uri = getArguments().getParcelable(KEY_URI);
        coordinatorLayoutdetected_astro = view.findViewById(R.id.detected_astro);
        coordinatorLayoutnondetected_astro = view.findViewById(R.id.undetected_astro);
        textView = view.findViewById(R.id.nom_constellation_bottom_sheet);
        textView2 = view.findViewById(R.id.nom_constellation2_bottom_sheet);
        textView3 = view.findViewById(R.id.ascension_droite_bottom_sheet);
        textView4 = view.findViewById(R.id.declinaison_bottom_sheet);
        textView5 = view.findViewById(R.id.etoile_brillante_bottom_sheet);
        textView6 = view.findViewById(R.id.etoile_proche_bottom_sheet);
        //Log.d(TAG, "onCreateView: URI : " + bitmap.toString());

        // On dit que c'est une instance (unique)
        UtilAnalyseImage_Astro utilAnalyseImage_astro = UtilAnalyseImage_Astro.getInstance();
        bitmap = getBitMapresized(uri);


        mImageView = view.findViewById(R.id.affichage_image_astro);
        utilAnalyseImage_astro.setBitmap(bitmap);
        mImageView.setImageBitmap(UtilAnalyseImage_Astro.getInstance().starAnalyse());

        analyseEtoile = new Thread(UtilAnalyseImage_Astro.getInstance());
        analyseEtoile.start();

        Handler handler = new Handler();
        handler.postDelayed(this, 2000);


        return view;
    }

    /**
     * Cette méthode permet d'avoir un bitmap à partir d'une image en prenons compte la densité du téléphone afin de mieux l'exploiter
     * @param uri
     * @return
     */
    public Bitmap getBitMapresized(Uri uri){
        Bitmap bitmapTemp = null;
        Bitmap resizedBitmap = null;

        try {
            bitmapTemp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "getBitMapresized: Le bitmap n'a pas était chargé correctement");
        }

        if(bitmapTemp != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            Bitmap bitmapOrg = new BitmapDrawable(getResources(), bitmapTemp).getBitmap();

            int width = bitmapOrg.getWidth();
            int height = bitmapOrg.getHeight();

            float scaleWidth = metrics.scaledDensity;
            float scaleHeight = metrics.scaledDensity;

            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(scaleWidth, scaleHeight);

            // recreate the new Bitmap
            resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
        }

        return resizedBitmap;
    }

    @Override
    public void run() {

        try {
            analyseEtoile.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(detectConstellation != null){
            idCons = detectConstellation.getId();
            isDetected = true;

            if (detectConstellation.getName().equals("Gémeaux") || detectConstellation.getName().equals("Bouvier")){
                Log.d(TAG, "run: connecxion null ? " + detectConstellation.getConnexion());
                mImageView.setImageBitmap(Drawing_Astro.dessineAsterisme(bitmap,detectConstellation));
            }
        }

        if(!isDetected){
            coordinatorLayoutdetected_astro.setVisibility(View.GONE);
        } else {
            coordinatorLayoutnondetected_astro.setVisibility(View.GONE);
            textView.setText(AstroActivity.dataCons[idCons][0]);
            textView2.setText(AstroActivity.dataCons[idCons][1]);
            textView3.setText(AstroActivity.dataCons[idCons][2]);
            textView4.setText(AstroActivity.dataCons[idCons][3]);
            textView5.setText(AstroActivity.dataCons[idCons][4]);
            textView6.setText(AstroActivity.dataCons[idCons][5]);
        }
    }
}
