package descartes.info.l3ag2.eyetrek.fragment;


import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.File;

import descartes.info.l3ag2.eyetrek.R;
import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

/**
 * Created by Dorian Quaboul.
 *
 * Un fragment qui va contenir l'édition d'une vidéo lorsque celle ci a une durée
 * supérieure à 10 secondes.
 * Utilisation de la libraire - https://github.com/titansgroup/k4l-video-trimmer
 */
public class FragmentEdition extends Fragment{

    private K4LVideoTrimmer videoTrimmer;

    private OnTrimVideoListener listener = new OnTrimVideoListener() {
        /**
         *  On récupère l'uri correspondant à l'extraction de la video.
         *  Puis, on redirige vers le fragment de visualisation (pour pouvoir sélectionner l'image
         *  à analyser dans la video tronquée).
         *
         * @param uri_edit uri du résultat de l'extraction de la video
         */
        @Override
        public void getResult(Uri uri_edit) {

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            FragmentVideo fragmentVideo = new FragmentVideo();

            Bundle arguments = new Bundle();
            //On envoie le chemin de la vidéo tronquée (vidéo qui est stockée temporaiement
            arguments.putString("chemin_video_edit", uri_edit.getPath());
            fragmentVideo.setArguments(arguments);

            fragmentTransaction.replace(R.id.contenu_fragment, fragmentVideo);
            fragmentTransaction.commit();
        }

        /**
         * Si l'utilisateur clique sur le bouton "Annuler" de l'éditeur de vidéo, on le redirige
         * vers le fragment Analyse (qui était le précédent dans la pile d'exécution).
         */
        @Override
        public void cancelAction() {

            FragmentManager fragmentManager = getFragmentManager();

            Toast.makeText(getContext(), "Vous avez annulé l'analyse", Toast.LENGTH_SHORT).show();
            //On retire ce fragment de la pile et on revient au fragment analyse
            fragmentManager.popBackStack();
        }
    };

    private File cheminVideo;

    private BottomNavigationView barreMenu;

    //SharedPreferences = Cache
    private static final String PREFS_EDITION = "EXPLICATION_EDITION";
    SharedPreferences sharedPreferences ;

    public FragmentEdition() {
        // Required empty public constructor
    }

    /**
     * On instancie les objets non graphiques
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
            On récupère les arguments passés lors de la creation du fragment
            c'est à dire dans le fragment analyse lors du choix de la video dans la galerie.
         */

        //la clé "chemin_edit" est associée à la valeur contenant le chemin de la video
        if (getArguments().containsKey("chemin_edit")) {
            String chemin = getArguments().get("chemin_edit").toString();
            cheminVideo = new File(chemin);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edition, container, false);

        videoTrimmer = view.findViewById(R.id.timeLine);

        //Récupération du cache
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (videoTrimmer != null) {

            //On affiche un message d'explication (un tutoriel pour extraire une partie dans une vidéo)
            afficherExplicationEdition();

            String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Temp/";
            //On définit le chemin où la vidéo tronquée va être sauvegardée.
            videoTrimmer.setDestinationPath(cheminDossier);
            //On doit déterminer le chemin de la vidéo à tronquer.
            videoTrimmer.setVideoURI(Uri.parse(cheminVideo.getPath()));
            //On définit la durée maximale de l'intervalle de l'extraction de la vidéo à 10 sec.
            videoTrimmer.setMaxDuration(10);
            //On associe un listener pour gérer et récupérer le résultat de l'extraction.
            videoTrimmer.setOnTrimVideoListener(listener);

            Button btCancel = view.findViewById(R.id.btCancel);
            btCancel.setText("Annuler");
            Button btSave = view.findViewById(R.id.btSave);
            btSave.setText("Continuer");
        } else {
            Log.e("FragmentEdition", "videoTrimmer == null");
        }

        //on cache la barre de menu
        barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.INVISIBLE);

        return view;
    }

    /**
     * Affiche une boîte de dialogue pour expliquer comment extraire une partie (de 10 sec max) d'une
     * vidéo.
     * Une case à cocher détermine si cette boîte de dialogue sera affichée la prochaine fois
     * que l'utilisateur édite une vidéo. Les préférences de l'affichage de cette boîte de dialogue
     * sont stockées dans le cache.
     */
    private void afficherExplicationEdition() {
        //Si la préférence existe, on récupère la valeur associée sinon cela renvoie null
        String contenuPreferences = sharedPreferences.getString(PREFS_EDITION, null);
        //On vérifie si dans le cache, il y a déjà une préférence enregistrée concernant l'affichage de la boîte de dialogue.
        if (contenuPreferences != null) {
            //Si la préférence donne pour valeur "check", cela veut dire que l'utilisateur ne veut plus afficher cette boîte.
            if (!contenuPreferences.equals("check")) {
                //On affiche un message d'explication (un tutoriel pour extraire une partie d'une vidéo)
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                LayoutInflater adbInflater = LayoutInflater.from(getContext());
                View layout = adbInflater.inflate(R.layout.alertdialog, null);

                CheckBox checkBox = layout.findViewById(R.id.checkboxDialog);
                //on place la checkbox dans la boîte de dialogue
                builder.setView(layout)
                        .setTitle("Extraire la partie parfaite d'une vidéo")
                        .setIcon(R.drawable.ic_info_outline_black_32dp)
                        .setMessage(R.string.dialog_edit)
                        .setPositiveButton("J'ai compris", (dialog, witch) -> {
                            if (checkBox.isChecked()) {
                                //On enregistre la préférence dans le cache si l'utilisateur ne veut plus afficher la boîte.
                                sharedPreferences.edit().putString(PREFS_EDITION, "check").apply();
                            } else {
                                //On enregistre la préférence dans le cache si l'utilisateur veut toujours afficher la boîte.
                                sharedPreferences.edit().putString(PREFS_EDITION, "no_check").apply();
                            }
                        })
                        .create().show();
            }

        }
        //La préférence n'est pas présente dans le cache donc on affiche la boîte de dialogue pour obtenir les préférences.
        else {
            //On affiche un message d'explication (un tutoriel pour choisir une partie dans une vidéo)
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            LayoutInflater adbInflater = LayoutInflater.from(getContext());
            View layout = adbInflater.inflate(R.layout.alertdialog, null);

            CheckBox checkBox = layout.findViewById(R.id.checkboxDialog);

            builder.setView(layout)
                    .setTitle("Extraire la partie parfaite d'une vidéo")
                    .setIcon(R.drawable.ic_info_outline_black_32dp)
                    .setMessage(R.string.dialog_edit)
                    .setPositiveButton("J'ai compris", (dialog, witch) -> {
                        if (checkBox.isChecked()) {
                            sharedPreferences.edit().putString(PREFS_EDITION, "check").apply();
                        } else {
                            sharedPreferences.edit().putString(PREFS_EDITION, "no_check").apply();
                        }
                    })
                    .create().show();
        }
    }

}
