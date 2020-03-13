package descartes.info.l3ag2.eyetrek.fragment;


import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
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
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import descartes.info.l3ag2.eyetrek.classes.CameraUtility;
import descartes.info.l3ag2.eyetrek.classes.CompareAire;
import descartes.info.l3ag2.eyetrek.classes.FileUtils;
import descartes.info.l3ag2.eyetrek.classes.ImageSauvegarde;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.activity.MainActivity;


import descartes.info.l3ag2.eyetrek.tensorflow.Classifier;
//contains logic for reading labels, creating classifier, and classifying
import descartes.info.l3ag2.eyetrek.tensorflow.TensorFlowImageClassifier;

import static android.content.Context.MODE_PRIVATE;

/**
 * C'est le fragment pour analyser des feuilles
 *
 * Created by Dorian QUABOUL
 * Updated by Ayaz ABDUL CADER
 * <p>
 * Sources :
 * <p>
 * Android Camera2 API Video App - https://youtu.be/CuvVpsFc77w
 * Chaîne : Mobile Application Tutorials
 * Vidéos PART 1 à PART10
 * <p>
 * Google Sample Camera2
 * https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java
 * <p>
 * Google Sample Camera2 Video
 * https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/Camera2VideoFragment.java
 * ---------------------------------------------
 */
public class FragmentAnalyse extends Fragment {
    //Composants graphiques :

    //La surface dans laquelle on peut voir ce qu'on va prendre en photo.
    private TextureView surfaceCamera;
    private Classifier classifieur = null;
    private static final int INPUT_SIZE = 150;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input_1";
    private static final String OUTPUT_NAME = "dense_2/Softmax";

