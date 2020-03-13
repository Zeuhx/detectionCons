package descartes.info.l3ag2.eyetrek.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import descartes.info.l3ag2.eyetrek.activity.MainActivity;
import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Dorian QUABOUL
 * Updated by Ayaz ABDUL CADER
 */
public class FragmentMessage extends Fragment {


    public FragmentMessage() {

    }


    /**
     * Création du Fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        //Demande des permissions
        requestsPermissions();
        //Si les permissions ont été accordées on affiche la choix des analyses
        if (requestsPermissions()) {
            startActivity(new Intent(getActivity(),MainActivity.class));
            /*getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentMenu()).commit();
            getFragmentManager().beginTransaction().addToBackStack(null).commit();*/
        }

        return view;
    }

    /**
     * Méthode appelé lorsque l'utilisateur a quitté l'application mais qu'elle est toujours présente dans la pile
     */
    @Override
    public void onResume() {
        super.onResume();
        requestsPermissions();
        if (requestsPermissions()) {
            startActivity(new Intent(getActivity(),MainActivity.class));
            /*getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentMenu()).commit();
            getFragmentManager().beginTransaction().addToBackStack(null).commit();*/
        }
    }

    /**
     * Méthode appelé lorsqu'il y a une fenêtre qui pop, par exemple un appel ou un sms
     */
    @Override
    public void onPause() {
        super.onPause();
        requestsPermissions();
        if (requestsPermissions()) {
            startActivity(new Intent(getActivity(),MainActivity.class));
            /*getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentMenu()).commit();
            getFragmentManager().beginTransaction().addToBackStack(null).commit();*/
        }
    }

    /**
     * Fonction permettant de demander les permissions, et renvoi un booléen pour indiquer si les 2 permissions ont été accaordées
     * La permission Internet est déjà incluse
     * @return boolean
     */
    public boolean requestsPermissions() {
        //Tableau contenant toutes les permissions
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        //Pour demander la permission même si le "Ne plus me demander" a été coché
        for (String permission : permissions) {
            //Demande des permissions
            requestPermissions(permissions, 100);
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
