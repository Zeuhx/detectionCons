package descartes.info.l3ag2.eyetrek.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;


import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import descartes.info.l3ag2.eyetrek.classes.AlgoKNN;
import descartes.info.l3ag2.eyetrek.classes.DatabaseHandler;
import descartes.info.l3ag2.eyetrek.classes.FeatureExtraction;
import descartes.info.l3ag2.eyetrek.classes.FeaturesVect;
import descartes.info.l3ag2.eyetrek.classes.FileUtils;
import descartes.info.l3ag2.eyetrek.classes.Result;
import descartes.info.l3ag2.eyetrek.classes.Voisins;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.pojo.Leaf;


/**
 * Created by Dorian Quaboul
 * Updated by Ayaz Abdul Cader
 * <p>
 * fragment correspondant à la visualisation de la d'une photo de la galerie ou qui vient d'être
 * capturée avec l'application.
 */
public class FragmentVisualisation extends Fragment {

    private ImageButton bouton_annuler;
    private Button bouton_next;

    private ImageView image_visualisation;

    private BottomNavigationView barreMenu;

    private File fichierImage;
    private Uri uri;
    private String type_data;

    public FragmentVisualisation() {
    }

    /**
     * On instancie les objets non graphiques
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
            On récupère les arguments passés lors de la création du fragment, c'est à dire dans le
            fragment analyse lors de la capture ou du choix de la photo située dans la galerie.
         */
        //la clé "chemin_photo" est associée à la valeur contenant le chemin de la photo capturée.
        if (getArguments().containsKey("chemin_photo")) {
            type_data = "camera_photo";
            String chemin = getArguments().get("chemin_photo").toString();
            fichierImage = new File(chemin);
        }

