package descartes.info.l3ag2.eyetrek.rank;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import descartes.info.l3ag2.eyetrek.R;

public class MushroomRank extends Fragment {

    String Url ="http://www.ens.math-info.univ-paris5.fr/~ij00084/rankmushroom.php";
    private static final String PREFS = "app_prefs";
    SharedPreferences sharedPreferences;
    Button bird,leaf;
    ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> listPoint = new ArrayList<>();
    ListView lv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
//        View view =  inflater.inflate(R.layout.fragment_classement, container, false);
//        bird = (Button) view.findViewById(R.id.rank_bird);
//        leaf = (Button) view.findViewById(R.id.rank_leaf);
//        lv= (ListView) view.findViewById(R.id.rank_list);

        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", null);

        new MushroomRank.Webservice().execute(Url+id);
        return null;

    }
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

                    StringBuffer sb = new StringBuffer();

                    String id_user =jObj.getString("id_user") ;
                    String score=jObj.getString("scoremushroom");

                    sb.append(x+" ");
                    sb.append(id_user);

                    HashMap<String,String> map =new HashMap<>();
                    map.put(sb.toString(),score);

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
                getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new BirdRank()).commit();
            });
            leaf.setOnClickListener((v) -> {
                getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new LeafRank()).commit();
            });



        }

    }
}
