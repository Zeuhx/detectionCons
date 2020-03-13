package descartes.info.l3ag2.eyetrek.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.userAccount.SessionManager;
import descartes.info.l3ag2.eyetrek.userAccount.SignIn;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Saad-Allah MAHI
 * Ce Fragment correspond au Fragment Profil
 */
public class Profil extends Fragment {


    private static final String PREFS = "app_prefs";
    private static final String DIDACTICIEL = "PREFERENCE";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor ;

    public Profil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profil, container, false);


        TextView username = view.findViewById(R.id.username6);
        ImageButton logout = view.findViewById(R.id.logout);
        ImageButton settings = view.findViewById(R.id.settings);
        CardView score = view.findViewById(R.id.button);
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        username.setText(email);


        /**
         * Bouton pour acceder à l'historique
         */
        CardView historic = view.findViewById(R.id.button2);
        historic.setOnClickListener((v)->{
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentHistory()).commit();
        });


        /**
         * Bouton pour acceder au classement
         */
//        score.setOnClickListener((v)->{
//            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentClassement()).commit();
//        });


        /**
         * Click sur le bouton de déconnexion
         */
        logout.setOnClickListener((v) -> {
            getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit();
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentLog()).commit();
        });

        /**
         * Click sur le bouton de paramètres
         */
        settings.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentOptions()).commit();
        });

        return view;
    }


}
