package descartes.info.l3ag2.eyetrek.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.AlgoKNNBirds;
import descartes.info.l3ag2.eyetrek.classes.DatabaseHandler;
import descartes.info.l3ag2.eyetrek.classes.FeaturesVectBirds;
import descartes.info.l3ag2.eyetrek.classes.Result;
import descartes.info.l3ag2.eyetrek.classes.SoundProcessing;
import descartes.info.l3ag2.eyetrek.classes.Syllabe;
import descartes.info.l3ag2.eyetrek.classes.Voisins;
import descartes.info.l3ag2.eyetrek.pojo.Bird;

/**
 * Created by Dorian Quaboul on 22/04/2018
 */
public class FragmentVisualisationAudio extends Fragment {

    private ImageButton bouton_annuler;
    private Button bouton_next;
    private ImageButton bouton_play;

    private BottomNavigationView barreMenu;

    private File fichierAudio;

    private MediaPlayer player;

    public FragmentVisualisationAudio() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            On récupère les arguments passés lors de la création du fragment, c'est à dire dans le
            fragment audio lors de l'enregistrement du son.
         */
        //la clé "chemin_audio" est associée à la valeur contenant le chemin du son enregistré.
        if (getArguments().containsKey("chemin_audio")) {
            String chemin = getArguments().get("chemin_audio").toString();
            fichierAudio = new File(chemin);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visualisation_audio, container, false);

        bouton_annuler = view.findViewById(R.id.bouton_annuler);
        bouton_next = view.findViewById(R.id.bouton_next);
        bouton_play = view.findViewById(R.id.bouton_play);

        barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.INVISIBLE);

        //Gestion du bouton "Annuler"
        bouton_annuler.setOnClickListener((v) -> {
            if(player != null) {
                if(player.isPlaying()) {
                    player.stop();
                }
            }
            FragmentManager fragmentManager = getFragmentManager();
            //le bouton "Annuler" permet de supprimer le son enregistré et d'annuler l'analyse

            if (fichierAudio.delete()) {
                Toast.makeText(getContext(), "Le son a bien été supprimé", Toast.LENGTH_SHORT).show();
                //On retire ce fragment de la pile et on revient au fragment audio
                fragmentManager.popBackStack();
            } else {
                Toast.makeText(getContext(), "Une erreur est survenue lors de la suppression", Toast.LENGTH_SHORT).show();
            }
        });

        //Gestion du bouton "Play"
        bouton_play.setOnClickListener((v) -> {
            player = new MediaPlayer();

            try {
                player.setDataSource(fichierAudio.getAbsolutePath());
                player.prepare();
                player.start();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            v.setVisibility(View.GONE);
        });



        //Gestion du bouton "Continuer"
        bouton_next.setOnClickListener((v) -> {
            //si l'enregistrement est cours de lecture, on l'arrête
            if(player != null) {
                if(player.isPlaying()) {
                    player.stop();
                }
            }
            //Poursuite de l'analyse
            new AnalyseTask().execute(fichierAudio.getPath());
        });

        return view;
    }


    public class AnalyseTask extends AsyncTask<String, String, List<Voisins>> {
        private ProgressDialog boiteChargement;
        private int numberOfSyllabe;

        protected void onPreExecute() {
            boiteChargement = new ProgressDialog(getActivity());
            boiteChargement.setMessage("Initialisation de l'analyse ...");
            boiteChargement.setTitle("Analyse de son d'oiseau");
            boiteChargement.setCancelable(false);
            boiteChargement.show();
        }

        @Override
        protected List<Voisins> doInBackground(String... filePath) {
            SoundProcessing.init(getContext());
            publishProgress("Extraction des données brutes de l'enregistrement ...");
            List<Float> rawPCMData = SoundProcessing.loadPCMData(filePath[0]);
            publishProgress("Extraction des données de l'enveloppe sonore de l'enregistrement ...");
            List<Float> enveloppePCMData = SoundProcessing.loadPCMEnergyEnveloppe(filePath[0]);
            publishProgress("Détection de syllabes dans l'enregistrement  ...");
            List<Syllabe> listSyllabes = SoundProcessing.automaticSegmentation(enveloppePCMData,rawPCMData);
            numberOfSyllabe = listSyllabes.size();
            publishProgress("Calcul des 12 coefficients MFCCS pour chaque syllabe détectée ...");
            SoundProcessing.extractMFCC(listSyllabes);
            List<FeaturesVectBirds> listVectOfSyllabe = new ArrayList<>();
            //on parcourt toutes les syllabes et on extrait leur vecteur de caracteristiques
            //pour pouvoir les comparer à ceux contenus dans le fichier csv
            publishProgress("Normalisation des 12 coefficients MFCCS pour chaque syllabe détectée ...");
            for(Syllabe s : listSyllabes) {
                FeaturesVectBirds vect = new FeaturesVectBirds();
                vect.setMFCCS(s.normalizeMFCC());
                listVectOfSyllabe.add(vect);
            }

            List<FeaturesVectBirds> allDataFeatures;
            List<Voisins> resultats = null;
            List<Result> classesOfSyllabe = new ArrayList<>();
            try {
                AlgoKNNBirds.setContext(getContext());
                publishProgress("Récupération des vecteurs de caractéristiques du fichier csv ...");
                allDataFeatures = AlgoKNNBirds.getListFeatures();
                publishProgress("Classification des syllabes détectées ...");
                for(FeaturesVectBirds vect : listVectOfSyllabe) {
                    List<Result> plusProchesVoisins = AlgoKNNBirds.getPlusProchesVoisins(allDataFeatures, vect);
                    //Sachant que K=1, un seul voisin est présent dans cette liste. On l'ajoute donc à la liste de la classification des syllabes
                    classesOfSyllabe.add(plusProchesVoisins.get(0));
                }
                //Maintenant que chaque syllabe est associée à une classe d'oiseaux, on va déterminer
                // la classe dominante parmis toutes les syllabes.
                publishProgress("Détermination des classes dominantes ...");
                resultats = AlgoKNNBirds.getPrediction(classesOfSyllabe);
                publishProgress("Analyse terminée");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultats;
        }

        /**
         * Permet d'afficher en temps réél, les différentes étapes de l'analyse
         * @param values
         */
        @Override
        protected void onProgressUpdate(String... values) {
            boiteChargement.setMessage(values[0]);
        }

        /**
         * Permet d'afficher la boite de dialogue contenant les résultats d'analyse
         * @param result
         */
        protected void onPostExecute(List<Voisins> result) {

            boiteChargement.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater adbInflater = LayoutInflater.from(getContext());
            View layout = adbInflater.inflate(R.layout.dialog_result, null);

            TextView resultPos1 = layout.findViewById(R.id.resultPos1);
            TextView resultPos2 = layout.findViewById(R.id.resultPos2);
            TextView resultPos3 = layout.findViewById(R.id.resultPos3);

            TextView pourcentagePos1 = layout.findViewById(R.id.pourcentagePos1);
            TextView pourcentagePos2 = layout.findViewById(R.id.pourcentagePos2);
            TextView pourcentagePos3 = layout.findViewById(R.id.pourcentagePos3);

            CircleImageView resultImage = layout.findViewById(R.id.image_result1);
            CircleImageView resultImage2 = layout.findViewById(R.id.image_result2);
            CircleImageView resultImage3 = layout.findViewById(R.id.image_result3);

            //On récupère les informations du résultat
            DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
            Bird oiseau = databaseHandler.getBird(result.get(0).getLabel());
            String strOiseau1 = oiseau.getName();
            int pourcent1 = result.get(0).getFrequencyBird(numberOfSyllabe);

            resultPos1.setText(strOiseau1);
            putColorPourcentage(pourcentagePos1,pourcent1);
            pourcentagePos1.setText(pourcent1+"%");
            resultImage.setImageResource(getContext().getResources().getIdentifier(oiseau.getPicture(), "drawable", getContext().getPackageName()));

            if(result.size() > 1){
                Bird oiseau2 = databaseHandler.getBird(result.get(1).getLabel());
                String strOiseau2 = oiseau2.getName();
                int pourcent2 = result.get(1).getFrequencyBird(numberOfSyllabe);

                resultPos2.setText(strOiseau2);
                putColorPourcentage(pourcentagePos2,pourcent2);
                pourcentagePos2.setText(pourcent2+"%");
                resultImage2.setImageResource(getContext().getResources().getIdentifier(oiseau2.getPicture(), "drawable", getContext().getPackageName()));

                if (result.size() > 3){
                    Bird oiseau3 = databaseHandler.getBird(result.get(2).getLabel());
                    String strOiseau3 = oiseau3.getName();
                    int pourcent3 = result.get(2).getFrequencyBird(numberOfSyllabe);

                    resultPos3.setText(strOiseau3);
                    putColorPourcentage(pourcentagePos3,pourcent3);
                    pourcentagePos3.setText(pourcent3+"%");
                    resultImage3.setImageResource(getContext().getResources().getIdentifier(oiseau3.getPicture(), "drawable", getContext().getPackageName()));
                }
            }

            //On place l'image dans la boite de dialog
            builder.setView(layout)
                    .setCancelable(false)
                    .setPositiveButton("Merci ! ", new DialogInterface.OnClickListener() {
                        //Quand l'utilisateur cliquera sur le bouton "Merci !", on le redirige vers le fragment audio

                        public void onClick(DialogInterface dialog, int which) {
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAudio()).commit();
                            fragmentTransaction.addToBackStack(null);
                        }
                    })
                    .create().show();
        }
    }





    /**
     * On applique une teinte de couleur selon le pourcentage de reconnaissance obtenu après
     * l'analyse.
     * >= 65% couleur verte = très bon résultat
     * compris entre 30 et 65% couleur jaune = résultat moyen
     * <= 30% couleur rouge = résultat insatisfaisant
     * @param view
     * @param pourcentage
     */
    private void putColorPourcentage(TextView view, int pourcentage) {

        if(pourcentage >= 65) {
            view.setTextColor(getResources().getColor(R.color.hightResult,null));
        } else if(pourcentage > 30 && pourcentage <65 ) {
            view.setTextColor(getResources().getColor(R.color.mediumResult,null));
        } else {
            view.setTextColor(getResources().getColor(R.color.lowResult,null));
        }
    }


}