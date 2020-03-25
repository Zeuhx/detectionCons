package descartes.info.l3ag2.eyetrek.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.ScalingTextureView;

public class FragmentMenuAstro extends Fragment {
    public static final String TAG = "FragmentMenuAstro";
    // Constante pour le document Access Storage Framework
    private final static int REQUEST_CAMERA_PERMISSON_RESULT = 0;
    private static final int READ_REQ = 0;
    private static final String TAG2 = "FRAGMENT_MENU";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private int SCREEN_WIDTH = 0;
    private CaptureRequest mPreviewRequest;

    private int SCREEN_HEIGHT = 0;
    private int mState = STATE_PREVIEW;

    /** Camera state: Showing camera preview. */
    private static final int STATE_PREVIEW = 0;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // Image qui sera afficher
    private ImageView image_load_photo;
    private Bitmap bitmap;
    private ScalingTextureView myTextureView; // textview du fragment
    private ImageButton buttonloop;
    private File file;
    private Context ContextCompat;
    private CaptureRequest.Builder myCaptureRequestBuilder;
    private String myCameraId;
    private Size myPreviewSize;
    private Handler myBackgroundHandler;
    private HandlerThread myBackgroundHandlerThread;
    private CameraDevice myCameraDevice;


    private ImageReader imageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

        }
    };
