package descartes.info.l3ag2.eyetrek.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.AutoFitTextureView;
import descartes.info.l3ag2.eyetrek.classes.CameraUtility;
import descartes.info.l3ag2.eyetrek.classes.CompareAire;
import descartes.info.l3ag2.eyetrek.classes.FileUtils;
import descartes.info.l3ag2.eyetrek.classes.ImageSauvegarde;
//import descartes.info.l3ag2.eyetrek.tensorflow.Classifier;
import descartes.info.l3ag2.eyetrek.classes.NetworkUtils;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireModels;
import descartes.info.l3ag2.eyetrek.classes.UtilitaireResultat;
import descartes.info.l3ag2.eyetrek.interfaces.IOnBackPressed;
import descartes.info.l3ag2.eyetrek.tensorflow.ImageClassifier;
import descartes.info.l3ag2.eyetrek.tensorflow.ImageClassifierCpasC;
import descartes.info.l3ag2.eyetrek.tensorflow.ImageClassifierFpasF;
import pl.droidsonroids.gif.GifImageView;
//import descartes.info.l3ag2.eyetrek.tensorflow.TensorFlowImageClassifier;

public class FragmentScanChampignon extends Fragment implements IOnBackPressed {

    SharedPreferences sharedPreferences;
    private static final String PREFS = "app_prefs";
    String insertUrl = "http://www.ens.math-info.univ-paris5.fr/~ij00084/saveMushroom.php";
    RequestQueue requestQueue;

    private final int NB_RESULTATS = 3;

    /* === === Variables fonction vidéo === === */

    // Bouton pour passer en mode video
    private Switch changementCamToVid;
    // Texte pour le changement de la camera à la video
    private TextView textChangeCamToVid;
    // Texte pour dire la durée max d'un enregistrement
    private CardView textDureeMaxRecord;
    // Le fichier correspondant à la vidéo enregistrée
    private File fichierVideo;
    // Le dossier où seront stockées les vidéos
    private File dossierVideo;
    // Bouton pour prendre une video
    private ImageButton boutonVideo;
    // Renseigne si l'enregistrement de la vidéo est en cours ou pas
    private boolean isRecord;
    // Remplissage progressif des 10 sec maximale de la video
    private ProgressBar circleProgress;
    // Objet qui gerera et contiendra l'enregistrement video
    private MediaRecorder mediaRecorder;
    //Taille de la surface de la vidéo.
    private Size tailleSurfaceVideo;


    // Contient l'image qui sera traitée par l'algo
    Bitmap bitmap;

    // Options d'encodage du bitmap
    BitmapFactory.Options bmOptions = new BitmapFactory.Options();



    /* === === Variables classifieur === === */

    private ImageClassifier classifieur = null;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input_1"; // Qui va passer ses layers à un "node conv2d_1/convolution"
    private static final String OUTPUT_NAME = "dense_2/Softmax";
    //private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String MODEL_FILE = "file:///android_asset/MS_48c_4.tflite";
    //private static final String LABEL_FILE =
    //      "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final String LABEL_FILE = "file:///assets/labelstest.txt";
    private Bitmap croppedBitmap = null;
    // Liste des résultats de l'algorithme
    ArrayList<ArrayList<Object>> results;
    // Booleen qui dit si la liste des résultats est prete
    private boolean resultats_prets = false;
    // Gif de chargement du modele
    private GifImageView chargement_modele;
    // Gif de chargement de l'inférence
    private GifImageView chargement_inference;

    /* -- -- Variables classifieur feuille/pas feuille -- -- */

    private final Object lock = new Object();
    private boolean runClassifierFpasF = false;
    private TextView textViewFpasF;
    private ImageClassifierCpasC classifieurCpasC;
    // Pour savoir si il faut executer ou pas ce classifieur
    private boolean execution_classifieurFpasF = true;

    // cercle de couleur autour du bouton de capture pour dire si la feuille est valide
    private ImageView couleur_validite_feuille;
    // CardView qui contient couleur_validite_feuille (on doit l'avoir pour le cacher une fois qu'on a les resultats, sinon il va rester un cercle blanc)
    private CardView card_couleur_validite_feuille;
    // texte au dessus du bouton de capture qui parle de la validité de la feuille
    private TextView validite_feuille;
    // Message pour dire à l'user de ne pas bouger quand il prend la photo
    private CardView message_pas_bouger;


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
    private int resultatPhotoHeight = 0;
    private int resultatPhotoWidth = 0;

    /* === === Variables camera === === */

    // La surface dans laquelle on peut voir ce qu'on va prendre en photo.
    private AutoFitTextureView surfaceCamera;
    // Représente la caméra active.
    private CameraDevice camera;
    // Identifiant de la caméra active.
    private String idCamera;
    // Renseigne si le flash est supporté ou non par l'appareil.
    private boolean isFlashSupported;
    // Taille de la surface de la retransmission caméra.
    private Size tailleSurfaceCamera;

