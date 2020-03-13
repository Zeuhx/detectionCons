package descartes.info.l3ag2.eyetrek.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.AdapterRecyclerViewHistory;
import descartes.info.l3ag2.eyetrek.historic.BirdHistoric;
import descartes.info.l3ag2.eyetrek.historic.LeafHistoric;
import descartes.info.l3ag2.eyetrek.historic.MushroomHistoric;

/**
 * Created by Dorian Quaboul on 19/04/2018.
 * A simple {@link Fragment} subclass.
 */
public class FragmentHistory extends Fragment {

    private Button leaf,mushroom,bird;
    SharedPreferences sharedPreferences;
    private static final String PREFS = "app_prefs";
    public FragmentHistory() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.history, container, false);
        leaf = (Button) view.findViewById(R.id.leaf);
        bird = (Button) view.findViewById(R.id.button7);
        mushroom = (Button) view.findViewById(R.id.mushroom);
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String prefs_lastname = sharedPreferences.getString("id", null);
        Toast.makeText(getActivity(),prefs_lastname , Toast.LENGTH_SHORT).show();

        leaf.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new LeafHistoric()).commit();
        });
        bird.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new BirdHistoric()).commit();
        });
        mushroom.setOnClickListener((v) -> {
            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new MushroomHistoric()).commit();
        });

        getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new LeafHistoric()).commit();

        return view;
    }


    /**
     * source : https://stackoverflow.com/questions/9530921/list-all-the-files-from-all-the-folder-in-a-single-list
     * @param parentDir
     * @return
     */
  /*  private List<File> getListFiles(File parentDir) {

        List<File> inFiles = new ArrayList<>();

        if(!parentDir.exists()) {
            return inFiles;
        }
        File[] files = parentDir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                inFiles.add(file);
            }
        }

        return inFiles;
    }

    /**
     * On trie par ordre decroissant la liste des fichiers grace a leur timeStamp.
     * Plus la valeur du timeStamp est grande plus le fichier est recent.
     * @param list
     */
   /* private void sortFiles(List<File> list) {
        Collections.sort(list, (File f1, File f2) -> {

            if(extractTime(f1.getName()) > extractTime(f2.getName())) {
                //f1 est avant f2
                return -1;
            }
            else if (extractTime(f1.getName()) < extractTime(f2.getName())) {
                //f2 est avant f1
                return 1;
            }
            else {
                return 0;
            }
        });
    }

    /**
     * On extrait le time stamp d'un nom de fichier.
     * @param nomFichier
     * @return
     */
   /* private long extractTime(String nomFichier) {
        StringTokenizer tokenizer1 = new StringTokenizer(nomFichier,"_");
        //on ignore le premier token qui correspond soit a "Capture" ou "Video" ou "RecordBird"
        tokenizer1.nextToken();
        //on recupere la chaine de caractere ressemblant a "time.extension"
        String timeExt = tokenizer1.nextToken();

        StringTokenizer tokenizer2 = new StringTokenizer(timeExt,".");
        //on retourne le time en long qui correspond au premier token
        return Long.parseLong(tokenizer2.nextToken());
    }*/
}