// permet d'obtenir des mises à jour de l'etat de la cameradevice
    // elle doit etre appele pour ouvrir la cam
    private CameraDevice.StateCallback myCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
        @Override
        //appelé quand la cameradevice a fini d'ouvrir
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            myCameraDevice = cameraDevice;
            Toast.makeText(getContext(), "La connection avec la caméra est établie", Toast.LENGTH_SHORT).show();
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            ;
            myCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            myCameraDevice = null;
        }
    };
    //listner qui est notifié quand la surfacetexture associée est disponible
    private TextureView.SurfaceTextureListener mySurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        //est invoquee quand la texturevieux de sufrfacetexture est prete
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            openCamera(width, height);
            changeDisplay(width,height);
            connectCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        changeDisplay(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

        //quand on est en mode portrait on doit swap height et width pour match les resolution dans le preview pour sélectionner un preview dans un textureview
        private static int sensorRotation (CameraCharacteristics cameraCharacteristics ,
        int orientation)
        {
            int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            orientation = ORIENTATIONS.get(orientation);
            return (sensorOrientation + orientation + 360) % 360;
        }

    private void changeDisplay (int width , int height)
    {

        if (myPreviewSize == null || myTextureView == null )
        {
            return ;
        }

        Matrix matrix = new Matrix();
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0,0,width,height);
        RectF previewRectF = new RectF(0,0,myPreviewSize.getHeight(),myPreviewSize.getWidth());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if ( rotation == Surface.ROTATION_90 || rotation== Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / myPreviewSize.getWidth(), (float) height / myPreviewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        float screenAspectRatio = (float)SCREEN_WIDTH / (float)SCREEN_HEIGHT;
        float previewAspectRatio = (float)myPreviewSize.getWidth() / (float)myPreviewSize.getHeight();
        String roundedScreenAspectRatio = String.format("%.2f", screenAspectRatio);
        String roundedPreviewAspectRatio = String.format("%.2f", previewAspectRatio);
        if(!roundedPreviewAspectRatio.equals(roundedScreenAspectRatio) ){

            float scaleFactor = (screenAspectRatio / previewAspectRatio);
            Log.d(TAG, "configureTransform: scale factor: " + scaleFactor);

            float heightCorrection = (((float)SCREEN_HEIGHT * scaleFactor) - (float)SCREEN_HEIGHT) / 2;

            matrix.postScale(scaleFactor, 1);
            matrix.postTranslate(-heightCorrection, 0);
        }

        myTextureView.setTransform(matrix);

    }


    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
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
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }


    }

    /**
     *
     *  Pour le fragement
     *
     **/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_affichage_principale_asto, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolBarAstro);
        DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_astromenu);

        toolbar.setTitle(null);
        toolbar.setNavigationIcon(null);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.navigation_open_drawer, R.string.navigation_close_drawer);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        image_load_photo = (ImageView) view.findViewById(R.id.bouton_import);
        image_load_photo.setOnClickListener(e -> {
            readPhoto();
        });

        //AUGMENTER LA TAILLE DES ICONS DE LA TOOLBAR
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            if(toolbar.getChildAt(i) instanceof ImageButton){
                toolbar.getChildAt(i).setScaleX(1.5f);
                toolbar.getChildAt(i).setScaleY(1.5f);
            }
        }

        myTextureView = (ScalingTextureView) view.findViewById(R.id.textureView);
        buttonloop = (ImageButton) view.findViewById(R.id.bouton_trouver_constellation);

        buttonloop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    takepicture();
                }catch (CameraAccessException e )
                {
                    e.printStackTrace();
                }
                //takepicture(); //Analyser ?*/
            }
        });


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myTextureView = view.findViewById(R.id.textureView);
        setMaxSizes();
    }


    private void setMaxSizes(){
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        SCREEN_HEIGHT = displaySize.x;
        SCREEN_WIDTH = displaySize.y;

        Log.d(TAG, "setMaxSizes: screen width:" + SCREEN_WIDTH);
        Log.d(TAG, "setMaxSizes: screen height: " + SCREEN_HEIGHT);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (resultCode == Activity.RESULT_OK) { // resultCode = -1 requestCode = 0

            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Rappelle le fragment : affiche le contenu du fragment
                replaceFragment(Fragment_AstroAffichagePhoto.newInstance(uri));
            }

            if (requestCode == READ_REQ) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Reprend le contexte du parent du fragment en question
                Cursor cursor = getContext().getContentResolver().query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
            } else {
                Log.e(TAG, "Erreur lors de la lecture importée");
            }
        }
    }

    // onResume permet de voir quand la textureview est dispo en lançant l'app li
    @Override
    public void onResume() {
        super.onResume();

        startBackgroundThread();

        if (myTextureView.isAvailable()) {
            openCamera(myTextureView.getWidth(), myTextureView.getHeight());
            changeDisplay(myTextureView.getWidth(),myTextureView.getHeight());
            connectCamera();

        } else {
            myTextureView.setSurfaceTextureListener(mySurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();

        stopBackgroundThread();

        super.onPause();
    }

    /**
     *
     * Pour la camera, l'importation et pour l'affichage
     *
     */


    /**
     * Importation et affichage
     */
    public void readPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQ);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSON_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "L'appli ne se lance pas sans la cam", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*@Override
    public void onRequestPermissionResult ( int requestCode , String[] permissions , int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode==REQUEST_CAMERA_PERMISSON_RESULT){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(),"L'appli ne se lance pas sans la cam",Toast.LENGTH_SHORT).show();
    }
    }
    }*/


    /**
     * ------- Methodes pour la camera
     */

    // methode permet d'ouvrir la caméra au dos du smarthpobe
    private void openCamera(int width, int height) {

        changeDisplay(width, height);
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {

            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                //Pemer en bas que la caméra frontale ne s'utilise pas

                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int orientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                int totalRotation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                //sensorRotation(cameraCharacteristics, orientation);

                boolean swapRotation = false;
                switch (orientation){
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (totalRotation== 90 || totalRotation == 270) {
                            swapRotation = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (totalRotation == 0 || totalRotation == 180) {
                            swapRotation= true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " +orientation);


                }

                int rotatedWidth = width;
                int rotatedHeight = height;
                Point displaySize = new Point();
                //activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swapRotation) {
                    rotatedHeight = height;
                    rotatedWidth = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }
                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // myVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                //myPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotatedWidth,rotatedHeight, myVideoSize);
                try {
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        myTextureView.setAspectRatio(myPreviewSize.getWidth(), myPreviewSize.getHeight(),SCREEN_WIDTH,SCREEN_HEIGHT);
                    } else {
                        myTextureView.setAspectRatio(myPreviewSize.getHeight(), myPreviewSize.getWidth(),SCREEN_HEIGHT,SCREEN_WIDTH);
                    }

                }catch (Exception e )
                {
                    e.printStackTrace();
                }


                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);
                imageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, myBackgroundHandler);

                myPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedWidth, rotatedHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                //int orientation = getResources().getConfiguration().orientation;
                /*if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    myTextureView.setAspectRatio(
                            myPreviewSize.getWidth(), myPreviewSize.getHeight());
                } else {
                    myTextureView.setAspectRatio(
                            myPreviewSize.getHeight(), myPreviewSize.getWidth());
                }*/
                myCameraId = cameraId;
                //changeDisplay(width,height);




                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (android.support.v4.content.ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(myCameraId, myCameraDeviceStateCallBack, myBackgroundHandler);

                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(getContext(), "L'appliation a besoin de la caméra", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSON_RESULT);
                }
            } else {
                cameraManager.openCamera(myCameraId, myCameraDeviceStateCallBack, myBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = myTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(myPreviewSize.getWidth(), myPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            myCaptureRequestBuilder = myCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            myCaptureRequestBuilder.addTarget(previewSurface);
            myCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    try {
                        myCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        mPreviewRequest = myCaptureRequestBuilder.build();
                        cameraCaptureSession.setRepeatingRequest(myCaptureRequestBuilder.build(), null, myBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getContext(), "Pas possible d'afficher", Toast.LENGTH_SHORT).show();
                }
            }, null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void takepicture() throws  CameraAccessException {

        if (myCameraDevice == null) {
            return;
        }
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);


            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(myCameraDevice.getId());


        Size[] jpegSizes = null;
        jpegSizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

        int width = 640 ;
        int height = 480;
        if (jpegSizes!=null && jpegSizes.length>0)
        {
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();

        }
        ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
        List<Surface> outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(reader.getSurface());
        outputSurfaces.add(new Surface(myTextureView.getSurfaceTexture()));
        final CaptureRequest.Builder captureBuilder = myCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        int rotation = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation(); //pas sur*
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

        Long timelong = System.currentTimeMillis()/1000;
        String time = timelong.toString();
         file = new File(Environment.getExternalStorageDirectory()+"/"+time+".jpg");
       /* File = new File(Environment.getExternalStorageDirectory() + "/"+time+".jpg");*/
        ImageReader.OnImageAvailableListener readerListner = new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader imageReader)
            {
                Image image = null;
                image = reader.acquireLatestImage();

                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes= new byte[buffer.capacity()];
                buffer.get(bytes);
                try {
                    save(bytes);
                }catch (IOException e )
                {
                    e.printStackTrace();
                }
                finally {
                    if(image!=null)
                    {
                        image.close();
                    }
                }
            }
        };

        reader.setOnImageAvailableListener(readerListner,myBackgroundHandler);

        final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Toast.makeText(getContext().getApplicationContext(),"Saved", Toast.LENGTH_LONG).show();

                startPreview();

            }
        };

        myCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,myBackgroundHandler);
                }catch(CameraAccessException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            }
        },myBackgroundHandler);

    }

        private void save (byte[] bytes )throws IOException
        {
            OutputStream outputStream = null ;
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.close();
        }

        private void closeCamera ()
        {
            if (myCameraDevice != null) {
                myCameraDevice.close();
                myCameraDevice = null;
            }

        }

        // permet de d'enlever les taches de longue durée de l'interface utilisatzeur donc ca ne va pas affecter le comportement de l'interface utilisateur
        private void startBackgroundThread ()
        {
            myBackgroundHandlerThread = new HandlerThread("Camera Background");
            myBackgroundHandlerThread.start();
            myBackgroundHandler = new Handler(myBackgroundHandlerThread.getLooper());


        }

        private void stopBackgroundThread ()
        {
            myBackgroundHandlerThread.quitSafely();
            try {
                myBackgroundHandlerThread.join();
                myBackgroundHandlerThread = null;
                myBackgroundHandler = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Soumet le fragment a l'activite
    private void replaceFragment(Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getActivity().getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_contenairAstro, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size size, Size t1) {
            return Long.signum((long) size.getHeight() * size.getHeight() / (long) t1.getWidth() * t1.getHeight());

        }
    }

}

