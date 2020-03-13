package descartes.info.l3ag2.eyetrek.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.AstroActivity;

public class FragmentConsCatalogDetails extends Fragment {

    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;


    /**
     * Creer une nouvelle instance de Fragment_AstroAffichagePhoto
     * initialise a l'affichage du texte "index"
     */
    public static FragmentConsCatalogDetails newInstance(int idCons) {
        FragmentConsCatalogDetails fragment = new FragmentConsCatalogDetails();

        Bundle b = new Bundle();
        b.putInt("idCons",idCons);
        // Fournir une entree d'index comme argument.
        fragment.setArguments(b);

        return fragment;
    }


    /**
     * Les TextView sont associes aux differentes informations de la constellation
     * Nous avons associe a chaque item du recyclerView un identifiant
     * Et une position du tableau correspondant à une information de notre base de donnees
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_const_detailcatalogue, container, false);

        int idCons = getArguments().getInt("idCons");


        textView = view.findViewById(R.id.nom_constellation);
        textView.setText(AstroActivity.dataCons[idCons][0]);


        textView2 = view.findViewById(R.id.nom_constellation2);
        textView2.setText(AstroActivity.dataCons[idCons][1]);

        textView3 = view.findViewById(R.id.ascension_droite);
        textView3.setText(AstroActivity.dataCons[idCons][2]);

        textView4 = view.findViewById(R.id.declinaison);
        textView4.setText(AstroActivity.dataCons[idCons][3]);

        textView5 = view.findViewById(R.id.etoile_brillante);
        textView5.setText(AstroActivity.dataCons[idCons][4]);

        textView6 = view.findViewById(R.id.etoile_proche);
        textView6.setText(AstroActivity.dataCons[idCons][5]);

        textView6 = view.findViewById(R.id.etoile_proche);
        textView6.setText(AstroActivity.dataCons[idCons][5]);



        return view;
    }
}
