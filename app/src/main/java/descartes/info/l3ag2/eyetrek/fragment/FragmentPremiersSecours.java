package descartes.info.l3ag2.eyetrek.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import descartes.info.l3ag2.eyetrek.R;

import static android.content.Context.VIBRATOR_SERVICE;

public class FragmentPremiersSecours extends Fragment {

    // Bouton pour les gestes à faire en cas d'arrets cardiaques et respiratoires
    Button bouton_geste_arret_cardiaque;

    // Bouton pour les gestes à faire en cas de saignements
    Button bouton_geste_saignement;

    // Bouton pour les gestes à faire en cas de fracture, entorse ou luxation
    Button bouton_geste_fracture;

    // Bouton pour les gestes à faire en cas d'étouffement
    Button bouton_geste_etouffement;

    // Fond de la page (pour la faire clignoter quand on clique sur SOS
    ImageView fond_page;

    // Bouton pour faire du SOS en morse avec le flash
    CardView bouton_sos_flash;

    boolean flashLightStatus = false;
    boolean sos_on = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_premiers_secours, container, false);

        bouton_geste_arret_cardiaque = view.findViewById(R.id.bouton_geste_arret_cardiaque);
        bouton_geste_saignement = view.findViewById(R.id.bouton_geste_saignement);
        bouton_geste_fracture = (Button) view.findViewById(R.id.bouton_geste_fracture);
        bouton_geste_etouffement = (Button) view.findViewById(R.id.bouton_geste_etouffement);
        bouton_sos_flash = view.findViewById(R.id.bouton_sos_flash);
        //fond_page = view.findViewById(R.id.fond_page);



        //Handler du bouton pour les gestes à faire en cas d'arrets cardiaques et respiratoires
        bouton_geste_arret_cardiaque.setOnClickListener((v) -> {
            Log.e("bouton_geste_arret_card", "setOnClickListener");
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentArretCardiaque());
            fragmentTransaction.addToBackStack("fragment_arret_cardiaque");
            fragmentTransaction.commit();
        });

        //Handler du bouton pour les gestes à faire en cas de saignements
        bouton_geste_saignement.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentSaignement());
            fragmentTransaction.addToBackStack("fragment_saignement");
            fragmentTransaction.commit();
        });

        //Handler du bouton pour les gestes à faire en cas de fracture, entorse oi luxation
        bouton_geste_fracture.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentFracture());
            fragmentTransaction.addToBackStack("fragment_fracture");
            fragmentTransaction.commit();
        });

        //Handler du bouton pour les gestes à faire en cas d'étouffement
        bouton_geste_etouffement.setOnClickListener((v) -> {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentEtouffement());
            fragmentTransaction.addToBackStack("fragment_etouffement");
            fragmentTransaction.commit();
        });

        //Handler du bouton pour les gestes à faire en cas d'étouffement
        bouton_sos_flash.setOnClickListener((v) -> {
            try{
                if(sos_on){
                    sos_on = false;
                } else {
                    sos_on = true;
                    ThreadSOS thread_sos = new ThreadSOS();
                    thread_sos.setDaemon(true);
                    thread_sos.start();
                }
            } catch (Throwable t){
                sos_on = false;
                t.printStackTrace();
            }
        });



        return view;
    }

    private class ThreadSOS extends Thread {
        ToneGenerator toneG;

        public ThreadSOS() {
            toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        }

        @TargetApi(Build.VERSION_CODES.O)
        public void run() {
            while(sos_on){
                try {
                    courte();
                    courte();
                    courte();

                    longue();
                    longue();
                    longue();

                    courte();
                    courte();
                    courte();
                } catch (InterruptedException e) {
                    //toneG.release();
                    e.printStackTrace();
                }

            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        private void courte() throws InterruptedException {
            if(sos_on){
                toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 100);
                vibrate(100, getContext());
                flashLightOn();
                sleep(100);
                flashLightOff();
                sleep(200);
            }
        }

        private void longue() throws InterruptedException {
            if(sos_on){
                toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 1000);
                vibrate(1000, getContext());
                flashLightOn();
                sleep(1000);
                flashLightOff();
                sleep(200);
            }
        }
    }


    public void vibrate(long milliseconds, Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(milliseconds);
        }
    }

    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(getContext().CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(getContext().CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



}
