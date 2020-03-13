package descartes.info.l3ag2.eyetrek.userAccount;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Saad-Allah MAHI
 * cette classe nous permet de gérer la session du compte utilisateur
 */

public class SessionManager {

    private SharedPreferences prefs ;
    private SharedPreferences.Editor editor ;
    public final static String PREFS_NAME = "app_prefs";
    private final static int PRIVATE_MODE = 0 ;
    private final static String  IS_LOGGED ="isLogged";
    private  final  static String EMAIL = "email";
    private final static String ID = "id" ;
    private Context context;

    public SessionManager(Context context) {
        this.context = context ;
        prefs =context.getSharedPreferences(PREFS_NAME , PRIVATE_MODE);
        editor =prefs.edit();
    }

    /**
     * method pour verifier si l'utilisateur est connécté ou pas
     *
     * @return true si est connécté false sinon
     */
    public boolean isLogged() {
        return prefs.getBoolean(IS_LOGGED, false);
    }
    public String getEmail() {

        return prefs.getString(EMAIL, null);
    }
    public String getId(){

        return prefs.getString(ID, null);
    }


    /**
     * method pour inserer l'utilisateur en ligne afin de pouvoir récuperer son id et email depuis d'autre classe
     * @param id
     * @param email
     */
    public  void insertUser(String id , String email){
        editor.putBoolean(IS_LOGGED , true);
        editor.putString(ID,id);
        editor.putString(EMAIL,email);
        editor.commit();
    }

    /**
     * method pour qui supprime les informations de la connexion pour se déconnécter
     */
    public void logout (){
        editor.clear().commit();
    }


}

