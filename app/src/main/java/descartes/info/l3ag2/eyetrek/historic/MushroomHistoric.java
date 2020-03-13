package descartes.info.l3ag2.eyetrek.historic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.classes.AdapterRecyclerViewHistory;

/**
 * Created by Saad-Allah MAHI
 * télécharger et afficher l'historique des champignons analysées
 */
public class MushroomHistoric extends Fragment {

    String Url ="http://www.ens.math-info.univ-paris5.fr/~ij00084/mushroomH.php?id_user=";
    private static final String PREFS = "app_prefs";
    SharedPreferences sharedPreferences;
    Button bird,leaf;
    ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> listPoint = new ArrayList<>();
    ListView lv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.history, container, false);
        bird = (Button) view.findViewById(R.id.button7);
        leaf = (Button) view.findViewById(R.id.leaf);
        lv= (ListView) view.findViewById(R.id.displaylist);
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", null);

        new Webservice().execute(Url+id);
        return view;

    }

    /**
     * class qui execute le lien donnée en paramétere pour récupérer la liste des champignons analysées
     */
    class Webservice extends AsyncTask<String , Void ,String> {

        @Override
        protected void onPreExecute () {
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Connexion au serveur ..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }



        @Override
        protected String doInBackground(String...strings) {
            StringBuilder result = new StringBuilder();
            try {

                HttpURLConnection conn;

                URL url = new URL(strings[0]);
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true);
                conn.setRequestMethod("GET");



                conn.setRequestProperty("Accept-Charset", "UTF-8");

                conn.setConnectTimeout(1000);

                conn.connect();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                conn.disconnect();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return  result.toString();
        }
        @Override
        protected void  onPostExecute (String  JSON_Reponse ) {
            pDialog.dismiss();
            JSONObject jObj=null ;
            JSONArray jArr =null;

            // try parse the string to a JSON object
            try {
                jArr = new JSONArray(JSON_Reponse.toString());

                for (int x=0 ;x<jArr.length();x++ ) {

                    jObj =jArr.getJSONObject(x) ;
                    String p=jObj.getString("mushroom") ;

                    HashMap<String,String> map =new HashMap<>();
                    map.put("mushroom",p);

                    listPoint.add(map) ;
                }


            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            ListAdapter adapter1 = new SimpleAdapter(
                    getContext(), listPoint,
                    R.layout.item, new String[] { "mushroom"},
                    new int[] { R.id.item0});

            lv.setAdapter(adapter1);


            bird.setOnClickListener((v) -> {
                getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new BirdHistoric()).commit();
            });
            leaf.setOnClickListener((v) -> {
                getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new LeafHistoric()).commit();
            });



        }

    }
}