        /*
            La clé "uri_image" est associée à la valeur contenant le chemin de la photo stockée
            dans la galerie.
        */
        else if (getArguments().containsKey("uri_image")) {
            type_data = "galerie_photo";
            uri = (Uri) getArguments().get("uri_image");
        }
    }

    /**
     * On instancie les composants graphiques
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visualisation, container, false);
        bouton_annuler = view.findViewById(R.id.bouton_annuler);
        bouton_next = view.findViewById(R.id.bouton_next);
        image_visualisation = view.findViewById(R.id.image_visualisation);
        barreMenu = getActivity().findViewById(R.id.barre_navigation);
        barreMenu.setVisibility(View.INVISIBLE);
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
        //if (fichierImage.exists()) {
        //on cache la barre de menu appartenant la MainActivity
        //barreMenu.setVisibility(View.INVISIBLE);
        //on affiche la photo dans l'imageView à partir du chemin de la photo
            /*Bitmap imageBitmap = BitmapFactory.decodeFile(fichierImage.getPath());

            /*
                Source:https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
                On rotate la Bitmap pour affichage, car certains constructeur sauvegarde par défault leurs photos en landscape mode
             */
            /*
            try {
                ExifInterface ei = new ExifInterface(fichierImage.getPath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap rotatedBitmap;
                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(imageBitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(imageBitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(imageBitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = imageBitmap;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }*/
        //}
        //Selon le type de données, on affiche différement l'image.
        switch (type_data) {
            case "galerie_photo":
                //On utilise la libraire Glide pour pouvoir charger l'image dans l'imageView
                Glide.with(this).load(uri).into(image_visualisation);
                break;
            case "camera_photo":
                if (fichierImage.exists()) {
                    Glide.with(this).load(fichierImage).into(image_visualisation);
                }
                break;
        }
        //Gestion du bouton "Annuler"
        bouton_annuler.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            //Selon le type de données, on gère différement l'action du bouton "annuler"
            switch (type_data) {
                //Le bouton "Annuler" d'annuler l'analyse
                case "galerie_photo":
                    Toast.makeText(getContext(), "Vous avez annulé l'analyse", Toast.LENGTH_SHORT).show();
                    //On retire ce fragment de la pile et on revient au fragment analyse
                    //fragmentManager.popBackStack();
                    fragmentManager.beginTransaction().replace(R.id.contenu_fragment, new FragmentScanFeuille()).commit();
                    break;
                //le bouton "Annuler" permet de supprimer la photo prise et d'annuler l'analyse
                case "camera_photo":
                    if (fichierImage.delete()) {
                        Toast.makeText(getContext(), "L'image a bien été supprimée", Toast.LENGTH_SHORT).show();
                        //On retire ce fragment de la pile et on revient au fragment analyse
                        //fragmentManager.popBackStack();
                        fragmentManager.beginTransaction().replace(R.id.contenu_fragment, new FragmentScanFeuille()).commit();
                    } else {
                        Toast.makeText(getContext(), "Une erreur est survenue lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    Toast.makeText(getContext(), "Rien ne s'est passé", Toast.LENGTH_SHORT).show();
                    //On retire ce fragment de la pile et on revient au fragment analyse
                    //fragmentManager.popBackStack();
                    fragmentManager.beginTransaction().replace(R.id.contenu_fragment, new FragmentScanFeuille()).commit();
            }

        });
        //Gestion du bouton "Continuer"
        bouton_next.setOnClickListener((v) -> {
            Bundle bundle = new Bundle();
            String imagePath = "";
            switch (type_data) {
                case "galerie_photo":
                    imagePath = FileUtils.getPath(getContext(), uri);
                    /*
                    //On vérifie si la couleurs dominante de la photo sélectionner depuis la galerie est présente dans la base de données
                    List<float[]> galerieHsl = getDominantColor(file);
                    if (!(galerieHsl.isEmpty())) {
                        new AnalyseTask().execute(FeatureExtraction.getFeaturesVect(FileUtils.getPath(getContext(), uri)));
                    } else {
                        createAlertBox("Erreur !", "Vous essayez d'analyser autre chose qu'une feuille !");
                    }
                    */
                    break;
                case "camera_photo":
                    imagePath = fichierImage.getPath();
                    /*
                    //On vérifie si la couleur dominante de la photo prise est présente dans la base de données
                    List<float[]> cameraHsl = getDominantColor(fichierImage);
                    if (!(cameraHsl.isEmpty())) {
                        new AnalyseTask().execute(FeatureExtraction.getFeaturesVect(fichierImage.getPath()));
                    } else {
                        createAlertBox("Erreur !", "Vous essayez d'analyser autre chose qu'une feuille !");
                    }
                    */
                    break;
            }
            bundle.putString("image_path", imagePath); // set your parameteres

            FragmentScanFeuille fragmentScanFeuille = new FragmentScanFeuille();
            fragmentScanFeuille.setArguments(bundle);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contenu_fragment, fragmentScanFeuille).commit();
        });
        return view;
    }



 /*   /**
     * Fonction permettant d'exercer une rotation sur une Bitmap
     * Source: https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
     *
     * @param source
     * @param angle
     * @return
     */
    /*public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
    matrix, true);
    }*/

    public class AnalyseTask extends AsyncTask<FeaturesVect, String, List<Voisins>> {
        private ProgressDialog boiteChargement;

        protected void onPreExecute() {
            boiteChargement = new ProgressDialog(getActivity());
            boiteChargement.setMessage("Initialisation de l'analyse ...");
            boiteChargement.setTitle("Analyse de feuille");
            boiteChargement.setCancelable(false);
            boiteChargement.show();
        }

        @Override
        protected List<Voisins> doInBackground(FeaturesVect... featuresVects) {
            List<FeaturesVect> allDataFeatures;
            List<Voisins> resultats = null;
            try {
                AlgoKNN.setContext(getContext());
                publishProgress("Récupération des vecteurs de caractéristiques du fichier csv ...");
                allDataFeatures = AlgoKNN.getListFeatures();
                FeatureExtraction.setFeaturesVect(featuresVects[0]);
                publishProgress("Calcul des plus proches voisins ...");
                List<Result> plusProchesVoisins = AlgoKNN.getPlusProchesVoisins(allDataFeatures, featuresVects[0]);
                publishProgress("Détermination des classes dominantes ...");
                resultats = AlgoKNN.getPrediction(plusProchesVoisins);
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
            Leaf feuille = databaseHandler.getLeaf(result.get(0).getLabel());
            String strFeuille1 = feuille.getName();
            int pourcent1 = result.get(0).getFrequency();

            resultPos1.setText(strFeuille1);
            putColorPourcentage(pourcentagePos1,pourcent1);
            pourcentagePos1.setText(pourcent1+"%");
            resultImage.setImageResource(getContext().getResources().getIdentifier(feuille.getPicture(), "drawable", getContext().getPackageName()));

            if(result.size() > 1){
                Leaf feuille2 = databaseHandler.getLeaf(result.get(1).getLabel());
                String strFeuille2 = feuille2.getName();
                int pourcent2 = result.get(1).getFrequency();

                resultPos2.setText(strFeuille2);
                putColorPourcentage(pourcentagePos2,pourcent2);
                pourcentagePos2.setText(pourcent2+"%");
                resultImage2.setImageResource(getContext().getResources().getIdentifier(feuille2.getPicture(), "drawable", getContext().getPackageName()));

                if (result.size() > 3){
                    Leaf feuille3 = databaseHandler.getLeaf(result.get(2).getLabel());
                    String strFeuille3 = feuille3.getName();
                    int pourcent3 = result.get(2).getFrequency();

                    resultPos3.setText(strFeuille3);
                    putColorPourcentage(pourcentagePos3,pourcent3);
                    pourcentagePos3.setText(pourcent3+"%");
                    resultImage3.setImageResource(getContext().getResources().getIdentifier(feuille3.getPicture(), "drawable", getContext().getPackageName()));
                }
            }

            //On place l'image dans la boite de dialog
            builder.setView(layout)
                    .setCancelable(false)
                    .setPositiveButton("Merci ! ", new DialogInterface.OnClickListener() {
                        //Quand l'utilisateur cliquera sur le bouton "Merci !", on le redirige vers le fragment analyse

                        public void onClick(DialogInterface dialog, int which) {
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnalyse()).commit();
                            fragmentTransaction.addToBackStack(null);
                        }
                    })
                    .create().show();
        }
    }


    /**
     * Fonction retournant tous les hsv d'une image
     *
     * @param file
     * @return
     */
    //TODO: Optimisation
    public static List<float[]> getDominantColor(File file) {

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        Bitmap resizeBM = resize(bitmap);

        Bitmap cropCenterBM = cropCenter(resizeBM);

        int SENSITIVITY = 15;

        float[] LOWER_GREEN = new float[]{80-SENSITIVITY, 0.17f, 0.24f};
        float[] UPPER_GREEN = new float[]{138+SENSITIVITY, 0.97f, 0.38f};

        //ROJ = Red Orange Yellow
        float[] LOWER_ROY = new float[]{36-SENSITIVITY,0.76f,0.34f};
        float[] UPPER_ROY = new float[]{35+SENSITIVITY,0.1f,0.64f};

        List<float[]> allHsv = new ArrayList<>();

        //Parcours de la bitmap
        for (int i = 0; i < cropCenterBM.getWidth(); i++) {
            for (int j = 0; j < cropCenterBM.getHeight(); j++) {

                //Instanciation des booléens
                boolean higher = true;
                boolean lower = true;

                //Extraction de chaque pixel
                int color = cropCenterBM.getPixel(i, j);

                //Extraction de chaque couleur
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);

                float[] hsv = new float[3];

                //Récupération du HSV
                Color.RGBToHSV(r, g, b, hsv);
                int couleurTest = Color.rgb(r, g, b);

                //On compare avec le plancher de HSV
                if((hsv[0] >= LOWER_GREEN[0] && hsv[1] >= LOWER_GREEN[1] && hsv[2] >= LOWER_GREEN[2])||(hsv[0] >= LOWER_ROY[0] && hsv[1] >= LOWER_ROY[1] && hsv[2] >= LOWER_ROY[2])) {
                    lower = true;
                }
                else {
                    lower = false;
                }

                //On compare avec le plafond de HSV
                if((hsv[0] <=  UPPER_GREEN[0] && hsv[1] <=  UPPER_GREEN[1] && hsv[2] <=  UPPER_GREEN[2])||(hsv[0] <=  UPPER_ROY[0] && hsv[1] <=  UPPER_ROY[1] && hsv[2] <=  UPPER_ROY[2])) {
                    higher = true;
                }
                else {
                    higher = false;
                }


                //Si tout est bon on ajoute les hsv dans la List des hsv
                if (higher && lower) {
                    allHsv.add(hsv);
                }
            }
        }
        return allHsv;

    }

    /**
     * Fonction permettant de redimnensionner une Bitmap
     * @param bitmap
     * @return
     */
    private static Bitmap resize(Bitmap bitmap) {
        Bitmap resize;

        double height = bitmap.getHeight();
        double width = bitmap.getWidth();

        double aspectRatio = width / height;


        if(width > height) {

            int newHeight = (int) (256 / aspectRatio);
            resize = Bitmap.createScaledBitmap(bitmap,256,newHeight,false);

        }
        else if(width == height) {

            resize = Bitmap.createScaledBitmap(bitmap,256,256,false);
        }

        else {
            int newWidth = (int) (256 * aspectRatio);

            resize = Bitmap.createScaledBitmap(bitmap,newWidth,256,false);
        }

        return resize;
    }

    /**
     * Fonction permettant de découper le centre d'une Bitmap
     * @param bitmap
     * @return
     */
    private static Bitmap cropCenter(Bitmap bitmap) {
        Bitmap crop;

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        int coordX = (width / 2) - 8;
        int coordY = (height / 2) - 8;

        crop = Bitmap.createBitmap(bitmap,coordX,coordY,16,16);

        return crop;
    }
    /**
     * Fonction permettant d'indiquer si la couleur dominante d'une image est déjà présente dans la base de donéne =
     *
     * @param colorsDb
     * @param colorPicture
     * @return boolean
     */
    public boolean checkColor(List<Integer> colorsDb, List<Palette.Swatch> colorPicture) {
        //Pour chaque couleur dans la photo
        for (Palette.Swatch uneColorPicture : colorPicture) {
            //Pour chaque HslColor de feuille dans la BDD
            for (Integer ColorsDb : colorsDb) {
                if (uneColorPicture.getRgb() == ColorsDb) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Méthode permettant de créer une AlertBox
     *
     * @param title
     * @param msg
     */

    private void createAlertBox(String title, String msg) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Oups !", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.contenu_fragment, new FragmentAnalyse()).commit();
                fragmentTransaction.addToBackStack(null);
            }
        }).show();
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

