package descartes.info.l3ag2.eyetrek.userAccount.MyRequest;


import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkDispatcher;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
//import com.example.myapplication.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Saad-Allah MAHI
 * cette classe nous permet de gérer les requetes de connexion , inscription et modification de données
 */
public class MyRequest {
    private Context context ;
    private RequestQueue queue ;

    public MyRequest ( Context context  , RequestQueue queue){
        this.context = context ;
        this.queue = queue ;
    }

    /**
     * method qui nous permet de faire l'inscription
     * @param firstName
     * @param lastName
     * @param birthday
     * @param yearExp
     * @param email
     * @param password
     * @param passwordV
     * @param callback
     */
    public void Register(final String firstName, final String lastName, final String birthday, final String yearExp,final String email,final String password,final String passwordV, final RegisterCallback callback){

        String url = "http://www.ens.math-info.univ-paris5.fr/~ij00084/register.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Map<String,String> errors =new HashMap<>();
                try {
                    JSONObject json = new JSONObject(response);
                    boolean  error = json.getBoolean("error");

                    if (!error) {
                        //inscription s'est bien deroulée
                        callback.onSuccess("");
                    }else {
                        JSONObject messages = json.getJSONObject("message");
                        if (messages.has("firstname")){
                            errors.put("firstname" ,messages.getString("firstname") );
                        }
                        if (messages.has("lastname")){
                            errors.put("lastname" ,messages.getString("lastname") );
                        }
                        if (messages.has("birthday")){
                            errors.put("birthday" ,messages.getString("birthday") );
                        }
                        if (messages.has("yearexp")){
                            errors.put("yearexp" ,messages.getString("yearexp") );
                        }
                        if (messages.has("email")){
                            errors.put("email" ,messages.getString("email") );
                        }
                        if (messages.has("password")){
                            errors.put("password" ,messages.getString("password") );
                        }
                        callback.inputErrors(errors);
                    }


                    Log.d("APP", response);
                }catch (JSONException e) {
                    e.printStackTrace () ;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof NetworkError){
                    callback.onError("Impossible de se connecter");
                }else if (error instanceof  VolleyError){
                    callback.onError("une erreur c'est produite");
                }

            }
        }){
            @Override
            protected Map<String, String > getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>( ) ;
                map.put("firstname",firstName);
                map.put("lastname", lastName) ;
                map.put("birthday",birthday);
                map.put("yearexp",yearExp);
                map.put("email",email);
                map.put("password",password);
                map.put("passwordV",passwordV);

                return map ;
            }
        };
        queue.add(request);
    }

    public interface RegisterCallback {
        void onSuccess (String message);
        void inputErrors(Map<String, String> errors);
        void onError(String message);
    }

    /**
     * method qui nous permet de se connecter
     * @param email
     * @param password
     * @param callback
     */
    public  void connection (final String email , final String password , final LoginCallback callback ){
        String url = "http://www.ens.math-info.univ-paris5.fr/~ij00084/login.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject json = null ;
                try {
                    json = new JSONObject(response);
                    boolean error = json.getBoolean("error");

                    if (!error ){
                        String id = json.getString("id");
                        String email = json.getString("email");
                        callback.onSuccess(id, email);

                    }else {
                        callback.onError(json.getString("message"));
                    }
                }catch (JSONException e ){

                    callback.onError("une erreur c'est produite");
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError){
                    callback.onError("Impossible de se connecter ");

                }else if (error instanceof  VolleyError){
                    callback.onError("une erreur c'est produite");
                }

            }
        }) {
            @Override
            protected Map<String  ,String > getParams() throws AuthFailureError {

                Map<String , String> map = new HashMap<>( ) ;
                map.put("email" ,email);
                map.put("password",password);

                return map ;
            }
        };
        queue.add(request);
    }
    public  interface LoginCallback{
        void onSuccess (String id , String email );

        void onError(String message);
    }

    /**
     * method pour modifier les informations personnel
     * @param id
     * @param firstName
     * @param lastName
     * @param birthday
     * @param yearExp
     * @param email
     * @param password
     * @param passwordV
     * @param callback
     */
    public void Update(final String id, final String firstName, final String lastName, final String birthday, final String yearExp,final String email,final String password,final String passwordV, final UpdateCallback callback){

        String url = "http://www.ens.math-info.univ-paris5.fr/~ij00084/update.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Map<String,String> errors =new HashMap<>();
                try {
                    JSONObject json = new JSONObject(response);
                    boolean  error = json.getBoolean("error");

                    if (!error) {
                        //inscription s'est bien deroulée
                        callback.onSuccess("");
                    }else {
                        JSONObject messages = json.getJSONObject("message");
                        if (messages.has("firstName")){
                            errors.put("firstName" ,messages.getString("firstName") );
                        }
                        if (messages.has("lastName")){
                            errors.put("lastName" ,messages.getString("lastName") );
                        }
                        if (messages.has("birthday")){
                            errors.put("birthday" ,messages.getString("birthday") );
                        }
                        if (messages.has("yearExp")){
                            errors.put("yearExp" ,messages.getString("yearExp") );
                        }
                        if (messages.has("email")){
                            errors.put("email" ,messages.getString("email") );
                        }
                        if (messages.has("password")){
                            errors.put("password" ,messages.getString("password") );
                        }
                        callback.inputErrors(errors);
                    }


                    Log.d("APP", response);
                }catch (JSONException e) {
                    e.printStackTrace () ;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof NetworkError){
                    callback.onError("Impossible de se connecter");
                }else if (error instanceof  VolleyError){
                    callback.onError("une erreur c'est produite");
                }

            }
        }){
            @Override
            protected Map<String, String > getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<>( ) ;
                map.put("id",id);
                map.put("firstname",firstName);
                map.put("lastname", lastName) ;
                map.put("birthday",birthday);
                map.put("yearexp",yearExp);
                map.put("email",email);
                map.put("password",password);
                map.put("passwordV",passwordV);

                return map ;
            }
        };
        queue.add(request);
    }

    public interface UpdateCallback {
        void onSuccess (String message);
        void inputErrors(Map<String, String> errors);
        void onError(String message);
    }



}