    //private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String MODEL_FILE = "file:///android_asset/model.tflite";
    //private static final String LABEL_FILE =
    //      "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final String LABEL_FILE = "file:///assets/labelstest.txt";
    private Bitmap croppedBitmap = null;

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
        }

        /**
         * Aucun rendu n'est présent à l'intérieur de la surface
         * après que cette méthode ait été invoquée.
         * @param surfaceTexture
         * @return false
         */
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };
    private Switch changementCamToVid;
    private TextView textChangeCamToVid;
    private TextView textDureeMaxRecord;
    private ImageButton boutonCapture;

    private ImageButton boutonVideo;
    private MediaRecorder mediaRecorder;
    //Renseigne si l'enregistrement de la vidéo est en cours ou pas
    private boolean isRecord;
    private ProgressBar circleProgress;

    private ImageButton boutonGalerie;
    private ImageButton settings;
    private BottomNavigationView barreMenu;

    //Représente la caméra active.
    private CameraDevice camera;
    //Identifiant de la caméra active.
    private String idCamera;
    //Renseigne si le flash est supporté ou non par l'appareil.
    private boolean isFlashSupported;
    //Taille de la surface de la retransmission caméra.
    private Size tailleSurfaceCamera;
    //Taille de la surface de la vidéo.
    private Size tailleSurfaceVideo;

    /*
        Ce listener est appelé à chaque changement d'état (ouverture, déconnexion ou cas d'erreur)
        de la caméra active.
     */
    private CameraDevice.StateCallback cameraEtatListener = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camera = cameraDevice;
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
        }
    };
    /**
     * Etat courant pour la procédure de capture d'image
     */
    private int etat = CameraUtility.ETAT_APERCU_CAM;

    //Une session pour la retransmission de la camera et pour envoyer des CaptureRequest.
    private CameraCaptureSession sessionCapture;

    /*
        Gestion des differents états (étapes) de la capture d'une image JPEG :

        Ce listener est appelé quand une requête déclenche une capture de commencer et quand
        la capture est terminée.

        Il permet de suivre la progression d'une CaptureRequest soumise à la caméra active.
    */
    private CameraCaptureSession.CaptureCallback captureCallback
            = new CameraCaptureSession.CaptureCallback() {

        /**
         * Permet de contrôler les résultats des différentes CaptureRequest émises dans la session.
         *
         * @param result résultat
         */
        private void checkEtat(CaptureResult result) {
            switch (etat) {
                case CameraUtility.ETAT_APERCU_CAM:
                    // Il s'agit de l'affichage de la camera
                    break;

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
                        }
                        //on détermine la séquence de mesure de précapture de l'auto-exposition (AE).
                        else {
                            runPrecaptureSequence();
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
            checkEtat(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            checkEtat(result);
        }

    };

    //"Constructeur" de CaptureRequest pour l'aperçu de la caméra.
    private CaptureRequest.Builder captureRequestBuilder;

    //Un thread qui permet d'executer des taches sans bloquer l'interface utilisateur.
    private HandlerThread arrierePlanThread;
    //Pour exécuter des taches en arriere plan
    private Handler arrierePlanHandler;

    //Le fichier correspondant à l'image capturée
    private File fichierImage;
    //Le dossier où seront stockées les image
    private File dossierImage;
    //Le fichier correspondant à la vidéo enregistrée
    private File fichierVideo;
    //Le dossier où seront stockées les vidéos
    private File dossierVideo;

    private static final String DIDACTICIEL = "DIDACTICIEL";


    //ImageReader qui contient l'image capturée et gère la capture d'image fixe.
    private ImageReader containerImage;
    //Ce listener est appelée quand une image est prête à être sauvegardée.
    private ImageReader.OnImageAvailableListener imageDisponibleListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            createImageFile();
            //On execute la sauvegarde de l'image en arriere-plan.
            arrierePlanHandler.post(new ImageSauvegarde(reader.acquireNextImage(), fichierImage));
        }
    };

    //Le constructeur doit être vide
    public FragmentAnalyse() {
    }

    /**
     * On instancie les objets non graphiques
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createImageFolder();

        createVideoFolder();

        // tensorflow
        //load up our saved model to perform inference from local storage
        loadModel();
    }

    /**
     * On instancie la vue et les composants graphiques
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Chargement du noeud parent du fichier layout fragment_analyse.xml
        View view = inflater.inflate(R.layout.fragment_analyse, container, false);

        //On instancie les composants graphiques avec findViewById
        surfaceCamera = view.findViewById(R.id.surface_camera);
        boutonCapture = view.findViewById(R.id.bouton_capture);
        boutonGalerie = view.findViewById(R.id.bouton_galerie);
        boutonVideo = view.findViewById(R.id.bouton_video);
        circleProgress = view.findViewById(R.id.barre_recordVideo);
        changementCamToVid = view.findViewById(R.id.changement_vid_cam);
        textChangeCamToVid = view.findViewById(R.id.text_switch);
        textDureeMaxRecord = view.findViewById(R.id.text_duree);
        barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.VISIBLE);
        settings = view.findViewById(R.id.settings);

        //Source : https://stackoverflow.com/questions/30766755/smooth-progress-bar-animation
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(circleProgress, "progress", 0, 100);
        //On configure la durée de l'animation à 10 secondes pour le remplissage du timer (barre circulaire)
        progressAnimator.setDuration(10000);
        //Le remplissage s'effectue de façon linéaire (c'est à dire en continue sans accélération)
        progressAnimator.setInterpolator(new LinearInterpolator());

        boutonVideo.setOnClickListener((v) -> {
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
                settings.setVisibility(View.INVISIBLE);
                //on démarre le remplissage de la jauge du timer
                progressAnimator.start();
                commencerEnregistrementVideo();
            }
        });


        //Clique sur le bouton de la Galerie
        boutonGalerie.setOnClickListener((v) -> {
            ouvrirGalerie();
        });

        //Click sur le bouton de capture
        boutonCapture.setOnClickListener((v) -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                prendrePhoto();
            } else {
                Log.e("ERREUR", "Permission de stockage refusé !");
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
                //rotation du bouton video
                //CameraUtility.flipIt(boutonVideo);
            } else {
                //Mode photo
                boutonVideo.setVisibility(View.GONE);
                circleProgress.setVisibility(View.GONE);
                boutonCapture.setVisibility(View.VISIBLE);
                textChangeCamToVid.setText(R.string.switch_text_photo);
                textDureeMaxRecord.setVisibility(View.GONE);
                //rotation du bouton de photo
                //CameraUtility.flipIt(boutonCapture);
            }
        });

        //Clique sur le bouton paramètre permettant d'accéder au choix d'analyse
        settings.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentMenu()).commit();
        });

        //On récupère le booléen permettant de savoir si l'utilisateur à déjà vu le didacticiel
        Boolean show = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).getBoolean("analyse", false);
        if (!show) {
            new TapTargetSequence(getActivity())
                    .targets(
                            TapTarget.forView(view.findViewById(R.id.settings), "Paramètres", "Ce bouton permet d'accéder au choix du mode d'analyse !").dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .transparentTarget(true)
                                    .textColor(android.R.color.black),
                            TapTarget.forView(view.findViewById(R.id.bouton_galerie), "Galerie ", "Ce bouton permet d'analyser une image provenant de votre galerie !")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .transparentTarget(true)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .textColor(android.R.color.black),
                            TapTarget.forView(view.findViewById(R.id.bouton_video), "Analyse", "Ce bouton permet de lancer l'analyse !")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .transparentTarget(true)
                                    .textColor(android.R.color.black),
                            TapTarget.forView(view.findViewById(R.id.changement_vid_cam), "Switch", "Ce bouton permet de switcher entre photo et vidéo !")
                                    .dimColor(android.R.color.black)
                                    .outerCircleColor(R.color.colorTheme)
                                    .targetCircleColor(R.color.transparent_gray)
                                    .transparentTarget(true)
                                    .textColor(android.R.color.black))
                    .listener(new TapTargetSequence.Listener() {
                        //Si l'utilisateur parcours tout le didacticiel ou l'annule au milieu on ajoute un booléen
                        @Override
                        public void onSequenceFinish() {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                            editor.putBoolean("analyse", true).commit();
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                            SharedPreferences.Editor editor = getActivity().getSharedPreferences(DIDACTICIEL, MODE_PRIVATE).edit();
                            editor.putBoolean("analyse", true).commit();
                        }
                    }).start();
        }

        return view;
    }

    /**
     * Ouvre la galerie du terminal contenant les images et les vidéos.
     */
    private void ouvrirGalerie() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.setType("image/*");
        //on lance un intent avec comme numéro de requête REQUETE_GALERY_ACCESS = 79
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraUtility.REQUETE_GALERY_ACCESS && resultCode == getActivity().RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                //Retourne l'Uri de la donnée choisie par l'utilisateur
                uri = data.getData();
                //Retourne le type MIME de la donnée choisie de l'intent (image/jpeg ou video/mp4)
                String typeMime = data.resolveType(getContext());

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

    //creates a model object in memory using the saved tensorflow protobuf model file
    //which contains all the learned weights
    private void loadModel() {
        //The Runnable interface is another way in which you can implement multi-threading other than extending the
        // //Thread class due to the fact that Java allows you to extend only one class. Runnable is just an interface,
        // //which provides the method run.
        // //Threads are implementations and use Runnable to call the method run().
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
                } catch (final Exception e) {
                    //if they aren't found, throw an error!
                    throw new RuntimeException("Erreur pendant l'initialisation des classifieurs !", e);
                }
            }
        }).start();
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

                //On instancie la dimension de surface de retransmission de la caméra
                tailleSurfaceCamera = new Size(largeur, hauteur);
                //On instancie la dimension de surface de la video (largeur <=1080 & format 4:3)
                tailleSurfaceVideo = CameraUtility.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

                //On choisit la taille maximale pour l'image
                Size tailleImage = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareAire());

                containerImage = ImageReader.newInstance(tailleImage.getWidth(), tailleImage.getHeight(), ImageFormat.JPEG, /*maxImages*/1);
                containerImage.setOnImageAvailableListener(imageDisponibleListener, arrierePlanHandler);

                // On regarde si le flash est disponible sur le terminal
                Boolean flashDispo = caracteristiqueCamera.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flashDispo) {
                    isFlashSupported = flashDispo;
                } else {
                    isFlashSupported = false;
                }

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

    /**
     * Avant de prendre en photo, il faut vérouiller le focus.
     */
    private void prendrePhoto() {
        lockFocus();
    }

    /**
     * Permet de vérouiller le focus en lançant une requête dans la session active.
     */
    private void lockFocus() {
        try {
            /*
                Déclenchement de  vérouillage de l'autofocus
                On définit une CaptureRequest pour le vérouillage de l'auto-focus

                Permet d'affecter à la clé "CONTROL_AF_TRIGGER" de la captureRequest
                la  valeur "CONTROL_AF_TRIGGER_START"
             */
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);

            //on change l'état de la capture.
            etat = CameraUtility.ETAT_ATTENTE_LOCK_FOCUS;
            /*
                Dans la session active, on soumet une requête à la caméra pour capturer une image
                avec les paramètres définis dans la captureRequest (c'est à dire : déclenchement du
                verrouillage du focus).
                Chaque requête (CaptureRequest) produit un CaptureResult qui va être traité dans le captureCallBack.
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
     * Capture une image. Cette méthode doit etre appelée après avoir vérouillé le focus.
     */
    private void captureImage() {
        try {
            Activity activity = getActivity();
            if (activity == null || camera == null) {
                return;
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

            CameraCaptureSession.CaptureCallback captureGestion = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    createImageFile();
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request,
                                               TotalCaptureResult result) {
                    unlockFocus();
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

                    File sd = Environment.getExternalStorageDirectory();
                    File image = new File(fichierImage.getPath());

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.outWidth = INPUT_SIZE;
                    bmOptions.outHeight = INPUT_SIZE;
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

                    //init an empty string to fill with the classification output
                    String text = "";
                    croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888);

                    List<Classifier.Recognition> results;
                    try {
                        //perform classification on the image
                        results = classifieur.recognizeImage(bitmap);


                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        //Set title for AlertDialog
                        builder.setTitle("Résultat");

                        //Set body message of Dialog
                        builder.setMessage(results.toString());

                        //// Is dismiss when touching outside?
                        builder.setCancelable(true);

                        //Positive Button and it onClicked event listener
                        builder.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });



                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch(Exception e){
                        e.printStackTrace();
                    }


                }
            };
            //on arrête la retransmission de la caméra
            sessionCapture.stopRepeating();
            //on interrompt toutes les capturesRequest en cours
            sessionCapture.abortCaptures();
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
     * Permet de déclencher la séquence de mesure de précapture de l'auto-exposition (AE).
     */
    private void runPrecaptureSequence() {
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

    /**
     * Permet de créer le dossier Pictures, s'il n'est pas déjà créé.
     */
    private void createImageFolder() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";

        dossierImage = new File(cheminDossier);

        if (!dossierImage.exists()) {
            dossierImage.mkdirs();
        }
    }

    /**
     * Permet de définir le nom de chaque capture d'image qui doit être sauvegardée dans le dossier
     * Pictures.
     */
    private void createImageFile() {
        String cheminDossier = Environment.getExternalStorageDirectory() + "/EyeTrek/Pictures/";

        StringBuffer nomFichier = new StringBuffer("Capture_");
        nomFichier.append(new Date().getTime());
        nomFichier.append(".jpg");

        fichierImage = new File(cheminDossier, nomFichier.toString());
    }

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
        settings.setVisibility(View.VISIBLE);

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

    /**
     * Démarre un nouveau thread en arriere plan pour optimiser les opérations et éviter des
     * chargements supplémentaires.
     */
    private void demarreThreadArrierePlan() {
        arrierePlanThread = new HandlerThread("CameraThread");
        arrierePlanThread.start();
        arrierePlanHandler = new Handler(arrierePlanThread.getLooper());
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
