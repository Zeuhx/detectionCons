package descartes.info.l3ag2.eyetrek.fragment;


import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireModels;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireResultat;
import descartes.info.l3ag2.eyetrek.spectro_android.DynamicAudioConfig;
import descartes.info.l3ag2.eyetrek.spectro_android.LocationClient;
import descartes.info.l3ag2.eyetrek.spectro_android.UiConfig;
import descartes.info.l3ag2.eyetrek.spectro_android.audioproc.BitmapProvider;
import descartes.info.l3ag2.eyetrek.spectro_android.storage.WAVExplorer;
import descartes.info.l3ag2.eyetrek.spectro_android.ui.SpectrogramSurfaceView;
import descartes.info.l3ag2.eyetrek.tensorflow.ImageClassifier;
import pl.droidsonroids.gif.GifImageView;

import static java.lang.Thread.sleep;

/**
 * Created by Jérémy on 20/03/2018.
 * Updated by Dorian Quaboul.
 */

public class FragmentAudio extends  android.support.v4.app.Fragment{

    public FragmentAudio() {
        super();
    }

    private static final int SAMPLE_RATE = 44100;
    private static final int REQUEST_PERMISSION_CODE = 1000;
    private static final int NUMBER_CHANNEL = 1;
    private static final String RECORDER_FORMAT = ".wav";
    private static final String NAME_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Eyetrek/Sounds/";
    private static final String NAME_FILE_ID = "RecordBird_";


    boolean mode_rec = false;
    CardView record;
    CardView cadre_spectrograme;
    ImageClassifier classifieur;

    // Liste des résultats de l'algorithme
    ArrayList<ArrayList<Object>> results;


    short[][] audioWindows;
    DynamicAudioConfig dac;

    //private View rootView;
    private ImageButton resumeButton;
    private CardView selectionConfirmButton;
    private CardView selectionCancelButton;
    private SpectrogramSurfaceView ssv;
    //private TextView leftTimeTextView;
    //private TextView rightTimeTextView;
    private TextView bottomFreqTextView;
    private TextView topFreqTextView;
    private LinearLayout captureButtonContainer;


    /* === === Variables résultat analyse === === */

    // Liste des cartes qui affichent le résultat de l'analyse
    private ScrollView listeCartes;

    // Carte du 1e résultat
    private CardView carte1;
    // Nom de la plante de la 1ere carte
    private TextView titreCarte1;
    // Sureté du résultat de la plante de la 1ere carte
    private TextView sureteCarte1;
    // Description de la plante de la 1ere carte
    private TextView descriptionCarte1;
    // Image de la plante de la 1ere carte
    private ImageView imageCarte1;

    // Carte du 2e résultat
    private CardView carte2;
    // Nom de la plante de la 2e carte
    private TextView titreCarte2;
    // Sureté du résultat de la plante de la 2e carte
    private TextView sureteCarte2;
    // Description de la plante de la 2e carte
    private TextView descriptionCarte2;
    // Image de la plante de la 2e carte
    private ImageView imageCarte2;

    // Carte du 3e résultat
    private CardView carte3;
    // Nom de la plante de la 3e carte
    private TextView titreCarte3;
    // Sureté du résultat de la plante de la 3e carte
    private TextView sureteCarte3;
    // Description de la plante de la 3e carte
    private TextView descriptionCarte3;
    // Image de la plante de la 3e carte
    private ImageView imageCarte3;

    // Bouton pour revenir en mode prise de photo quand on est en mode affichage des résultats
    private ImageButton boutonRetour;

    // Image avec le résultat de la photo qui a été prise
    private ImageView resultatPhoto;

    // Petit texte pour expliquer qu'il faut rester appuyé sur le spectrogramme pour selectionner la zonee d'interet
    public TextView texte_tuto;



    // Gif de chargement de l'inférence
    private GifImageView chargement_inference;



    private MediaRecorder recorder;

    // icon bouton recorder
    private ImageView icon_record_button;



    private boolean isRecord;

    //Le fichier correspondant à la vidéo enregistrée
    private File fichierSound;
    //Le dossier où seront stockées les vidéos
    protected File dossierSounds;

    private BottomNavigationView barreMenu;

    private ProgressBar circleProgress;

