package descartes.info.l3ag2.eyetrek.classes;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Classe qui permet l'affichage d'informations à l'analyse d'une feuille.
 *
 * Created by Antony Torbey on 06/03/2018.
 */

public class UtilitaireResultat {

    public final static char SEPARATOR=';';


    public UtilitaireResultat(){}


    public static ArrayList<String> readFile(InputStream file){
        ArrayList<String> res = new ArrayList<String>();


        InputStreamReader r = new InputStreamReader(file);
        BufferedReader br = new BufferedReader(r);

        try {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                res.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return res;
    }

    public static ArrayList<String> getMushInfo(String id, Activity activity){
        ArrayList<String> res = new ArrayList<String>();
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<String[]> data = new ArrayList<String[]>();
        String sep = new Character(SEPARATOR).toString();

        res.add(ajouter_majuscules(id.replaceAll("_", " ")));

        lines = readFile(activity.getResources().openRawResource(R.raw.mushrooms));

        // On reinitialise la variable data
        data = new ArrayList<String[]>();

        for(String line : lines){
            String[] oneData = line.split(sep);
            data.add(oneData);
        }

        boolean trouve = false;
        for(int i=0 ; i<data.size() ; i++){
            String[] tabStr = data.get(i);
            if (tabStr[1].equals(id)) {
                res.add(tabStr[2]);
                trouve = true;
                break;
            } else {
                Log.w("getMushInfo()", "" + tabStr[1] + " --- " + id);
            }
        }
        if(!trouve){
            Log.e("getInfo()", "Aucun nom de plante trouvé pour cette entrée");
            res.add("Aucune description disponible pour le moment.\n\nCliquez sur cette case pour acceder à la page Wikipedia correspondante");
        }






        res.add("https://fr.wikipedia.org/wiki/"+id);

        res.add("drawable/"+id+".*");


        return res;
    }

    public static ArrayList<String> getInfo(String id, Activity activity){

        ArrayList<String> res = new ArrayList<String>();
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<String[]> data = new ArrayList<String[]>();
        String sep = new Character(SEPARATOR).toString();

        //res.add(ajouter_majuscules(id.replaceAll("_", " ")));









        lines = readFile(activity.getResources().openRawResource(R.raw.liste_leaf1));

        for(String line : lines){
            String[] oneData = line.split(sep);
            data.add(oneData);
        }

        boolean trouve = false;
        for(int i=0 ; i<data.size() ; i++){
            String[] tabStr = data.get(i);
            if (tabStr[2].equals(id)) {
                res.add(tabStr[1]);
                trouve = true;
                break;
            }
        }
        if(!trouve){
            Log.e("getInfo()", "Aucun nom de plante trouvé pour cette entrée");
            res.add(ajouter_majuscules(id.replaceAll("_", " ")));
        }




        lines = readFile(activity.getResources().openRawResource(R.raw.liste_leaf_details));

        // On reinitialise la variable data
        data = new ArrayList<String[]>();

        for(String line : lines){
            String[] oneData = line.split(sep);
            data.add(oneData);
        }

        trouve = false;
        for(int i=0 ; i<data.size() ; i++){
            String[] tabStr = data.get(i);
            if (tabStr[2].equals(id)) {
                res.add(tabStr[1]);
                trouve = true;
                break;
            }
        }
        if(!trouve){
            Log.e("getInfo()", "Aucun nom de plante trouvé pour cette entrée");
            res.add("Aucune description disponible pour le moment.\n\nCliquez sur cette case pour acceder à la page Wikipedia correspondante");
        }






        res.add("https://fr.wikipedia.org/wiki/"+id);

        res.add("drawable/"+id+".*");




        /*
        ArrayList<String> res = new ArrayList<String>();
        res.add("Nom feuille");
        res.add("Description feuille, cette feuille originaire d'asie n'a jamais été aimée par les gens de là bas car elle sent le caca, très fort. \nUn arreté ministériel a été mis en vigueur pour la placer en enemie public n°1");
        res.add("drawable://" + "acer_palmatum.png");
        */




        return res;
    }



    public static String get_description_feuille(String id_feuille, Context context){
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<String[]> data = new ArrayList<String[]>();
        String sep = new Character(SEPARATOR).toString();

        String resultat = "";

        lines = readFile(context.getResources().openRawResource(R.raw.liste_leaf_details));

        // On reinitialise la variable data
        data = new ArrayList<String[]>();

        for(String line : lines){
            String[] oneData = line.split(sep);
            data.add(oneData);
        }

        boolean trouve = false;
        for(int i=0 ; i<data.size() ; i++){
            String[] tabStr = data.get(i);
            if (tabStr[2].equals(id_feuille)) {
                resultat = tabStr[1];
                trouve = true;
                break;
            }
        }
        if(!trouve){
            Log.e("get_description()", "Aucun nom de plante trouvé pour cette entrée");
            resultat = "Aucune description disponible pour le moment.";
        }

        return resultat;
    }



    public static String ajouter_majuscules(String str) {

        // Create a char array of given String
        char ch[] = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {

            // If first character of a word is found
            if (i == 0 && ch[i] != ' ' ||
                    ch[i] != ' ' && ch[i - 1] == ' ') {

                // If it is in lower-case
                if (ch[i] >= 'a' && ch[i] <= 'z') {

                    // Convert into Upper-case
                    ch[i] = (char)(ch[i] - 'a' + 'A');
                }
            }

            // If apart from first character
            // Any one is in Upper-case
            else if (ch[i] >= 'A' && ch[i] <= 'Z')

                // Convert into Lower-Case
                ch[i] = (char)(ch[i] + 'a' - 'A');
        }

        // Convert the char array to equivalent String
        String st = new String(ch);
        return st;
    }


}