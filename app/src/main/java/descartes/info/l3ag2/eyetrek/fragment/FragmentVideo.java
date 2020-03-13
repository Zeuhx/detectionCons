package descartes.info.l3ag2.eyetrek.fragment;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Dorian Quaboul.
 * <p>
 * Un fragment qui va contenir la visualisation de la vidéo et le choix de l'image à analyser
 * dans celle ci.
 */
public class FragmentVideo extends Fragment {

    private VideoView video_visualisation;
    private MediaController barreControle;

    private BottomNavigationView barreMenu;

    private ImageButton bouton_annuler;
    private Button bouton_next;
    private ImageButton bouton_play;

    private File fichierVideo;
    private File fichierExtract;
    private Uri uri;

    private String type_data;

    //SharedPreferences = Cache
    private static final String PREFS_VIDEO = "EXPLICATION_VIDEO";
    SharedPreferences sharedPreferences ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            La clé "chemin_video" est associée à la valeur contenant le chemin de la vidéo qui vient
            d'être enregistrée à l'aide de l'application.
        */
        if (getArguments().containsKey("chemin_video")) {
            //On instancie la barre de contrôle de vidéo (time line, play, pause...)
            barreControle = new MediaController(getContext());

            type_data = "video_camera";
            String chemin = getArguments().get("chemin_video").toString();

            fichierVideo = new File(chemin);
        }
        /*
            La clé "chemin_video_edit" est associée à la valeur contenant le chemin de la video
            éditée (stockée temporairement dans le dossier Temp).
        */
        else if (getArguments().containsKey("chemin_video_edit")) {
            //On instancie la barre de contrôle de vidéo (time line, play, pause...)
            barreControle = new MediaController(getContext());

            type_data = "video_edit";
            String chemin = getArguments().get("chemin_video_edit").toString();

            fichierVideo = new File(chemin);
        }
        /*
            La clé "uri" est associée à la valeur contenant le chemin de la video stockée dans la
            galerie
        */
        else if (getArguments().containsKey("uri_video")) {
            //On instancie la barre de contrôle de vidéo (time line, play, pause...)
            barreControle = new MediaController(getContext());

            type_data = "video_galery";
            uri = (Uri) getArguments().get("uri_video");
        }
        else {
            Toast.makeText(getContext(), "Une erreur s'est produite lors de la visualisation", Toast.LENGTH_SHORT).show();
        }

    }

    public FragmentVideo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        //Récupération du cache
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        bouton_annuler = view.findViewById(R.id.bouton_annuler);
        bouton_next = view.findViewById(R.id.bouton_next);
        bouton_play = view.findViewById(R.id.bouton_play);

        barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.INVISIBLE);

        video_visualisation = view.findViewById(R.id.video_visualisation);
        video_visualisation.setMediaController(barreControle);

        afficherExplicationVideo();

        switch (type_data) {
            case "video_galery":
                video_visualisation.setVideoURI(uri);
                //On déplace le curseur de la vidéo à 1 msec pour pouvoir avoir un aperçu de la vidéo.
                video_visualisation.seekTo(1);
                break;

            case "video_camera": case "video_edit":
                video_visualisation.setVideoURI(Uri.parse(fichierVideo.getPath()));
                //On déplace le curseur de la vidéo à 1 msec pour pouvoir avoir un aperçu de la vidéo.
                video_visualisation.seekTo(1);
                break;
        }

        //Gestion du bouton "Play"
        bouton_play.setOnClickListener((v) -> {
            //On démarre la vidéo
            video_visualisation.start();
            //On fait disparaître le bouton play.
            v.setVisibility(View.GONE);
        });

        //Gestion du bouton "Annuler"
        bouton_annuler.setOnClickListener((v) -> {

            FragmentManager fragmentManager = getFragmentManager();
            //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            //Selon le type de données, le bouton "Annuler" réagit différement.
            switch (type_data) {
                //Le bouton "Annuler" permet d'annuler l'analyse.
                case "video_galery":

                    Toast.makeText(getContext(), "Vous avez annulé l'analyse", Toast.LENGTH_SHORT).show();

                    fragmentManager.beginTransaction().replace(R.id.contenu_fragment, new FragmentScanFeuille()).commit();


                    /*
                        On retire ce fragment de la pile et on revient au fragment analyse car
                        il s'agissait du fragment précédent dans la pile d'exécution.
                    */
                    //fragmentManager.popBackStack();

                    break;

                //Le bouton "Annuler" supprime la vidéo et annule l'analyse.
                case "video_camera":

                    if (fichierVideo.delete()) {

                        Toast.makeText(getContext(), "La vidéo a bien été supprimée", Toast.LENGTH_SHORT).show();

                        fragmentManager.beginTransaction().replace(R.id.contenu_fragment, new FragmentScanFeuille()).commit();


                        /*
                            On retire ce fragment de la pile et on revient au fragment analyse car
                            il s'agissait du fragment précédent dans la pile d'exécution.
                         */
                        //fragmentManager.popBackStack();

                    } else {
                        Toast.makeText(getContext(), "Une erreur est survenue lors de la suppression", Toast.LENGTH_SHORT).show();
                    }

                    break;

                //Le bouton "Annuler" supprime la vidéo et annule l'analyse.
                case "video_edit":

                    if (fichierVideo.delete()) {
                        Toast.makeText(getContext(), "La vidéo a bien été supprimée", Toast.LENGTH_SHORT).show();

                        /*
                        fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnalyse());
                        fragmentTransaction.commit();
                        */

                        fragmentManager.beginTransaction().replace(R.id.contenu_fragment, new FragmentScanFeuille()).commit();

                    } else {
                        Toast.makeText(getContext(), "Une erreur est survenue lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
            }

        });

        //Gestion du bouton "Continuer"
        bouton_next.setOnClickListener((v) -> {
            createExtractFile();
            //Poursuite de l'analyse
            MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
            mediaRetriever.setDataSource(fichierVideo.getPath());
            //conversion des millisecondes en microsecondes
            long time = video_visualisation.getCurrentPosition() * 1000;
            Bitmap bitmap = mediaRetriever.getFrameAtTime(time);
            saveBitmapIntoFile(bitmap);

            //on redirige l'utilisateur vers le fragment vidéo pour qu'il choisisse l'image parfaite
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            FragmentVisualisation fragmentVisualisation = new FragmentVisualisation();

            Bundle arguments = new Bundle();

            arguments.putString("chemin_photo", fichierExtract.getPath());
            fragmentVisualisation.setArguments(arguments);

            fragmentTransaction.replace(R.id.contenu_fragment, fragmentVisualisation);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }


    /**
     * Affiche une boîte de dialogue pour expliquer comment extraire une image d'une vidéo.
     * Une case à cocher détermine si cette boîte de dialogue sera affichée la prochaine fois
     * que l'utilisateur choisit une image dans une vidéo. Les préférences de l'affichage de
     * cette boîte de dialogue sont stockées dans le cache.
     */
    public void afficherExplicationVideo() {
        //Si la préférence existe, on récupère la valeur associée sinon cela renvoie null
        String contenuPreferences = sharedPreferences.getString(PREFS_VIDEO,null);
        //On vérifie si dans le cache, il y a déjà une préférence enregistrée concernant l'affichage de la boîte de dialogue.
        if(contenuPreferences != null) {
            //Si la préférence donne pour valeur "check", cela veut dire que l'utilisateur ne veut plus afficher cette boîte.
            if(!contenuPreferences.equals("check")) {
                //On affiche un message d'explication (un tutoriel pour choisir une image dans une vidéo)
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                LayoutInflater adbInflater = LayoutInflater.from(getContext());
                View layout = adbInflater.inflate(R.layout.alertdialog, null);

                CheckBox checkBox = layout.findViewById(R.id.checkboxDialog);
                //on place la checkbox dans la boîte de dialogue
                builder.setView(layout)
                        .setTitle("Extraire l'image parfaite depuis votre vidéo")
                        .setIcon(R.drawable.ic_info_outline_black_32dp)
                        .setMessage(R.string.dialog_video)
                        .setPositiveButton("J'ai compris", (dialog, witch) -> {
                            if(checkBox.isChecked()) {
                                //On enregistre la préférence dans le cache si l'utilisateur ne veut plus afficher la boîte.
                                sharedPreferences.edit().putString(PREFS_VIDEO,"check").apply();
                            }
                            else {
                                //On enregistre la préférence dans le cache si l'utilisateur veut toujours afficher la boîte.
                                sharedPreferences.edit().putString(PREFS_VIDEO,"no_check").apply();
                            }
                        })
                        .create().show();
            }

        }
        else {
            //On affiche un message d'explication (un tutoriel pour choisir une image dans une vidéo)
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            LayoutInflater adbInflater = LayoutInflater.from(getContext());
            View layout = adbInflater.inflate(R.layout.alertdialog, null);

            CheckBox checkBox = layout.findViewById(R.id.checkboxDialog);

            builder.setView(layout)
                    .setTitle("Extraire l'image parfaite depuis votre vidéo")
                    .setIcon(R.drawable.ic_info_outline_black_32dp)
                    .setMessage(R.string.dialog_video)
                    .setPositiveButton("J'ai compris", (dialog, witch) -> {
                        if(checkBox.isChecked()) {
                            sharedPreferences.edit().putString(PREFS_VIDEO,"check").apply();
                        }
                        else {
                            sharedPreferences.edit().putString(PREFS_VIDEO,"no_check").apply();
                        }
                    })
                    .create().show();
        }
    }

    private void createExtractFile() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";

        StringBuffer nomFichier = new StringBuffer("CaptureVideo_");
        nomFichier.append(new Date().getTime());
        nomFichier.append(".jpg");

        fichierExtract = new File(cheminDossier, nomFichier.toString());
    }

    /**
     * Permet de sauvegarder la bitmap un fichier jpg dans le dossier Pictures
     *
     * Source : https://stackoverflow.com/questions/649154/save-bitmap-to-location
     */
    private void saveBitmapIntoFile(Bitmap bmp) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fichierExtract);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
