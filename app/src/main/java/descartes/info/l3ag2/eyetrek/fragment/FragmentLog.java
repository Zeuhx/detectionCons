package descartes.info.l3ag2.eyetrek.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.userAccount.SignIn;
import descartes.info.l3ag2.eyetrek.userAccount.SignUp;

/**
 * Created by Saad-Allah MAHI
 * Ce Fragment s'affiche quand l'utilisateur n'est pas connecté
 */
public class FragmentLog extends Fragment {


    public FragmentLog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        CardView signin = view.findViewById(R.id.signin);
        CardView signup = view.findViewById(R.id.signup);

        /**
         * Click sur le bouton de connexion
         */
        signin.setOnClickListener((v)->{
            getFragmentManager().beginTransaction().replace(R.id.contenu_fragment,new SignIn()).addToBackStack("fragment_signin").commit();
        });

        /**
         * Click sur le bouton d'inscription
         */
        signup.setOnClickListener((v)->{
            getFragmentManager().beginTransaction().replace(R.id.contenu_fragment,new SignUp()).addToBackStack("fragment_signup").commit();
        });


        return view ;
    }

}
