package descartes.info.l3ag2.eyetrek.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.UpdateAPK;


public class SplashActivity extends AppCompatActivity {



    // Timer d'écoulement du chargement
    private Timer timerBar;

    //private ProgressBar splashBar;
    private int cpt = 0;

    private TextView viewBar;

    /**
     * Permet d'afficher le splash-screen
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //No NotificationBar - Source : https://stackoverflow.com/questions/4222713/hide-notification-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       // splashBar = (ProgressBar) findViewById(R.id.splashScreenBar);
       // splashBar.setProgress(0);

        // RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutsplash);

        // Permet de récupérer le titre dans le splash
        viewBar = findViewById(R.id.viewBar);

        if(viewBar == null){
            Log.e("SplashActivity", "la variable viewBar est null");
            Log.e("SplashActivity", "R.id.viewBar : " + R.id.viewBar);
             while(viewBar == null){
                 viewBar = findViewById(R.id.viewBar);
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }

        }

        // Permet d'ouvrir une police personnalisée
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/antro_vectra.ttf");

        if(font == null){
            Log.e("SplashActivity", "la variable font est null");
            while(font == null){
                font = Typeface.createFromAsset(getAssets(), "fonts/antro_vectra.ttf");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        // Permet d'appliquer une police personnalisée au titre
        viewBar.setTypeface(font);

        UpdateAPK testUpdate = new UpdateAPK(getApplicationContext());


        // Déclaration de l'animation
        Animation frombottom;
        frombottom = AnimationUtils.loadAnimation(this,R.anim.frombot);

        //Application de l'animation sur le texte
        viewBar.setAnimation(frombottom);


        // Ecoulement du timer
        final long period = 15;
        timerBar = new Timer();
        timerBar.schedule(new TimerTask() {
            @Override
            public void run() {
                if (cpt < 100) {
                    //   splashBar.setProgress(cpt);
                    cpt++;
                } else {
                    timerBar.cancel();
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 0, period);

    }
}

