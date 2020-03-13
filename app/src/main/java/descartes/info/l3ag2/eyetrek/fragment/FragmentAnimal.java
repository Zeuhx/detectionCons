package descartes.info.l3ag2.eyetrek.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.fragment.animalanalysis.FragmentClogs;
import descartes.info.l3ag2.eyetrek.fragment.animalanalysis.FragmentHands;
import descartes.info.l3ag2.eyetrek.fragment.animalanalysis.FragmentPads;

/**
 * Détermination général d'une empreinte: http://svt-barcelo.fr/detente/traces/traces.php | https://www.ma-chasse.com/mammif.shtml
 * Fragment Parent pour la détection des empreintes
 */
public class FragmentAnimal extends Fragment {


    public FragmentAnimal() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animal, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.groupe);
        Button next = view.findViewById(R.id.next);
        ImageButton cancel = view.findViewById(R.id.cancel);

        cancel.setOnClickListener((v)->{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contenu_fragment, new FragmentMenu());
            fragmentTransaction.commit();
        });


        next.setOnClickListener((v) -> {
            Log.e("TESt","OK");
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                createAlertBox("Erreur", "Veuillez sélectionner un choix !");
            } else {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //Id du bouton radio sélectionné
                int selectedId = radioGroup.getCheckedRadioButtonId();
                //On sélectionne le bouton à partir de l'id
                RadioButton radioButton = (RadioButton) view.findViewById(selectedId);
                Log.e("NAME", radioButton.getText().toString());
                //Empreintes en formes de Mains
                if (radioButton.getText().toString().equals("Mains")) {
                    fragmentTransaction.replace(R.id.contenu_fragment, new FragmentHands());
                    fragmentTransaction.addToBackStack("fragment_hands");
                    fragmentTransaction.commit();
                } else if (radioButton.getText().toString().equals("Coussinets")) {
                    fragmentTransaction.replace(R.id.contenu_fragment, new FragmentPads());
                    fragmentTransaction.addToBackStack("fragment_pads");
                    fragmentTransaction.commit();
                } else {
                    fragmentTransaction.replace(R.id.contenu_fragment, new FragmentClogs());
                    fragmentTransaction.addToBackStack("fragment_clogs");
                    fragmentTransaction.commit();
                }
            }
        });
        return view;
    }

    /**
     * Fonction permettant de créer une AlertBox
     *
     * @param title
     * @param msg
     */

    private void createAlertBox(String title, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Oups ! ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

}