    // Largeur maximale supportée par l'API Camera 2
    private static final int MAX_PREVIEW_WIDTH = 1920;
    // hauteur maximale supportée par l'API Camera 2
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    // ImageReader qui contient l'image capturée et gère la capture d'image fixe.
    private ImageReader containerImage;
    // Ce listener est appelée quand une image est prête à être sauvegardée.
    private ImageReader.OnImageAvailableListener imageDisponibleListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            createImageFile();
            //On execute la sauvegarde de l'image en arriere-plan.
            arrierePlanHandler.post(new ImageSauvegarde(reader.acquireNextImage(), fichierImage));
        }
    };

    // Pointeur vers le fichier correspondant à l'image capturée
    private File fichierImage;
    // On crée le dossier contenant les images capturées s'il n'existe pas
    private File dossierImage = new File(Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/");


    // Un thread qui permet d'executer des taches sans bloquer l'interface utilisateur.
    private HandlerThread arrierePlanThread;
    // Pour exécuter des taches en arriere plan
    private Handler arrierePlanHandler;

    // Listener appelé à chaque changement d'états de la camera
    private CameraDevice.StateCallback cameraEtatListener = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camera = cameraDevice;
            //InputConfiguration inputConfiguration = new InputConfiguration(INPUT_SIZE, INPUT_SIZE, ImageFormat.YUV_420_888);
            //camera.createReprocessableCaptureSession(inputConfiguration, );
            commencerVisualisation();
        }
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            camera.close();
            camera = null;
        }
        @Override
        public void onError(CameraDevice cameraDevice, int erreur) {
            camera.close();
            camera = null;
            Log.e("cameraEtatListener", "Erreur sur le cameraEtatListener");
            // TODO Peut etre essayer de rattraper l'erreur en redemarrant la camera pour éviter un crash ?
        }
    };
    // Etat courant pour la procédure de capture d'image
    private int etat = CameraUtility.ETAT_APERCU_CAM;

    // Une session pour la retransmission de la camera et pour envoyer des CaptureRequest.
    // > elle sera initialisée plus tard, lors de l'appel de la méthode commencerVisualisation()
    private CameraCaptureSession sessionCapture;

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        /**
         * Permet de contrôler les résultats des différentes CaptureRequest émises dans la session.
         *
         * @param result résultat
         */
        private void checkEtat(CaptureResult result) throws Exception {
            switch (etat) {
                // Si on est juste en mode aperçu
                case CameraUtility.ETAT_APERCU_CAM:
                    break;

                // Si on est en attente du bloquage du focus POUR PRENDRE UNE PHOTO
                case CameraUtility.ETAT_ATTENTE_LOCK_FOCUS:
                    //On récupère des informations concernant l'état de l'auto-focus.
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);

                    //L'état de l'auto-focus n'est pas défini.
                    if (afState == null) {
                        captureImage();
                    }

                    //L'état de l'auto-focus est défini. Il est soit vérouillé soit dévérouillé.
                    else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        //On récupère des informations concernant l'état de l'auto-exposition.
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        //Auto-exposition a un bon ensemble de valeurs de contrôle pour la scène actuelle ou il n'est pas défini
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            etat = CameraUtility.ETAT_IMAGE_PRISE;
                            captureImage();
                        } else {  //on détermine la séquence de mesure de précapture de l'auto-exposition (AE).
                            try {
                                /*
                                   Déclenchement du controle de l'auto-exposition

                                   On définit une CaptureRequest pour le déclenchement de la séquence de mesure de
                                   précapture de l'auto-exposition (AE).

                                   Permet d'affecter à la clé "CONTROL_AF_TRIGGER" de la captureRequest
                                   la  valeur "CONTROL_AF_TRIGGER_CANCEL"

                                 */
                                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

                                //on change l'état de la capture.
                                etat = CameraUtility.ETAT_ATTENTE_PRECAPTURE;

                                /*
                                    Dans la session active, on soumet une requête à la caméra pour capturer une image
                                    avec les paramètres définis dans la captureRequest (c'est à dire : déclenchement de
                                    la séquence de mesure de précapture de l'auto-exposition (AE)).
                                    Chaque requête (CaptureRequest) produit un CaptureResult qui va être traité dans le captureCallBack.
                                 */
                                sessionCapture.capture(captureRequestBuilder.build(), captureCallback, arrierePlanHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;

                case CameraUtility.ETAT_ATTENTE_PRECAPTURE:
                    //On récupère des informations concernant l'état de l'auto-exposition.
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        etat = CameraUtility.ETAT_ATTENTE_NO_PRECAPTURE;
                    }
                    break;

                case CameraUtility.ETAT_ATTENTE_NO_PRECAPTURE:
                    //On récupère des informations concernant l'état de l'auto-exposition.
                    aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        etat = CameraUtility.ETAT_IMAGE_PRISE;
                        captureImage();
                    }
                    break;
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            try {
                checkEtat(partialResult);
                Log.d("captureCallback : ", "la méthode onCaptureProgressed() est appellée");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            try {
                checkEtat(result);
                //Log.d("captureCallback : ", "la méthode onCaptureCompleted() est appellée");
                // >> Elle est appellée en continue quand la camera est ON
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    //"Constructeur" de CaptureRequest pour l'aperçu de la caméra.
    private CaptureRequest.Builder captureRequestBuilder;

    // Bouton pour choisir une image dans la galerie
    private ImageButton boutonGalerie;

    // Bouton pour prendre une photo
    private ImageButton boutonCapture;

    //Permet de gérer les differents états de la surface de retransmission de la caméra.
    private TextureView.SurfaceTextureListener surfaceCameraListener = new TextureView.SurfaceTextureListener() {
        /**
         * Lorsque la TextureView est prête, nous initialisons la caméra.
         * Permet de récupérer la largeur et la hauteur de la surface de retransmission
         * quand elle est disponible.
         * @param surfaceTexture surface de retransmission
         * @param largeur largeur de la surface
         * @param hauteur hauteur de la surface
         */
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int largeur, int hauteur) {
            miseEnPlaceCamera(largeur, hauteur);
            connecterCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int largeur, int hauteur) {
            //TODO Coder la méthode dans le cas où les dimensions de la surface d'apercu de la photo changent
        }

        /**
         * Aucun rendu n'est présent à l'intérieur de la surface
         * après que cette méthode ait été invoquée.
         * @param surfaceTexture
         * @return false
         */
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            //TODO Coder la méthode dans le cas où on veut supprimer le contenu de la surface
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            //TODO Est ce qu'on est obligé d'Override cette méthode ?
        }
    };
















    // Le constructeur du fragment doit être vide
    public FragmentScanChampignon() {}

    /**
     * Méthode pour instancier les objets non graphiques
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // createImageFolder();

        //createVideoFolder();

        // tensorflow
        //load up our saved model to perform inference from local storage
        loadModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        //On démarre le thread pour effectuer toutes les opérations de la caméra
        demarreThreadArrierePlan();

        /*
            La surface de visualisation est disponible donc onSurfaceTextureAvailable ne sera pas
            appelée. On peut directement ouvrir la camera.
        */
        if (surfaceCamera.isAvailable()) {
            miseEnPlaceCamera(surfaceCamera.getWidth(), surfaceCamera.getHeight());
            connecterCamera();
        } else {
            surfaceCamera.setSurfaceTextureListener(surfaceCameraListener);
        }
    }

    @Override
    public void onPause() {
        fermerCamera();
        stopThreadArrierePlan();
        super.onPause();
    }

    /**
     * Méthode pour instacier les objets graphiques
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Chargement du noeud parent du fichier layout fragment_analyse.xml
        View view = inflater.inflate(R.layout.fragment_scan_feuille, container, false);

        // On met l'encodage à ARGB_8888 car c'est le seul que TensorFlow supporte
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // On mlet les dimentions qu'ils faut
        bmOptions.outWidth = INPUT_SIZE;
        bmOptions.outHeight = INPUT_SIZE;


        //On instancie les composants graphiques avec findViewById

        // Surface où s'affiche le flux d'images de la camera
        surfaceCamera = (AutoFitTextureView) view.findViewById(R.id.surface_camera);
        // Bouton pour prendre la photo
        boutonCapture = view.findViewById(R.id.bouton_capture);
        boutonCapture.setClickable(false); // on le désactive le temps que le CNN se charge
        boutonCapture.setEnabled(false);
        // Bouton pour choisir une photo dans la galerie
        boutonGalerie = view.findViewById(R.id.bouton_galerie);
        // Bouton pour prendre une video
        boutonVideo = view.findViewById(R.id.bouton_video);
        // Bouton pour passer en mode video
        changementCamToVid = view.findViewById(R.id.changement_vid_cam);
        // Texte pour le changement de la camera à la video
        textChangeCamToVid = view.findViewById(R.id.text_switch);
        // Texte pour dire la durée max d'un enregistrement
        textDureeMaxRecord = view.findViewById(R.id.text_duree);
        textDureeMaxRecord.setVisibility(View.GONE);
        // Barre de progression pour voir l'avancement jusqu'à la limite des 10sec max de video
        circleProgress = view.findViewById(R.id.barre_recordVideo);
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

        // Image avec le résultat de la photo qui a été prise
        resultatPhoto = view.findViewById(R.id.resultat_photo);
        resultatPhoto.setVisibility(View.GONE);
        // Gif de chargement du modele
        chargement_modele = view.findViewById(R.id.chargement_modele);
        // Gif de chargement de l'inférence
        chargement_inference = view.findViewById(R.id.chargement_inference);
        chargement_inference.setVisibility(View.GONE);
        // cercle de couleur autour du bouton de capture pour dire si la feuille est valide
        couleur_validite_feuille = view.findViewById(R.id.couleur_validite_feuille);
        couleur_validite_feuille.setVisibility(View.VISIBLE);
        // texte au dessus du bouton de capture qui parle de la validité de la feuille
        validite_feuille = view.findViewById(R.id.validite_feuille);
        // On commence par afficher que la feuille est invalide (avant le chargement du modele qui en fera la verification)
        afficher_validite("...", false);
        // CardView qui contient couleur_validite_feuille (on doit l'avoir pour le cacher une fois qu'on a les resultats, sinon il va rester un cercle blanc)
        card_couleur_validite_feuille = view.findViewById(R.id.card_couleur_validite_feuille);
        // Message pour dire à l'user de ne pas bouger quand il prend la photo
        message_pas_bouger = view.findViewById(R.id.card_message_pas_bouger);
        message_pas_bouger.setVisibility(View.GONE);

        //Click sur le bouton de capture
        boutonCapture.setOnClickListener((v) -> {
            try { // Pas trop grave si cette ligne de code crash
                vibrate(10, getContext());
            } catch(Throwable t){
                t.printStackTrace();
            }
            message_pas_bouger.setVisibility(View.VISIBLE);
            /*
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                execution_classifieurFpasF = false;
                // On prend la photo
                prendrePhoto();
                resultats_prets = false;
                //mode_resultats();

            } else {
                Log.e("ERREUR", "Permission de stockage refusé !");
            }
            */
            execution_classifieurFpasF = false;
            bitmap = surfaceCamera.getBitmap(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
            ThreadResultats tr = new ThreadResultats();
            tr.setDaemon(true);
            tr.start();

        });

        boutonRetour.setOnClickListener((v) -> {
            retourModeCapture();
        });


        // Click sur le bouton de la galerie
        //Clique sur le bouton de la Galerie
        boutonGalerie.setOnClickListener((v) -> {
            execution_classifieurFpasF = false;
            ouvrirGalerie();
        });


        //Source : https://stackoverflow.com/questions/30766755/smooth-progress-bar-animation
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(circleProgress, "progress", 0, 100);
        //On configure la durée de l'animation à 10 secondes pour le remplissage du timer (barre circulaire)
        progressAnimator.setDuration(10000);
        //Le remplissage s'effectue de façon linéaire (c'est à dire en continue sans accélération)
        progressAnimator.setInterpolator(new LinearInterpolator());

        boutonVideo.setOnClickListener((v) -> {
            execution_classifieurFpasF = false;
            if (isRecord) {
                isRecord = false;
                boutonVideo.setBackground(getResources().getDrawable(R.drawable.ic_bouton_record_60dp, null));
                //on arrête le remplissage de la jauge du timer
                progressAnimator.cancel();
                stopperEnregistrementVideo();
            } else {
                isRecord = true;
                boutonVideo.setBackground(getResources().getDrawable(R.drawable.ic_bouton_stop_60dp, null));

                //on cache tous les éléments sauf le bouton principale d'enregistrement
                boutonGalerie.setVisibility(View.INVISIBLE);
                changementCamToVid.setVisibility(View.INVISIBLE);
                textChangeCamToVid.setVisibility(View.INVISIBLE);
                //settings.setVisibility(View.INVISIBLE);
                //on démarre le remplissage de la jauge du timer
                progressAnimator.start();
                commencerEnregistrementVideo();
            }
        });

        //Listener pour le switch de la camera vers la video
        changementCamToVid.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                //Mode vidéo
                isRecord = false;
                boutonCapture.setVisibility(View.GONE);
                boutonVideo.setVisibility(View.VISIBLE);
                boutonVideo.setBackground(getResources().getDrawable(R.drawable.ic_bouton_record_60dp, null));
                circleProgress.setVisibility(View.VISIBLE);
                textChangeCamToVid.setText(R.string.switch_text_video);
                textDureeMaxRecord.setVisibility(View.VISIBLE);
                couleur_validite_feuille.setVisibility(View.GONE);
                //rotation du bouton video
                //CameraUtility.flipIt(boutonVideo);
            } else {
                //Mode photo
                boutonVideo.setVisibility(View.GONE);
                circleProgress.setVisibility(View.GONE);
                boutonCapture.setVisibility(View.VISIBLE);
                textChangeCamToVid.setText(R.string.switch_text_photo);
                textDureeMaxRecord.setVisibility(View.GONE);
                couleur_validite_feuille.setVisibility(View.VISIBLE);
                //rotation du bouton de photo
                //CameraUtility.flipIt(boutonCapture);
            }
        });

        if (!dossierImage.exists()) dossierImage.mkdirs(); // On crée le dossier s'il n'existe pas


        // ... Code ...
        /*
        lancer_apercu_background();
        Button bouton_photo = new Button;
        List<String> resultat;

        bouton_photo.onClick((event)->{
            bouton_photo.setState("chargement");
            stopper_background();

            bitmap = get_photo();
            resultat = recognizeImage(bitmap);
            bouton_photo.setState("caché");

            afficher_resultats(resultat);
        });
        */

        if(this.getArguments() != null && this.getArguments().getString("image_path") != null){

            // On recupere la photo qui a été prise
            bitmap = BitmapFactory.decodeFile(this.getArguments().getString("image_path"), bmOptions);
            for(int i = 0; i < 5 && bitmap == null; i++){ // Ceci est un gros pansement degeulasse
                Log.e("onCreateView()", "Essai de recupération du fichier : Capture_" + ((int) (new Date().getTime() / 1000) - i) + ".jpg");
                String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";
                String nomFichier = "Capture_" + ((int) (new Date().getTime() / 1000) - i) + ".jpg";
                fichierImage = new File(cheminDossier, nomFichier);
                bitmap = BitmapFactory.decodeFile(fichierImage.getAbsolutePath(),bmOptions);
            }
            for(int i = 0; i < 120 && bitmap == null; i++){ // Ceci est un gros pansement degeulasse
                Log.e("onCreateView()", "Essai de recupération du fichier : CaptureVideo_" + ((int) (new Date().getTime() / 1000) - i) + ".jpg");
                String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";
                String nomFichier = "CaptureVideo_" + ((int) (new Date().getTime() / 1000) - i) + ".jpg";
                fichierImage = new File(cheminDossier, nomFichier);
                bitmap = BitmapFactory.decodeFile(fichierImage.getAbsolutePath(),bmOptions);
            }
            Log.e("onCaptureCompleted()", "appel de la méthode getResizedBitmap() de stackOverflow pour redimentionner le bitmap");
            bitmap = getResizedBitmap(bitmap, INPUT_SIZE);


            new ThreadResultats().start();
        }


        // On affiche un message d'avrtissement à chaque ouverture de cette fonction
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("ATTENTION AUX CHAMPIGNONS !");
        alertDialog.setIcon(R.drawable.ic_info_outline_black_32dp);
        alertDialog.setMessage(getString(R.string.dialog_mushrooms));
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


        // On retourne la vue à l'activitée ScanChampignon
        return view;
    }

    private void retourModeCapture() {
        changementCamToVid.setVisibility(View.VISIBLE);
        textChangeCamToVid.setVisibility(View.VISIBLE);
        listeCartes.setVisibility(View.GONE);
        boutonCapture.setVisibility(View.VISIBLE);
        boutonGalerie.setVisibility(View.VISIBLE);
        boutonRetour.setVisibility(View.GONE);
        resultatPhoto.setVisibility(View.GONE);
        execution_classifieurFpasF = true;
        validite_feuille.setVisibility(View.VISIBLE);
        couleur_validite_feuille.setVisibility(View.VISIBLE);
        card_couleur_validite_feuille.setVisibility(View.VISIBLE);
        message_pas_bouger.setVisibility(View.GONE);
        chargement_inference.setVisibility(View.GONE);
        carte1.setClickable(false);
        carte2.setClickable(false);
        carte3.setClickable(false);
        couleur_validite_feuille.setVisibility(View.VISIBLE);
        //on rermet la barre de menu
        BottomNavigationView barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.VISIBLE);
    }

    public void vibrate(long milliseconds, Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(milliseconds);
        }
    }


    @Override
    public boolean onBackPressed() {
        if(listeCartes != null
                && boutonCapture != null
                && boutonGalerie != null
                && boutonRetour != null
                && resultatPhoto != null
        ){
            retourModeCapture();
            return true;
        }
        return false;

    }


    private class ThreadResultats extends Thread {
        public ThreadResultats(){
            // constructeur vide ..
        }

        public void run(){
            try { sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    execution_classifieurFpasF = false;

                    boutonCapture.setVisibility(View.GONE);
                    boutonGalerie.setVisibility(View.GONE); // TODO Mettre un fondu ?

                    validite_feuille.setVisibility(View.GONE);
                    couleur_validite_feuille.setVisibility(View.GONE);
                    card_couleur_validite_feuille.setVisibility(View.GONE);

                    changementCamToVid.setVisibility(View.GONE);
                    textChangeCamToVid.setVisibility(View.GONE);

                    resultatPhoto.setVisibility(View.VISIBLE);
                    resultatPhoto.setImageBitmap(bitmap);
                    resultatPhoto.requestLayout();

                    // On récupere la largeur de l'écran de l'utilisateur
                    WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int screenWidth = size.x;

                    //dimentionne l'image pour que ça se cale bien
                    resultatPhoto.getLayoutParams().height = resultatPhotoHeight * screenWidth / resultatPhotoWidth;
                    resultatPhoto.getLayoutParams().width = screenWidth;

                    message_pas_bouger.setVisibility(View.GONE);
                    chargement_inference.setVisibility(View.VISIBLE);


                }
            });

            // Pour éviter que l'inférence ne ralentisse l'execution du thread qu'on vient de lancer
            try { sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

            //Bitmap bitmapp = surfaceCamera.getBitmap(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);
            //String textToShow = classifieurCpasC.classifyFrame(bitmap);

            // on va utiliser ces deux variables pour savoir quelle méthode fini en premiere
            AtomicBoolean reseau_fini = new AtomicBoolean(false);
            AtomicBoolean local_fini = new AtomicBoolean(false);
            AtomicBoolean erreur_reseau = new AtomicBoolean(false);
            AtomicBoolean local_erreur = new AtomicBoolean(false);

            // On va voir si on peut envoye l'image en ligne pourr accelerer l'inférence
            Log.e("ThreadResultats", "isNetworkAvailable() : " + NetworkUtils.isNetworkAvailable(getActivity()));
            Log.e("ThreadResultats", "checkActiveInternetConnection() : " + NetworkUtils.checkActiveInternetConnection(getActivity()));
            if(NetworkUtils.isNetworkAvailable(getActivity()) && NetworkUtils.checkActiveInternetConnection(getActivity())) {
                new Thread(() -> {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    String image_base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);


                    try {
                        /*
                        URL url = new URL("http://192.168.1.22:5000/predict/");
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("POST");
                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                        */

                        /*
                        InputStream inputStream = urlConnection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        */
                        String reponse_serv = NetworkUtils.sendPost("http://192.168.1.22:5000/predict/", image_base64);// bufferedReader.readLine();

                        /*
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                        writer.write("image/png;base64," + image_base64);
                        writer.flush();
                        writer.close();
                        out.close();

                        urlConnection.connect();
                        */

                        Log.e("ThreadResultats", "reponse serveur : " + reponse_serv);

                        String regex = "([\\[]+|)([\\w\\. -]*)([\\]]+|)";

                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(reponse_serv);

                        String reponse_serv_finale = "";

                        while (m.find()) {
                            // Get the matching string
                            reponse_serv_finale += m.group(2);
                        }

                        //reponse_serv = m.group(2);

                        Log.e("ThreadResultats", "reponse serveur traitee : " + reponse_serv_finale);


                        float[] labelProbArray = new float[classifieur.labelList.size()];
                        int iterateur = 0;

                        StringTokenizer st = new StringTokenizer(reponse_serv_finale, " ");
                        ArrayList<String> liste = new ArrayList<>();

                        while (st.hasMoreTokens()) {
                            labelProbArray[iterateur++] = Float.parseFloat(st.nextToken());
                        }
                        /*
                        for(String proba : reponse_serv_finale.split(" ")){
                            labelProbArray[iterateur++] = Float.parseFloat(proba);
                        }
                        */

                        classifieur.labelProbArray[0] = labelProbArray;
                        results = new ArrayList<ArrayList<Object>>();
                        results = classifieur.setTopKLabels(results);

                        reseau_fini.set(true);


                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("ThreadResultats", "Erreur lors de la connexion au serveur");
                        reseau_fini.set(false);
                        erreur_reseau.set(true);
                    }
                    if (results == null) {
                        Log.e("ThreadResultats", "Impossible d'obtenir des résultats en ligne");
                        reseau_fini.set(false);
                        erreur_reseau.set(true);
                    }
                }).start();

            } else if(classifieur == null){
                reseau_fini.set(false);
                erreur_reseau.set(true);
            } else {
                Log.e("ThreadResultats", "Aucune connexion internet disponible");
                reseau_fini.set(false);
                erreur_reseau.set(true);
            }

            new Thread(() -> {
                try{
                    results = classifieur.classifyFrame(bitmap);
                    local_fini.set(true);
                } catch (Throwable t){
                    local_erreur.set(true);
                    t.printStackTrace();
                    Log.e("ThreadResultats", "Le modele n'est pas chargé");
                }

            }).start();


            // boucle qui attend qu'une des deux méthodes d'analysee termine
            // si classifieur == null, la boucle tourne à l'infini -> l'execution du thread ne continue pas
            do{
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.w("ThreadResultats", "reseau_fini : " + reseau_fini.get());
                Log.w("ThreadResultats", "erreur_reseau : " + erreur_reseau.get());
                if(erreur_reseau.get() && local_erreur.get()){
                    // Si le modele pour l'analyse de feuilles n'a pas pu etre chargé, on lui dit qu'il ne pourra
                    // faire d'analyse que s'il a du réseau
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setTitle("Réseau de neurones manquant !");
                            alertDialog.setIcon(R.drawable.ic_info_outline_black_32dp);
                            alertDialog.setMessage(getString(R.string.dialog_nn_manquant));
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
            } while((!reseau_fini.get() && !local_fini.get()) && (!erreur_reseau.get() || !local_erreur.get()));






            //Log.e("ThreadResultats", classifieurCpasC.classifyFrame(bitmap));

            if(!erreur_reseau.get() || !local_erreur.get()){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                        String id = sharedPreferences.getString("id", null);
                        Toast.makeText(getActivity(), id, Toast.LENGTH_SHORT).show();
                        requestQueue = Volley.newRequestQueue(getContext());
                        boutonRetour.setVisibility(View.VISIBLE);
                        chargement_inference.setVisibility(View.GONE);

                        listeCartes.setVisibility(View.VISIBLE);


                        Log.e("ThreadResultats", results.toString());

                        set_infos_cartes(carte1, titreCarte1, sureteCarte1, descriptionCarte1, imageCarte1, (String) results.get(0).get(0), (Double) results.get(0).get(1));
                        set_infos_cartes(carte2, titreCarte2, sureteCarte2, descriptionCarte2, imageCarte2, (String) results.get(1).get(0), (Double) results.get(1).get(1));
                        set_infos_cartes(carte3, titreCarte3, sureteCarte3, descriptionCarte3, imageCarte3, (String) results.get(2).get(0), (Double) results.get(2).get(1));

                        StringRequest request = new StringRequest(Request.Method.POST, insertUrl, new com.android.volley.Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                System.out.println(response.toString());
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> parameters  = new HashMap<String, String>();
                                String l = (String) results.get(0).get(0);
                                parameters.put("id_user",id);
                                parameters.put("mushroom",l);



                                return parameters;
                            }
                        };
                        requestQueue.add(request);

                    }
                });
            }
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

    private void mode_resultats() {
        boutonCapture.setVisibility(View.GONE);
        boutonGalerie.setVisibility(View.GONE); // TODO Mettre un fondu ?



        while(!resultats_prets){
            Log.e("boutonCapture-> onClick", "Attente que la variable resultats_prets soit à true");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.e("boutonCapture-> onClick", "Les résultats sont prets, affichage ..");
        //afficher_resultats(results);
        boutonRetour.setVisibility(View.VISIBLE);

        resultatPhoto.setVisibility(View.VISIBLE);
        resultatPhoto.setImageBitmap(bitmap);
        resultatPhoto.requestLayout();
        Log.e("setOnClickListener()", "resultatPhoto.getLayoutParams().height -> " + resultatPhoto.getLayoutParams().height);
        Log.e("setOnClickListener()", "resultatPhoto.getLayoutParams().width -> " + resultatPhoto.getLayoutParams().width);

        // On récupere la largeur de l'écran de l'utilisateur
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        //dimentionne l'image pour que ça se cale bien
        resultatPhoto.getLayoutParams().height = resultatPhotoHeight * screenWidth / resultatPhotoWidth;
        resultatPhoto.getLayoutParams().width = screenWidth;
        Log.e("setOnClickListener()", "resultatPhoto.getLayoutParams().height -> " + resultatPhoto.getLayoutParams().height);
        Log.e("setOnClickListener()", "resultatPhoto.getLayoutParams().width -> " + resultatPhoto.getLayoutParams().width);
        resultatPhoto.setScaleType(ImageView.ScaleType.FIT_XY);

        // TODO Stopper le flux de la camera

        Log.e("mode_resultats()", "Visibilité de listeCartes: " + ((listeCartes.getVisibility() == View.VISIBLE)?"visible":"invisible"));

    }


    /**
     * Méthode qui va afficher les résultats de l'algorithme
     */
    private void afficher_resultats(List<String> results){
        Log.e("afficher_resultats()", "Affichage des résultats : " + results.toString());
        // Code ...
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    listeCartes.setVisibility(View.VISIBLE);
                    Log.e("afficher_resultats()run", "On met la liste de cartes en visible ..");
                }
            });
        }
        Log.e("afficher_resultats()", "On met la liste de cartes en visible ..");
        listeCartes.setVisibility(View.VISIBLE);
        titreCarte1.setText(results.get(0));
        descriptionCarte1.setText("Le texte de la description a bien été changé\nConfiance : " + results.get(0));
    }


    /**
     * Charger le modele pour la classification des feuilles
     */
    private void loadModel() {
        //The Runnable interface is another way in which you can implement multi-threading other than extending the
        // //Thread class due to the fact that Java allows you to extend only one class. Runnable is just an interface,
        // //which provides the method run.
        // //Threads are implementations and use Runnable to call the method run().
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    classifieurCpasC = new ImageClassifierCpasC(getActivity());
                    classifieur = new ImageClassifier(getActivity(), UtilitaireModels.MODELE_CHAMPIGNIONS_PATH, UtilitaireModels.LABELS_MODELE_CHAMPIGNIONS_PATH);

                    /*
                    classifieur = TensorFlowImageClassifier.create(
                            getActivity().getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    */
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if(boutonCapture == null){
                                    Log.e("loadModel()", "le bouton boutonCapture est null");
                                } else if(chargement_modele == null) {
                                    Log.e("loadModel()", "l'animation chargement_modele est null");
                                } else {
                                    boutonCapture.setClickable(true); // on le désactive le temps que le CNN se charge
                                    boutonCapture.setEnabled(true);
                                    chargement_modele.setVisibility(View.GONE);
                                }

                            }
                        });
                    }

                    Log.e("loadModel()", "modele chargé");
                } catch (final Exception e) {
                    e.printStackTrace();
                    if(classifieurCpasC == null){
                        throw new RuntimeException("Erreur pendant l'initialisation du classifieur ! (classifieurCpasC == null) ", e);
                    } else if(classifieur == null){
                        // Si le modele pour l'analyse de feuilles n'a pas pu etre chargé, on lui dit qu'il ne pourra
                        // faire d'analyse que s'il a du réseau
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(boutonCapture == null){
                                    Log.e("loadModel()", "le bouton boutonCapture est null");
                                } else if(chargement_modele == null) {
                                    Log.e("loadModel()", "l'animation chargement_modele est null");
                                } else {
                                    boutonCapture.setClickable(true); // on le désactive le temps que le CNN se charge
                                    boutonCapture.setEnabled(true);
                                    chargement_modele.setVisibility(View.GONE);
                                }

                                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                alertDialog.setTitle("Réseau de neurones manquant !");
                                alertDialog.setIcon(R.drawable.ic_info_outline_black_32dp);
                                alertDialog.setMessage(getString(R.string.dialog_nn_manquant));
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
                            }
                        });
                    } else {
                        throw new RuntimeException("Erreur pendant l'initialisation du classifieur !", e);
                    }

                }
            }
        }).start();
    }

    /**
     * Permet de définir le nom de chaque capture d'image qui doit être sauvegardée dans le dossier Pictures.
     * ATTENTION : ne crée aucune image
     */
    private void createImageFile() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";
        String nomFichier = "Capture_" + ((int) (new Date().getTime() / 1000)) + ".jpg";
        fichierImage = new File(cheminDossier, nomFichier);
    }



    /**
     * Permet de vérouiller le focus et lancer une requête de prise de photo dans la session active.
     */
    private void prendrePhoto() {
        try {
            Log.e("prendrePhoto()", "Appel de la méthode prendrePhoto()");
            /*
                Déclenchement de  vérouillage de l'autofocus
                On définit une CaptureRequest pour le vérouillage de l'auto-focus

                Permet d'affecter à la clé "CONTROL_AF_TRIGGER" de la captureRequest
                la  valeur "CONTROL_AF_TRIGGER_START"
             */
            //captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            // TODO Utile ? vu qu'on prend la photo que quand c'est focus

            //on change l'état de la capture pour qu'elle valide une éventuelle capture
            etat = CameraUtility.ETAT_ATTENTE_LOCK_FOCUS;

            /*
                Dans la session active, on soumet une requête à la caméra pour capturer une image
                avec les paramètres définis dans la captureRequest (c'est à dire : déclenchement du
                verrouillage du focus).
                Chaque requête (CaptureRequest) produit un CaptureResult qui va être traité dans le captureCallBack.

                imageDisponibleListener ?

                captureCallback va gerer les différentes étapes de la prise de photo à l'aide de la variable "etat"
                Quand la camera sera prete, il appellera la méthode captureImage() qui va gérer l'orientation de la camera et creer
                un nouvel objet du meme type que captureCallback, mettre en pause la prévisualisation et, à l'aide de sessionCapture,
                lancer ce nouvel objet qui va:
                    1. donner un nouveau path à la variable fichierImage
                > Au cours de la capture, arrierePlanHandler va s'occuper de l'enregistrement de l'image sur un autre thread
                    2. quand la capture sera terminée, traiter l'image tout juste créée

             */
            sessionCapture.capture(captureRequestBuilder.build(), captureCallback, arrierePlanHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet de dévérouiller le focus en lançant une requête dans la session active.
     * Cette méthode doit etre appelée quand la capture d'image est terminée.
     */
    private void unlockFocus() {
        try {
            /*
               Réinitialise l'état du l'auto-focus.
               Déclenchement du dévérouillage de l'auto-focus.

               On définit une CaptureRequest pour le vérouillage de l'auto-focus.

               Permet d'affecter à la clé "CONTROL_AF_TRIGGER" de la captureRequest
               la  valeur "CONTROL_AF_TRIGGER_CANCEL".
             */
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            //on active le flash si besoin
            CameraUtility.setAutoFlash(captureRequestBuilder, isFlashSupported);
            /*
                Dans la session active, on soumet une requête à la caméra pour capturer une image
                avec les paramètres définis dans la captureRequest (c'est à dire : déclenchement du
                dévérrouillage du focus).
                Chaque requête (CaptureRequest) produit un CaptureResult qui va être traité dans le captureCallBack.
             */
            sessionCapture.capture(captureRequestBuilder.build(), captureCallback, arrierePlanHandler);

            //on change l'état de la capture.
            etat = CameraUtility.ETAT_APERCU_CAM;
            //on réactive le mode de retransmission de la caméra
            sessionCapture.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, arrierePlanHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet de définir les paramètres de la surface de retransmission :
     * - initialiser la surface de retransmission de la caméra (hauteur et largeur),
     * - initialiser la session pour la retransmission de la caméra,
     * - initialiser le constructeur de CaptureRequest pour la retransmission de la caméra.
     * - définir le mode continu de l'auto-focus pour la retransmision de la caméra.
     */
    private void commencerVisualisation() {
        SurfaceTexture textureSurface = surfaceCamera.getSurfaceTexture();
        textureSurface.setDefaultBufferSize(tailleSurfaceCamera.getWidth(), tailleSurfaceCamera.getHeight());

        Surface surfaceVisualisation = new Surface(textureSurface);

        try {
            //on initialise le constructeur de requête pour la visualisation de la caméra.
            captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surfaceVisualisation);
            /*
                On crée une session pour la visualisation de la caméra, on définit la surface et
                le conteneur où l'on va garder l'image capturée.
            */
            camera.createCaptureSession(Arrays.asList(surfaceVisualisation, containerImage.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // La camera est deja fermée
                    if (camera == null) {
                        return;
                    }
                    /*
                        Quand la session est prête, on affiche la visusalisation de la camera.
                        On initialise la session avec la session configurée.
                     */
                    sessionCapture = session;
                    try {
                        /*
                           Auto-focus est permanent durant la visualisation de la camera.

                           Modifie continuellement la position de l'objectif pour tenter de fournir
                           un flux d'image constamment mis au point.

                           On définit une CaptureRequest pour l'activation continue de l'auto-focus.

                           Permet d'affecter à la clé "CONTROL_AF_MODE" de la captureRequest
                           la  valeur "CONTROL_AF_MODE_CONTINUOUS_PICTURE".
                         */
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // Le flash est automatiquement activé si necessaire.
                        CameraUtility.setAutoFlash(captureRequestBuilder, isFlashSupported);
                        /*
                            Dans la session active, on soumet une requête à la caméra pour la
                            retransmission de la caméra avec les paramètres définis dans la
                            captureRequest (c'est à dire : le focus en mode continue).
                            Chaque requête (CaptureRequest) produit un CaptureResult qui va être
                            traité dans le captureCallBack.
                         */
                        session.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, arrierePlanHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getContext(), "Impossible de configurer la camera", Toast.LENGTH_SHORT).show();
                }
            }, arrierePlanHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /** Compares two {@code Size}s based on their areas. */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * Resizes image.
     *
     * Source: tutoriel officiel sur TensorFlow lite
     *
     * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
     * resulting in gorgeous previews but the storage of garbage capture data.
     *
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that is
     * at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size, and
     * whose aspect ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param textureViewWidth The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth The maximum width that can be chosen
     * @param maxHeight The maximum height that can be chosen
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(
            Size[] choices,
            int textureViewWidth,
            int textureViewHeight,
            int maxWidth,
            int maxHeight,
            Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth
                    && option.getHeight() <= maxHeight
                    && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("chooseOptimalSize()", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Permet de mettre en place la surface de retransmission de la caméra,
     * de déterminer l'identifiant de la caméra dorsale et de récupérer les
     * caractéristiques de la caméra.
     * <p>
     * Définit les variables des membres liées à la caméra.
     *
     * @param largeur largeur de la TextureView
     * @param hauteur hauteur de la TextureView
     */
    private void miseEnPlaceCamera(int largeur, int hauteur) {
        Activity activity = getActivity();
        /*
            CameraManager permet de gérer les différentes caméras de l'appareil.
            On peut récupérer les caractéristiques des camèras (dimensions,
            orientations...), le nombre de caméras présentes, le type de caméras.
         */
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            //La liste de tous les identifiants des caméras présentes sur le terminal
            String[] listIdCameras = cameraManager.getCameraIdList();

            /*
                On récupère tous les identifiants des cameras du terminal
                pour déterminer l'identifiant de la camera dorsale
            */
            for (String id : listIdCameras) {
                //Voici les caracteriques de chaque camera du terminal
                CameraCharacteristics caracteristiqueCamera = cameraManager.getCameraCharacteristics(id);

                /*
                    On regarde dans la caracteristique de chaque camera :
                    - s'il s'agit de la camera frontale, on passe à l'itération suivante.
                 */
                if (caracteristiqueCamera.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                /*
                    On récupère toutes les informations concernant tous les formats de sorties (et leurs tailles respectives)
                    qui sont supportés par la caméra.
                */
                StreamConfigurationMap map = caracteristiqueCamera.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) { // Si on arrive pas à avoir l'objet on reessaye pour plus de stabilité
                    continue;
                }

                //On instancie la dimension de surface de retransmission de la caméra
                tailleSurfaceCamera = new Size(largeur, hauteur);

                //On instancie la dimension de surface de la video (largeur <=1080 & format 4:3)
                tailleSurfaceVideo = CameraUtility.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));


                //On choisit la taille maximale pour l'image (correspond à largest sur Camera2BasicFragment)
                Size tailleImage = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareAire());

                containerImage = ImageReader.newInstance(tailleImage.getWidth(), tailleImage.getHeight(), ImageFormat.JPEG, /*maxImages*/1);
                // imageDisponibleListener va en continue actualiser le chemin et l'image de ImageSauvegarde
                // TODO Peut etre que la description est pas top ?
                // Mis en commentaire pour pas qu'il sauvegarde l'image au moment de prendre une photo
                //containerImage.setOnImageAvailableListener(imageDisponibleListener, arrierePlanHandler);


                // On regarde si le flash est disponible sur le terminal
                Boolean flashDispo = caracteristiqueCamera.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flashDispo) {
                    isFlashSupported = flashDispo;
                } else {
                    isFlashSupported = false;
                }




                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                // noinspection ConstantConditions
                /* Orientation of the camera sensor */
                int sensorOrientation = caracteristiqueCamera.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e("miseEnPlaceCamera()", "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = largeur;
                int rotatedPreviewHeight = hauteur;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = hauteur;
                    rotatedPreviewHeight = largeur;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                tailleSurfaceCamera =
                        chooseOptimalSize(
                                map.getOutputSizes(SurfaceTexture.class),
                                rotatedPreviewWidth,
                                rotatedPreviewHeight,
                                maxPreviewWidth,
                                maxPreviewHeight,
                                tailleImage);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    surfaceCamera.setAspectRatio(tailleSurfaceCamera.getWidth(), tailleSurfaceCamera.getHeight());
                } else {
                    surfaceCamera.setAspectRatio(tailleSurfaceCamera.getHeight(), tailleSurfaceCamera.getWidth());
                }



                resultatPhotoHeight = tailleSurfaceCamera.getWidth();
                resultatPhotoWidth = tailleSurfaceCamera.getHeight();
                Log.e("miseEnPlaceCamera()", "tailleSurfaceCamera.getWidth() -> " + tailleSurfaceCamera.getWidth());
                Log.e("miseEnPlaceCamera()", "tailleSurfaceCamera.getHeight() -> " + tailleSurfaceCamera.getHeight());







                //On instancie l'identifiant de la caméra
                idCamera = id;
                //on sort de la fonction sachant qu'on a trouvé l'identifiant de la caméra dorsale.
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode permettant d'ouvrir la caméra
     */
    private void connecterCamera() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(idCamera, cameraEtatListener, arrierePlanHandler);
            } else {
                Log.e("ERROR", "Échec de l'ouverture de la caméra");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Permet de fermer la caméra active, fermer la session de retransmission active et le conteneur
     * d'image pour la capture.
     */
    private void fermerCamera() {
        if (camera != null) {
            camera.close();
            camera = null;
        }

        if (null != sessionCapture) {
            sessionCapture.close();
            sessionCapture = null;
        }

        if (null != containerImage) {
            containerImage.close();
            containerImage = null;
        }

        if (null != mediaRecorder) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
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

    /**
     * Méthode pour effectuer une rotation sur une image.
     *
     * Source: https://stackoverflow.com/questions/9015372/how-to-rotate-a-bitmap-90-degrees
     *
     * @param source (bitmap à tourner)
     * @param angle (angle de rotation)
     * @return (un bitmap contenant l'image après rotation)
     */
    public static Bitmap getRotatedBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Capture une image. Cette méthode doit etre appelée après avoir vérouillé le focus.
     */
    private void captureImage() throws Exception {
        try {
            Activity activity = getActivity();
            if (activity == null || camera == null) {
                throw new Exception("La variable activity ou camera est null");
            }
            //on initialise le constructeur de requête pour la capture d'image.
            CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //on associe la surface
            captureBuilder.addTarget(containerImage.getSurface());

            // Auto-focus est permanent durant la visualisation de la camera.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraUtility.setAutoFlash(captureBuilder, isFlashSupported);

            /*
                Réglages de l'orientation de l'image prise :
                - On récupère la disposition de l'appareil
            */
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

            //On définit les parametres d'orientation de l'image de la CaptureRequete
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, CameraUtility.ORIENTATIONS_ANGLE_CAMERA.get(rotation));

            // On crée l'objet qui va gérer ce qui va se passer une fois que la photo sera prise
            CameraCaptureSession.CaptureCallback captureGestion = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.e("onCaptureStarted()", "Appel de la méthode onCaptureStarted()");
                    // >> Est appelé dès qu'on prend une photo
                    // Mis en commntrairr pour éviter qu'il sauvegardee l'image au moment de prendre la photo
                    //createImageFile();
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request,
                                               TotalCaptureResult result) {
                    Log.e("onCaptureCompleted()", "Appel de la méthode onCaptureCompleted()");
                    try{
                        unlockFocus();
                    } catch(Throwable t){
                        Log.e("onCaptureCompleted()", "Imoossible d'unlock le focus");
                        t.printStackTrace();
                    }

                    Log.e("onCaptureCompleted()", "Le focus a été unlock");
                    /*
                        Quand la capture est terminée, on doit afficher l'image capturée par l'utilisateur
                        pour qu'il accepte ou non de l'analyser
                        Il est donc redirigé vers un fragment contenant la visualisation de l'image
                        Il peut soit poursuivre l'analyse avec cette photo soit annuler (cela supprime la
                        photo prise)
                    */
                    /*

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    FragmentVisualisation fragment = new FragmentVisualisation();

                    Bundle arguments = new Bundle();
                    arguments.putString("chemin_photo", fichierImage.getPath());
                    fragment.setArguments(arguments);

                    fragmentTransaction.replace(R.id.contenu_fragment, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    */


                    // classifieur : variable avec notre classifieur à l'intérieur




                    try {
                        /*
                        // On recupere la photo qui a été prise

                        bitmap = BitmapFactory.decodeFile(fichierImage.getAbsolutePath(),bmOptions);
                        if(bitmap == null){ // Ceci est un gros pansement degeulasse
                            Log.e("onCaptureCompleted()", "Erreur lors de la recuperation du bitmap, 2e essai ..");
                            String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";
                            String nomFichier = "Capture_" + ((int) (new Date().getTime() / 1000) - 1) + ".jpg";
                            fichierImage = new File(cheminDossier, nomFichier);
                            bitmap = BitmapFactory.decodeFile(fichierImage.getAbsolutePath(),bmOptions);
                        }
                        if(bitmap == null) throw new Exception("Erreur, la variable bitmap est null");
                        Log.e("onCaptureCompleted()", "appel de la méthode getResizedBitmap() de stackOverflow pour redimentionner le bitmap");
                        bitmap = getResizedBitmap(bitmap, INPUT_SIZE);
                        bitmap = getRotatedBitmap(bitmap, 90);
                        */

                        bitmap = surfaceCamera.getBitmap(ImageClassifier.DIM_IMG_SIZE_X, ImageClassifier.DIM_IMG_SIZE_Y);

                        // TODO Tourner la photo en fonction de l'orientation de l'apareil

                        Log.e("onCaptureCompleted()", "Le bitmap a été créé");

                        /*

                        // On execute l'algorithme de reconnaissance
                        results = classifieur.classifyFrame(bitmap);

                        resultats_prets = true;
                        Log.e("onCaptureCompleted()", "La variable resultats_prets a été mise à true");
                        */

                        new ThreadResultats().start();


                    } catch(Exception e){
                        e.printStackTrace();
                    } catch(Throwable t){
                        t.printStackTrace();
                    }


                }
            };

            /*
            //on arrête la retransmission de la caméra
            sessionCapture.stopRepeating();
            //on interrompt toutes les capturesRequest en cours
            sessionCapture.abortCaptures();
            Log.e("captureImage()", "La capture de la visualisation a bien été stoppée");
            */
            /*
                Dans la session active, on soumet une requête à la caméra pour capturer une image
                avec les paramètres définis dans la captureRequest (c'est à dire : capture d'image
                fixe selon des dimensions et rotations définies).
                Chaque requête (CaptureRequest) produit un CaptureResult qui va être traité dans le gestionCapture.
             */
            sessionCapture.capture(captureBuilder.build(), captureGestion, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Arrête le thread en arriere plan.
     * Réinitialisation du thread et du handler.
     */
    private void stopThreadArrierePlan() {
        arrierePlanThread.quitSafely();
        try {
            arrierePlanThread.join();
            arrierePlanThread = null;
            arrierePlanHandler = null;
            synchronized (lock) {
                runClassifierFpasF = false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Démarre un nouveau thread en arriere plan pour la camera pour optimiser les opérations et éviter des
     * chargements supplémentaires.
     */
    private void demarreThreadArrierePlan() {
        arrierePlanThread = new HandlerThread("CameraThread");
        arrierePlanThread.start();
        arrierePlanHandler = new Handler(arrierePlanThread.getLooper());
        synchronized (lock) {
            runClassifierFpasF = true;
        }
        arrierePlanHandler.post(periodicClassifyFpasF);
    }


    /** Prends des photos et les classifies periodiquement */
    private Runnable periodicClassifyFpasF =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifierFpasF && execution_classifieurFpasF) {
                            classifyFrameFpasF();
                        }
                    }
                    arrierePlanHandler.post(periodicClassifyFpasF);
                }
            };


    /** Classifie une image du stream d'aperçu */
    private void classifyFrameFpasF() {
        //afficher_validite("...", false);
        if (classifieurCpasC == null || getActivity() == null || camera == null) {
            //Log.e("classifyFrameFpasF()", "classifieurCpasC == null || getActivity() == null || camera == null");
            return;
        }
        Bitmap bitmap =
                surfaceCamera.getBitmap(ImageClassifierFpasF.DIM_IMG_SIZE_X, ImageClassifierFpasF.DIM_IMG_SIZE_Y);
        String textToShow = classifieurCpasC.classifyFrame(bitmap);
        boolean isFeuilleValide = classifieurCpasC.getIsFeuilleValide();
        bitmap.recycle();
        afficher_validite(textToShow, isFeuilleValide);
    }

    private void afficher_validite(String textValidite, boolean isFeuilleValide) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                validite_feuille.setText(textValidite);
                if(isFeuilleValide){
                    couleur_validite_feuille.setImageResource(R.color.colorPrimary);
                } else {
                    couleur_validite_feuille.setImageResource(R.color.lowResult);
                }
            }
        });

    }










    /* === === Méthodes pour la prise de video === === */


    /**
     * Permet de créer le dossier Videos, s'il n'est pas déjà créé.
     */
    private void createVideoFolder() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Videos/";

        dossierVideo = new File(cheminDossier);

        if (!dossierVideo.exists()) {
            dossierVideo.mkdirs();
        }
    }

    /**
     * Permet de définir le nom de chaque video qui doit être sauvegardée dans le dossier
     * Videos
     */
    private void createVideoFile() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Videos/";

        StringBuffer nomFichier = new StringBuffer("Video_");
        nomFichier.append(new Date().getTime());
        nomFichier.append(".mp4");

        fichierVideo = new File(cheminDossier, nomFichier.toString());
    }

    /**
     * Démarre l'enregistrement de la vidéo
     */
    private void commencerEnregistrementVideo() {
        try {
            mediaRecorder = new MediaRecorder();
            //on créé le fichier où sera enregistrée la vidéo
            createVideoFile();
            //on initialise les paramètres du mediaRecorder
            setMediaRecorder();

            SurfaceTexture textureSurface = surfaceCamera.getSurfaceTexture();
            textureSurface.setDefaultBufferSize(tailleSurfaceCamera.getWidth(), tailleSurfaceCamera.getHeight());

            Surface surfaceVisualisation = new Surface(textureSurface);
            Surface surfaceVideo = mediaRecorder.getSurface();

            try {
                //on initialise le constructeur de requête pour l'enregistrement de video.
                captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                captureRequestBuilder.addTarget(surfaceVisualisation);
                captureRequestBuilder.addTarget(surfaceVideo);

                 /*
                    On crée une session pour l'enregistrement de video, on définit les surfaces
                  */
                camera.createCaptureSession(Arrays.asList(surfaceVisualisation, surfaceVideo), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        // La camera est deja fermée
                        if (camera == null) {
                            return;
                        }
                        /*
                            Quand la session est prête, on affiche la visusalisation de la camera.
                            On initialise la session avec la session configurée.
                         */
                        sessionCapture = session;
                        try {
                            /*
                               Activation du mode-auto (auto-exposition,
                               auto-balance des blancs, mise au point automatique)

                               Permet d'activer les 3 modes pendant l'enregistrement d'une vidéo.

                               On définit une CaptureRequest pour l'activation des 3 contrôles.

                               Permet d'affecter à la clé "CONTROL_MODE" de la captureRequest
                               la  valeur "CONTROL_MODE_AUTO".
                             */
                            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

                            /*
                                Dans la session active, on soumet une requête à la caméra pour
                                l'enregistrement d'une vidéo avec les paramètres définis dans la
                                captureRequest (c'est à dire : le focus en mode continue).
                             */
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, arrierePlanHandler);
                            //on exécute le démarrage de l'enregistrement dans le thread principal
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // on démarre l'enregistrement
                                    mediaRecorder.start();
                                }
                            });

                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                        Toast.makeText(getContext(), "Impossible de configurer la camera", Toast.LENGTH_SHORT).show();
                    }
                }, arrierePlanHandler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Arrête l'enregistrement de la vidéo
     */
    private void stopperEnregistrementVideo() {
        //On réinitialise le remplissage de la barre circulaire du timer (remis à 0)
        circleProgress.setProgress(0);
        //On ré-affiche tous les éléments
        boutonGalerie.setVisibility(View.VISIBLE);
        textChangeCamToVid.setVisibility(View.VISIBLE);
        changementCamToVid.setVisibility(View.VISIBLE);
        //settings.setVisibility(View.VISIBLE);

        try {
            //On arrête la session de visualisation de la caméra
            sessionCapture.stopRepeating();
            sessionCapture.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        // On stoppe l'enregistrement de la vidéo
        mediaRecorder.stop();
        mediaRecorder.reset();

        //on redirige l'utilisateur vers le fragment vidéo pour qu'il choisisse l'image parfaite
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FragmentVideo fragmentVideo = new FragmentVideo();

        Bundle arguments = new Bundle();

        arguments.putString("chemin_video", fichierVideo.getPath());

        fragmentVideo.setArguments(arguments);

        fragmentTransaction.replace(R.id.contenu_fragment, fragmentVideo);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    /**
     * Permet de définir le mediaRecorder et de lui associer toutes les propriétés nécessaires
     * pour l'enregistrement de la vidéo (format, durée max, taille, Fichier destination...).
     */
    private void setMediaRecorder() throws IOException {
        //la source de la vidéo
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //format de la vidéo en mp4
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //durée enregistrement maximale = 10 secondes
        mediaRecorder.setMaxDuration(10000);
        //le fichier où enregistrer la vidéo
        mediaRecorder.setOutputFile(fichierVideo.getPath());
        //le débit de la vidéo
        mediaRecorder.setVideoEncodingBitRate(10000000);
        //frame par secondes
        mediaRecorder.setVideoFrameRate(30);
        //taille de la vidéo
        mediaRecorder.setVideoSize(tailleSurfaceVideo.getWidth(), tailleSurfaceVideo.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        mediaRecorder.setOrientationHint(CameraUtility.ORIENTATIONS_ANGLE_CAMERA.get(rotation));

        mediaRecorder.prepare();

        //Si la durée max (10 secondes) de la vidéo est atteinte, on arrête la vidéo.
        mediaRecorder.setOnInfoListener((MediaRecorder mr, int what, int extra) -> {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopperEnregistrementVideo();
            }
        });
    }









    /* === === En passant par la galerie === === */

    /**
     * Ouvre la galerie du terminal contenant les images et les vidéos.
     */
    private void ouvrirGalerie() {
        Log.e("ouvrirGalerie()", "Appel de ouvrirGalerie()");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.setType("image/*");
        //on lance un intent avec comme numéro de requête REQUETE_GALERY_ACCESS = 79
        Log.e("ouvrirGalerie()", "Lancement de startActivityForResult() ..");
        startActivityForResult(intent, CameraUtility.REQUETE_GALERY_ACCESS);
    }

    /**
     * Récupère le résultat (uri de la photo ou vidéo) selon le numéro de la requête.
     * On est redirigé soit vers le fragment visualisation (image de la galerie), soit le fragment
     * vidéo (vidéo de la galerie <= 10s) ou soit le fragment édition (vidéo > 10s et format mp4)
     *
     * @param requestCode le code de la requête envoyée depuis startActivityForResult
     * @param resultCode  le code retourné par la requête
     * @param data        uri de la photo ou vidéo sélectionnée
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult()", "onActivityResult() est appelé");
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraUtility.REQUETE_GALERY_ACCESS && resultCode == getActivity().RESULT_OK) {
            Log.e("onActivityResult()", "C'est pour un résultat de la galerie");
            Uri uri = null;
            if (data != null) {
                //Retourne l'Uri de la donnée choisie par l'utilisateur
                uri = data.getData();
                //Retourne le type MIME de la donnée choisie de l'intent (image/jpeg ou video/mp4)
                String typeMime = data.resolveType(getContext());

                Log.e("onActivityResult()", "les données ont été extraites");

                if (typeMime != null) {
                    StringTokenizer tokenizer = new StringTokenizer(typeMime, "/");
                    String typeData = tokenizer.nextToken();
                    String format = tokenizer.nextToken();

                    /*
                        L'utilisateur chosit le type de données qu'il veut analyser (image ou video).
                        Il est donc redirigé vers un fragment contenant la visualisation de l'image
                        Il peut soit poursuivre l'analyse avec cette photo soit annuler
                    */

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    FragmentVisualisation fragmentVisualisation;
                    FragmentEdition fragmentEdition;
                    FragmentVideo fragmentVideo;

                    Bundle arguments = new Bundle();

                    //le type de données, on défini une clé spécifique à envoyer avec le Bundle
                    switch (typeData) {
                        case "image":
                            // Annulation de l'action de base qui consistait à prendre la photo prise et rediriger l'user
                            // vers la page de visualisation de l'image pour la valider ou non
                            // ==> On va directement traiter la photo que l'user a prise et afficher le résultat.
                            /*
                            fragmentVisualisation = new FragmentVisualisation();boutonGalerie
                            arguments.putParcelable("uri_image", uri);
                            fragmentVisualisation.setArguments(arguments);

                            fragmentTransaction.replace(R.id.contenu_fragment, fragmentVisualisation);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();

                            break;
                            */
                            Log.e("onActivityResult()", "Appel de onActivityResult() avec typeData pour 'image'");

                            InputStream ims = null;
                            try {
                                ims = getActivity().getContentResolver().openInputStream(uri);
                                bitmap = BitmapFactory.decodeStream(ims, null, bmOptions);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            Log.e("onActivityResult()", "Le bitmap a été créé");

                            bitmap = getResizedBitmap(bitmap, INPUT_SIZE);
                            bitmap = getRotatedBitmap(bitmap, 90);

                            /*

                            // On execute l'algorithme de reconnaissance
                            results = classifieur.classifyFrame(bitmap);

                            resultats_prets = true;
                            Log.e("onActivityResult()", "La variable resultats_prets a été mise à true");

                            mode_resultats();

                            */

                            ThreadResultats tr = new ThreadResultats();
                            tr.setDaemon(true);
                            tr.start();



                            // TODO utiliser l'image de la galerie et lancer la reconnaissance, etc ...


                            break;

                        case "video":

                            //On récupère la durée de la vidéo
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(getContext(), uri);

                            //On extrait des informations de la vidéo : la durée
                            String duree = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            //on calcule le nombre de secondes
                            long dur = Long.parseLong(duree);
                            long seconds = (dur % 60000) / 1000;
                            /*
                                si la vidéo est supérieure à 10 sec, on renvoie le chemin de la vidéo
                                dans le fragment d'édition de vidéo.
                             */
                            if (seconds > 10) {
                                if (format.equals("mp4")) {
                                    fragmentEdition = new FragmentEdition();

                                    String chemin = FileUtils.getPath(getContext(), uri);

                                    arguments.putString("chemin_edit", chemin);
                                    fragmentEdition.setArguments(arguments);

                                    fragmentTransaction.replace(R.id.contenu_fragment, fragmentEdition);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                } else {
                                    Toast.makeText(getContext(), "La vidéo doit être au format MP4", Toast.LENGTH_SHORT).show();
                                }
                            }
                            //si la vidéo est inférieure à 10 sec, on le renvoie dans le fragment normal
                            else {
                                fragmentVideo = new FragmentVideo();

                                arguments.putParcelable("uri_video", uri);
                                fragmentVideo.setArguments(arguments);

                                fragmentTransaction.replace(R.id.contenu_fragment, fragmentVideo);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }

                            break;
                    }
                }
            }
        }
    }



}