    // Image du spectrogram capturé
    //ImageView spectrogram;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSoundFolder();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //classifieur = new ImageClassifier(getActivity(), UtilitaireModels.MODELE_OISEAUX_PATH, UtilitaireModels.LABELS_MODELE_OISEAUX_PATH);
                    classifieur = new ImageClassifier(getActivity(), UtilitaireModels.MODELE_OISEAUX_PATH, UtilitaireModels.LABELS_MODELE_OISEAUX_PATH);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        record = view.findViewById(R.id.record_button);
        //ImageButton settings = view.findViewById(R.id.settings);

        //spectrogram = view.findViewById(R.id.spectrogram);

        //circleProgress = view.findViewById(R.id.barre_recordVideo);

        barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.VISIBLE);

        cadre_spectrograme = view.findViewById(R.id.cadre_spectrograme);
        cadre_spectrograme.setVisibility(View.INVISIBLE);



        // Bouton pour revenir en mode prise de photo quand on est en mode affichage des résultats
        boutonRetour = view.findViewById(R.id.bouton_retour);
        boutonRetour.setVisibility(View.GONE);
        // Liste des cartes qui affichent le résultat d'analyse
        listeCartes = view.findViewById(R.id.listeCartes);
        listeCartes.setVisibility(View.GONE);

        // Carte du 1e résultat
        carte1 = view.findViewById(R.id.carte_resultat_1);
        carte1.setClickable(false);
        // Nom de la plante de la 1ere carte
        titreCarte1 = view.findViewById(R.id.titre_resultat_1);
        // Sureté du résultat de la plante de la 1ere carte
        sureteCarte1 = view.findViewById(R.id.certitude_resultat_1);
        // Description de la plante de la 1ere carte
        descriptionCarte1 = view.findViewById(R.id.description_resultat_1);
        // Image de la plante de la 1ere carte
        imageCarte1 = view.findViewById(R.id.image_resultat_1);

        // Carte du 2e résultat
        carte2 = view.findViewById(R.id.carte_resultat_2);
        carte2.setClickable(false);
        // Nom de la plante de la 2e carte
        titreCarte2 = view.findViewById(R.id.titre_resultat_2);
        // Sureté du résultat de la plante de la 2e carte
        sureteCarte2 = view.findViewById(R.id.certitude_resultat_2);
        // Description de la plante de la 2e carte
        descriptionCarte2 = view.findViewById(R.id.description_resultat_2);
        // Image de la plante de la 2e carte
        imageCarte2 = view.findViewById(R.id.image_resultat_2);

        // Carte du 3e résultat
        carte3 = view.findViewById(R.id.carte_resultat_3);
        carte3.setClickable(false);
        // Nom de la plante de la 3e carte
        titreCarte3 = view.findViewById(R.id.titre_resultat_3);
        // Sureté du résultat de la plante de la 3e carte
        sureteCarte3 = view.findViewById(R.id.certitude_resultat_3);
        // Description de la plante de la 3e carte
        descriptionCarte3 = view.findViewById(R.id.description_resultat_3);
        // Image de la plante de la 3e carte
        imageCarte3 = view.findViewById(R.id.image_resultat_3);


        // Gif de chargement de l'inférence
        chargement_inference = view.findViewById(R.id.chargement_inference);
        chargement_inference.setVisibility(View.GONE);

        // Image avec le résultat de la photo qui a été prise
        resultatPhoto = view.findViewById(R.id.resultat_photo);

        icon_record_button = view.findViewById(R.id.icon_record_button);



        texte_tuto = view.findViewById(R.id.texte_tuto);
        texte_tuto.setVisibility(View.GONE);


        boutonRetour.setOnClickListener((v) -> {
            retourModeCapture();
        });






        init(view);

        /*

        //Source : https://stackoverflow.com/questions/30766755/smooth-progress-bar-animation
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(circleProgress, "progress", 0, 100);
        //On configure la durée de l'animation à 20 secondes pour le remplissage du timer (barre circulaire)
        progressAnimator.setDuration(20000);
        //Le remplissage s'effectue de façon linéaire (c'est à dire en continue sans accélération)
        progressAnimator.setInterpolator(new LinearInterpolator());

        //Clique sur le bouton paramètre permettant d'accéder au choix d'analyse
        settings.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentMenu()).commit();
        });

        init(view);


        if (checkPermissionFromDevice()){

            //Toast.makeText(getContext(), "Préparation lecture", Toast.LENGTH_LONG).show();
            record.setOnClickListener((View v) -> {
                if (!isRecord) {
                    isRecord = true;
                    createSoundFile();
                    //on démarre le remplissage de la jauge du timer
                    progressAnimator.start();
                    setupMediaRecorder();
                    recorder.start();
                } else {
                    isRecord = false;
                    //On réinitialise le remplissage de la barre circulaire du timer (remis à 0)
                    circleProgress.setProgress(0);
                    //on arrête le remplissage de la jauge du timer
                    progressAnimator.cancel();
                    recorder.stop();
                    recorder.reset();

                    */

                    /*

                    Ancien code dee quand l'analyse marchais sous KNN

                    //on redirige l'utilisateur vers le fragment visualisation audio pour qu'il écoute son enregistrement
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    FragmentVisualisationAudio fragmentVisualisationAudio = new FragmentVisualisationAudio();

                    Bundle arguments = new Bundle();

                    arguments.putString("chemin_audio", fichierSound.getPath());

                    fragmentVisualisationAudio.setArguments(arguments);

                    fragmentTransaction.replace(R.id.contenu_fragment, fragmentVisualisationAudio);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    */

                    /*



                    // On va :
                    // 1. ouvrir le fichier wav
                    // 2. le convertir en spectrograme
                    // 3. obtenir le spectrogramme en Bitmap et s'assurer qu'il soit utilisable sur tensorflow (encodé sous ARGB_8888)
                    // 4. passer le bitmap sous tensorflow
                    // 5. obtenir les résultats et les afficher



                    WAVExplorer fichier_wav = new WAVExplorer(fichierSound.getPath());

                    dac = new DynamicAudioConfig(getContext());
                    audioWindows = new short[DynamicAudioConfig.WINDOW_LIMIT][dac.SAMPLES_PER_WINDOW];

                    double[] mono_record = fichier_wav.getFirstChannelData();

                    for(int i = 0; i<audioWindows[0].length; i++){
                        audioWindows[0][i] = (short) mono_record[i];
                    }

                    BitmapProvider generateur_spectrogram = new BitmapProvider(dac);
                    generateur_spectrogram.start();

                    Bitmap bitmap = generateur_spectrogram.createEntireBitmap(0, 500, 0, 15000);

                    spectrogram.setImageBitmap(bitmap);



                }
            });

        }

        else{
            requestPermission();
        }
        */

        return view;
    }

    private void init(View view) {
        // SpectrogramSurfaceView:
        ssv = view.findViewById(R.id.ssv);
        if(this == null) Log.e("init() FragmentAudio", "this est null !!!");
        ssv.setSpectroFragment(this);

        // Resume button:
        resumeButton = (ImageButton) view.findViewById(R.id.button_resume);

        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ssv.resumeScrolling();
            }
        });

        record.setOnClickListener((View v) -> {
            cadre_spectrograme.setVisibility(View.VISIBLE);
            if(mode_rec){
                ssv.pauseScrolling();
                icon_record_button.setImageResource(R.drawable.ic_mic_black_24dp);
                texte_tuto.setVisibility(View.VISIBLE);
            } else {
                ssv.resumeScrolling();
                icon_record_button.setImageResource(R.drawable.pause);
            }
        });



        resumeButton.setVisibility(View.GONE); // only show when paused

        // Confirm button:
        selectionConfirmButton = (CardView) view.findViewById(R.id.selection_confirm);

        selectionConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ssv.confirmSelection();
                Bitmap bitmap = getResizedBitmap(ssv.getBitmap(), 224);
                execute_inference(bitmap);
            }
        });

        selectionConfirmButton.setEnabled(false); // only enable when a selection has been made

        // Cancel button:
        selectionCancelButton = (CardView) view.findViewById(R.id.selection_cancel);

        selectionCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ssv.cancelSelection();
            }
        });

        selectionCancelButton.setEnabled(false); // only enable when a selection has been made

        // get references to all text views so they can be updated:
        //leftTimeTextView = (TextView) view.findViewById(R.id.time_text_left);
        //rightTimeTextView = (TextView) view.findViewById(R.id.time_text_right);
        bottomFreqTextView = (TextView) view.findViewById(R.id.freq_text_bottom);
        topFreqTextView = (TextView) view.findViewById(R.id.freq_text_top);

        // Capture button container (invisible until a capture is made):
        captureButtonContainer = (LinearLayout) view.findViewById(R.id.capture_button_container);
        captureButtonContainer.setVisibility(View.INVISIBLE);

        // Set the contents of the two invariant text views:
        bottomFreqTextView.setText("0 kHz");
        //rightTimeTextView.setText("0 sec");

        Thread t_pause = new Thread(()->{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(100);
                        if(ssv == null) Log.w("init() FragmentAudio", "ssv est null !");
                        ssv.pauseScrolling();
                        Log.w("init() FragmentAudio", "Le scrolling a été arreté");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
        });
        t_pause.setDaemon(true);
        t_pause.start();


    }




    private void retourModeCapture() {
        listeCartes.setVisibility(View.GONE);
        boutonRetour.setVisibility(View.GONE);
        chargement_inference.setVisibility(View.GONE);
        carte1.setClickable(false);
        carte2.setClickable(false);
        carte3.setClickable(false);
        resultatPhoto.setVisibility(View.GONE);
        //on rermet la barre de menu
        BottomNavigationView barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.VISIBLE);
        texte_tuto.setVisibility(View.GONE);
        record.setVisibility(View.VISIBLE);
    }






    private void execute_inference(Bitmap bitmap) {

        Thread t = new Thread(()->{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // code pour cacher tous les elements d'interaction + affichage du chargement
                    record.setVisibility(View.GONE);
                    captureButtonContainer.setVisibility(View.GONE);
                    cadre_spectrograme.setVisibility(View.INVISIBLE);
                    chargement_inference.setVisibility(View.VISIBLE);
                    texte_tuto.setVisibility(View.GONE);

                    resultatPhoto.setVisibility(View.VISIBLE);
                    resultatPhoto.setImageBitmap(bitmap);
                    resultatPhoto.requestLayout();
                }
            });

            try{
                if(classifieur == null) throw new Throwable("classifieur est null");
                results = classifieur.classifyFrame(bitmap);
            } catch (Throwable throwable){
                throwable.printStackTrace();
                Log.e("ThreadResultats", "Le modele n'est pas chargé");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Erreur !");
                        alertDialog.setIcon(R.drawable.ic_info_outline_black_32dp);
                        alertDialog.setMessage("Analyse impossible, il est possible que le réseau de neurones servant à l'analyse des chants d'oiseaux ne soit pas installé");
                        // Alert dialog button
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Alert dialog action goes here
                                        // onClick button code here
                                        dialog.dismiss();// use dismiss to cancel alert dialog
                                    }
                                });
                        alertDialog.show();

                        // on retourne en mode capture au cas où l'user remet du réseau
                        retourModeCapture();
                    }
                });
            }


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boutonRetour.setVisibility(View.VISIBLE);
                    chargement_inference.setVisibility(View.GONE);

                    listeCartes.setVisibility(View.VISIBLE);

                    set_infos_cartes(carte1, titreCarte1, sureteCarte1, descriptionCarte1, imageCarte1, (String) results.get(0).get(0), (Double) results.get(0).get(1));
                    set_infos_cartes(carte2, titreCarte2, sureteCarte2, descriptionCarte2, imageCarte2, (String) results.get(1).get(0), (Double) results.get(1).get(1));
                    set_infos_cartes(carte3, titreCarte3, sureteCarte3, descriptionCarte3, imageCarte3, (String) results.get(2).get(0), (Double) results.get(2).get(1));

                }
            });


        });

        t.start();
    }




    private void set_infos_cartes(CardView carte, TextView titreCarte, TextView sureteCarte, TextView descriptionCarte, ImageView imageCarte, String resultat, Double surete){
        try{
            if(surete > 0.001){
                ArrayList<String> details_resultats = UtilitaireResultat.getMushInfo(resultat, getActivity());

                Log.e("set_infos_cartes", "UtilitaireResultat.getMushInfo() : " + details_resultats.toString());

                titreCarte.setText(details_resultats.get(0) /*details_resultats.get(0)*/);

                sureteCarte.setText(""+ String.format("%4.2f", (surete.doubleValue()*100)) + "% de sureté");
                descriptionCarte.setText((details_resultats.get(1)));
                //Bitmap image = BitmapFactory.decodeFile(details_resultats.get(2));
                //imageCarte.setImageBitmap(image);
                try{
                    imageCarte.setImageDrawable(getDrawable(resultat));
                } catch (Throwable t){
                    t.printStackTrace();
                    try{
                        imageCarte.setImageDrawable(getDrawable("aucun_apercu_dispo"));
                    } catch (Throwable tt){
                        tt.printStackTrace();
                        Log.e("set_infos_cartes()", "Impossible de charger une image d'aperçu");
                    }

                }

                carte.setClickable(true);
                carte.setOnClickListener((v) -> {
                    try{
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(details_resultats.get(2))));
                    } catch(Throwable t){
                        // S'il n'y a rien à l'index 2, ça veut probablement dire que la plante n'est pas dans la base de données et que le lien est donc en 1ere position
                        try{
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(details_resultats.get(0))));
                        } catch (Throwable t2){
                            Toast.makeText(getContext(), "Le lien vers wikipedia pour cette espèce n'a pas pu etre trouvé", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
            } else {
                carte.setVisibility(View.GONE);
            }

        } catch (Throwable t){
            t.printStackTrace();
        }




    }


    /** Méthode pour extraire une image du dossier drawable
     *
     * (version modifié d'une méthode donnée ici : https://stackoverflow.com/questions/16369814/how-to-access-the-drawable-resources-by-name-in-android)
     *
     * @param name (nom du drawable)
     * @return (un drawable)
     */
    public Drawable getDrawable(String name) {
        Context context = getActivity().getBaseContext();
        int resourceId = context.getResources().getIdentifier(name, "drawable", getActivity().getBaseContext().getPackageName());
        return context.getResources().getDrawable(resourceId);
    }

    /**
     * Méthode pour redimentionner un bitmap avec comme résultat une image du meme ratio dont le coté
     * le plus long et de longueur maxSize
     *
     * Source: https://stackoverflow.com/questions/15759195/reduce-size-of-bitmap-to-some-specified-pixel-in-android
     *
     * /!\
     * Cette méthode freeze parfois, surement à cause de la quantité de mémoire qui est demandée pour gérer la photo prise
     * TODO : Régler ce problème car c'est bloquant si le tel a pas assez de mémoire
     * /!\
     *
     * @param image (image à redimentionner)
     * @param maxSize (taille de coté max voulue)
     * @return (bitmap redimentionné)
     */
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        BitmapDrawable bd = new BitmapDrawable(getResources(), image);
        bd.setBounds(0, 0, width, height);

        //return bd.getBitmap();
        return Bitmap.createScaledBitmap(image, width, height, true);
    }



    private void setupMediaRecorder(){
        recorder = new MediaRecorder();
        //On utilise VOICE_RECOGNITION pour éliminer tous les bruits parasytes
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        recorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
        //recorder.setAudioEncodingBitRate(128000);
        recorder.setAudioSamplingRate(16);
        //durée enregistrement maximale = 20 secondes
        recorder.setMaxDuration(20000);
        recorder.setAudioChannels(NUMBER_CHANNEL);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(SAMPLE_RATE);

        recorder.setOutputFile(fichierSound.getAbsolutePath());
        try{
            recorder.prepare();
        }catch (IOException exception) {
            exception.printStackTrace();
        }

        //Si la durée max (20 secondes) de la vidéo est atteinte, on arrête la vidéo.
        recorder.setOnInfoListener((MediaRecorder mr, int what, int extra) -> {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                //On réinitialise le remplissage de la barre circulaire du timer (remis à 0)
                circleProgress.setProgress(0);
                recorder.stop();
                recorder.reset();

                //on redirige l'utilisateur vers le fragment visualisation audio pour qu'il écoute son enregistrement
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                FragmentVisualisationAudio fragmentVisualisationAudio = new FragmentVisualisationAudio();

                Bundle arguments = new Bundle();

                arguments.putString("chemin_audio", fichierSound.getPath());

                fragmentVisualisationAudio.setArguments(arguments);

                fragmentTransaction.replace(R.id.contenu_fragment, fragmentVisualisationAudio);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

    }

    private void createSoundFolder() {
        String cheminDossier = NAME_DIRECTORY;

        dossierSounds = new File(cheminDossier);

        if (!dossierSounds.exists()) {
            dossierSounds.mkdirs();
        }
    }


    private void createSoundFile() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Sounds/";

        StringBuffer nomFichier = new StringBuffer(NAME_FILE_ID);
        nomFichier.append(new Date().getTime());
        nomFichier.append(RECORDER_FORMAT);

        fichierSound = new File(cheminDossier, nomFichier.toString());
    }


    private void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(),new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permission, @NonNull int[]grantResults){
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(),"Permission Denied", Toast.LENGTH_SHORT ).show();
            }
            break;
        }
    }

    private boolean checkPermissionFromDevice(){
        int writeExternalStorageResult = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordAudioResult = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);

        return writeExternalStorageResult == PackageManager.PERMISSION_GRANTED && recordAudioResult == PackageManager.PERMISSION_GRANTED;
    }










    public void pauseScrolling() {
        if (ssv != null) {
            ssv.pauseScrolling();
        }
    }

    /**
     * Displays and enables the buttons held in the capture button container.
     */
    public void enableCaptureButtonContainer() {
        captureButtonContainer.setVisibility(View.VISIBLE);
        selectionConfirmButton.setEnabled(true);
        selectionCancelButton.setEnabled(true);
    }

    /**
     * Hides and disables the buttons held in the capture button container.
     */
    public void disableCaptureButtonContainer() {
        captureButtonContainer.setVisibility(View.GONE);
        selectionConfirmButton.setEnabled(false);
        selectionCancelButton.setEnabled(false);
    }

    /**
     * Calculates the new position for the capture button container given the new dimensions
     * of the selection rectangle.
     *
     * @param selectRectL
     * @param selectRectT
     * @param selectRectR
     * @param selectRectB
     */
    public void moveCaptureButtonContainer(float selectRectL, float selectRectT, float selectRectR, float selectRectB) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)captureButtonContainer.getLayoutParams();

        int lowestDimension = (selectRectT < selectRectB) ? (int)selectRectB : (int)selectRectT;
        int highestDimension = (selectRectT < selectRectB) ? (int)selectRectT : (int)selectRectB;
        int leftmostDimension = (selectRectL < selectRectR) ? (int)selectRectL : (int)selectRectR;

        int halfDifference = (int)Math.abs((selectRectL-selectRectR)/2); // half the width of the selection rectangle
        int leftMargin = leftmostDimension + halfDifference - captureButtonContainer.getWidth()/2; // absolute x-coord for container centre

        int yDimension;
        if (ssv.getHeight() - lowestDimension < UiConfig.CAPTURE_BUTTON_CONTAINER_MIN_HEIGHT + 10)
            // if y-dimension is getting too low then flip and draw the container on the other side of the selection rectangle
            yDimension = highestDimension - UiConfig.CAPTURE_BUTTON_CONTAINER_MIN_HEIGHT - 10;
        else
            yDimension = lowestDimension + 10;

        if (leftmostDimension + UiConfig.CAPTURE_BUTTON_CONTAINER_MIN_WIDTH > ssv.getWidth())
            // if right side is getting too close to the border then shift leftwards until it fits
            leftmostDimension = ssv.getWidth()-UiConfig.CAPTURE_BUTTON_CONTAINER_MIN_WIDTH;

        // set margins so that the container is positioned appropriately:
        params.setMargins(leftMargin, (int)(yDimension + UiConfig.SELECT_RECT_CORNER_RADIUS/2) /* make it half-way clear of the corner */, 0, 0);
        captureButtonContainer.setLayoutParams(params);
    }

    public void enableResumeButton() {
        //resumeButton.setVisibility(View.VISIBLE);
        mode_rec = false;
    }

    public void disableResumeButton() {
        //resumeButton.setVisibility(View.GONE);
        mode_rec = true;
    }

    /**
     * Displays the provided time as a decimal in the left time text view.
     * @param timeInSec
     */
    public void setLeftTimeText(float timeInSec) {
        BigDecimal bd = new BigDecimal(Float.toString(timeInSec));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); //round to 2 dp
        //leftTimeTextView.setText(bd.floatValue()+" sec");
    }

    /**
     * Displays the provided time as a decimal in the right time text view.
     * @param timeInSec
     */
    public void setRightTimeText(float timeInSec) {
        BigDecimal bd = new BigDecimal(Float.toString(timeInSec));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); //round to 2 dp
        //rightTimeTextView.setText(bd.floatValue()+" sec");
    }

    /**
     * Displays the provided frequency as a decimal in the top frequency text view.
     * @param freqInKHz
     */
    public void setTopFreqText(float freqInKHz) {
        BigDecimal bd = new BigDecimal(Float.toString(freqInKHz));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); //round to 2 dp
        topFreqTextView.setText(bd.floatValue()+" kHz");
    }

    public void setLocationClient(LocationClient lc) {
        ssv.setLocationClient(lc);
    }

    //Bottom freq is always 0 kHz

}