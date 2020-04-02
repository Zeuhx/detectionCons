package descartes.info.l3ag2.eyetrek.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Cette classe permet d'afficher l'apercu de la camera et permet a travers
 * differente option de sauvegarder soit dans le cloud
 * soit dans le telephone
 * Cloud : non dispo pour l'instant
 */
public class FragmentBIS_AffichageCamera extends Fragment {
    private static final String TAG = "CapturePicture";
    static final int REQUEST_PICTURE_CAPTURE = 1;
    private static final int RESULT_OK = 1;
    private ImageView image;
    private String pictureFilePath;
    // private FirebaseStorage firebaseStorage;
    private String deviceIdentifier;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_capture_img, container, false);

        image = view.findViewById(R.id.picture);

        Button captureButton = view.findViewById(R.id.capture);
        captureButton.setOnClickListener(capture);
        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            captureButton.setEnabled(false);
        }

        view.findViewById(R.id.save_local).setOnClickListener(saveGallery);
        // view.findViewById(R.id.save_cloud).setOnClickListener(saveCloud);

       //  firebaseStorage = FirebaseStorage.getInstance();
        getInstallationIdentifier();

        return view ;
    }

    private View.OnClickListener capture = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                sendTakePictureIntent();
            }
        }
    };

    /**
     * À l'aide de FileProvider,
     * On obtien un URI du fichier,
     * Et on ajoute a l'intent en tant qu'extra, puis demarre l'activité.
     */
    private void sendTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra( MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);

            File pictureFile = null;
            try {
                pictureFile = getPictureFile();
            } catch (IOException ex) {
                // getContexte ou getActivity ???
                Toast.makeText(getContext(),"La photo n'est pas pu etre creer, veuillez reessayer", Toast.LENGTH_SHORT).show();
                return;
            }
            /**
             * ATTENTION -> IL FAUT METTRE LE BON CHEMIN
             */
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "descartes.info.l3ag2.eyetrek.fragment",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_PICTURE_CAPTURE);
            }
        }
    }

    /**
     * Créer un nom de fichier unique pour l'image.
     */
    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "SKYWANDER_" + timeStamp;
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile,  ".jpg", storageDir);
        pictureFilePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new  File(pictureFilePath);
            if(imgFile.exists())            {
                image.setImageURI(Uri.fromFile(imgFile));
            }
        }
    }

    /**
     * Sauvegarde dans la galerie
     */
    private View.OnClickListener saveGallery = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            addToGallery();
        }
    };

    /**
     * Methode permettant de mettre dans la galerie
     */
    private void addToGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pictureFilePath);
        Uri picUri = Uri.fromFile(f);
        galleryIntent.setData(picUri);
        // Get Activity ??
        getActivity().sendBroadcast(galleryIntent);
    }


    // Sauvegarder dans le cloud
//    private View.OnClickListener saveCloud = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            addToCloudStorage();
//        }
//    };


//    private void addToCloudStorage() {
//        File f = new File(pictureFilePath);
//        Uri picUri = Uri.fromFile(f);
//        final String cloudFilePath = deviceIdentifier + picUri.getLastPathSegment();
//
//        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//        StorageReference storageRef = firebaseStorage.getReference();
//        StorageReference uploadeRef = storageRef.child(cloudFilePath);
//
//        uploadeRef.putFile(picUri).addOnFailureListener(new OnFailureListener(){
//            public void onFailure(@NonNull Exception exception){
//                Log.e(TAG,"Failed to upload picture to cloud storage");
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){
//                Toast.makeText(CapturePictureActivity.this,
//                        "Image has been uploaded to cloud storage",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    protected synchronized String getInstallationIdentifier() {
        if (deviceIdentifier == null) {
            // Get Activity ??
            SharedPreferences sharedPrefs = getActivity().getSharedPreferences(
                    "DEVICE_ID", Context.MODE_PRIVATE);
            deviceIdentifier = sharedPrefs.getString("DEVICE_ID", null);
            if (deviceIdentifier == null) {
                deviceIdentifier = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("DEVICE_ID", deviceIdentifier);
                editor.commit();
            }
        }
        return deviceIdentifier;
    }
}
