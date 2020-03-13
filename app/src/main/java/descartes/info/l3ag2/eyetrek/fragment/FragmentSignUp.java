package descartes.info.l3ag2.eyetrek.fragment;


import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.joda.time.LocalDate;
import org.joda.time.Years;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import descartes.info.l3ag2.eyetrek.pojo.Account;
import descartes.info.l3ag2.eyetrek.pojo.Accounts;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.service.ApiService;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Ayaz ABDUL CADER
 * Ce fragment est le fragment d'inscription
 */

public class FragmentSignUp extends Fragment {
    private static final String PREFS = "PREFERENCES";

    public FragmentSignUp() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        Button signup = view.findViewById(R.id.signup_button);
        final EditText email = view.findViewById(R.id.email_signup);
        final EditText password = view.findViewById(R.id.password_signup);
        final EditText password_2 = view.findViewById(R.id.password2_signup);
        final EditText last_name = view.findViewById(R.id.last_name);
        final EditText first_name = view.findViewById(R.id.first_name);
        final TextView birthday = view.findViewById(R.id.birthday);
        final EditText year_exp = view.findViewById(R.id.year_exp);

        signup.setOnClickListener((v) -> {
            if (!(isConnected())) {

                createAlertBox("ERREUR", "Veuillez activer votre connexion internet pour vous s'inscrire");

            } else if (isEmpty(last_name)) {
                last_name.setError("Veuillez remplir ce champ");
            } else if (isEmpty(first_name)) {
                first_name.setError("Veuillez remplir ce champ");
            } else if (birthday.getText() == null || (birthday.getText().length() == 0)) {
                birthday.setError("Veuillez remplir ce champ");
            } else if (isEmpty(year_exp)) {
                year_exp.setError("Veuillez remplir ce champ");
            } else if (ageCalcul(birthday.getText().toString()).getYears() < 13) {
                birthday.setFocusable(true);
                birthday.setFocusableInTouchMode(true);
                birthday.setClickable(true);
                birthday.setError("Vous devez être âgé de minimum 13 ans");
            } else if (ageCalcul(birthday.getText().toString()).getYears() >= 100) {
                birthday.setFocusable(true);
                birthday.setFocusableInTouchMode(true);
                birthday.setClickable(true);
                birthday.setError("L'âge maximum est de 99 ans");
            } else if (ageCalcul(birthday.getText().toString()).getYears() <= Integer.parseInt(year_exp.getText().toString())) {
                year_exp.setError("Vos années d'expériences ne peuvent être supérieur ou égal à votre âge !");
            } else if (ageCalcul(birthday.getText().toString()).getYears() - 6 < Integer.parseInt(year_exp.getText().toString())) {
                year_exp.setError("Les années d'expérience ne contabilisés qu'à partir de 6 ans !");
            } else if (isEmpty(email)) {
                email.setError("Veuillez remplir ce champ");
            } else if (!(Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())) {
                email.setError("Veuillez rentrer un email valide");
            } else if (isEmpty(password)) {
                password.setError("Veuillez remplir ce champ");
            } else if (password.length() < 8) {
                password.setError("Votre doit faire au minimum 8 caractères");
            } else if (!(isEqual(password, password_2))) {
                password_2.setError("Les mots de passes ne correspondent pas");
            } else {
                ApiService apiService = new Retrofit.Builder().baseUrl(ApiService.LINK).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class);
                Call<Accounts> call = apiService.getAccount("compte.email,eq," + email.getText().toString(), 1);

                call.enqueue(new Callback<Accounts>() {
                    @Override
                    public void onResponse(Call<Accounts> call, Response<Accounts> response) {
                        int statusCode = response.code();
                        Accounts accounts = response.body();
                        if ((statusCode == 400) || (statusCode == 404)) {
                            createAlertBox("ERREUR", "Serveur momentanément indisponible" + System.getProperty("line.separator") + "Veuillez nous excusez pour la gêne occasioné.");
                            Log.e("ERROR", "ERREUR AU NIVEAU DU LIEN");
                        }

                        //Mail inexistant
                        else if (accounts.getAccounts().size() == 0) {
                            signUpService(last_name, first_name, birthday, year_exp, email, password);
                        } else {
                            createAlertBox("ERREUR", "Email déjà existant");
                        }
                    }

                    @Override
                    public void onFailure(Call<Accounts> call, Throwable t) {
                        Log.e("ERROR", "MÉTHODE onFailure APPELLÉE");
                    }
                });
            }
        });
        birthday.setOnClickListener((v)->{
            DialogFragment newFragment = new DatePickFragment();
            newFragment.show(getFragmentManager(), "datePicker");
            if (Build.VERSION.SDK_INT >= 23) {
                Toast.makeText(getContext(), "Veuillez appuyer sur l'année en haut à droite pour naviguer plus rapidement !", Toast.LENGTH_SHORT).show();
            }

        });
        return view;

    }

    /**
     * Méthode permettant d'ajouter un compte dans la BDD
     *
     * @param last_name
     * @param first_name
     * @param birthday
     * @param year_exp
     * @param email
     * @param password
     */

    public void signUpService(EditText last_name, EditText first_name, TextView
            birthday, EditText year_exp, final EditText email, EditText password) {
        Account account = new Account();
        account.setLastName(last_name.getText().toString());
        account.setFirstName(first_name.getText().toString());
        account.setBirthday(birthday.getText().toString());
        account.setYearExp(Integer.parseInt(year_exp.getText().toString()));
        account.setEmail(email.getText().toString());
        account.setPassword(getSHA(password.getText().toString()));
        ApiService apiService = new Retrofit.Builder().baseUrl(ApiService.LINK).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class);
        //Ajout du Json dans le body de la requête POST
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), account.toJson().toString());

        Call<ResponseBody> call = apiService.setAccount(body);
        if (call.request().body() == null) {
            createAlertBox("ERREUR", "Serveur momentanément indisponible" + System.getProperty("line.separator") + "Veuillez nous excusez pour la gêne occasioné.");
            Log.e("ERROR", "RESPONSE BODY NULL");
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                int statusCode = response.code();
                if ((statusCode == 400) || (statusCode == 404)) {
                    createAlertBox("ERREUR", "Serveur momentanément indisponible/n Veuillez nous excusez pour la gêne occasioné.");
                    Log.e("ERROR", "ERREUR AU NIVEAU DU LIEN");
                } else {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS, MODE_PRIVATE).edit();
                    editor.putString("email", email.getText().toString()).commit();
                    editor.putString("year_exp", account.getYearExp().toString()).commit();
                    editor.putString("last_name", account.getLastName()).commit();
                    editor.putString("first_name", account.getFirstName()).commit();
                    editor.putString("birthday", account.getBirthday()).commit();
                    editor.putString("id",getResponsBody(response.body().byteStream())).commit();
                    editor.commit();
                    //Redirection vers le fragment Profil
                    alertBoxSignIn();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ERROR", "PROBLEME DE CONNEXION INTERNET");
            }
        });
    }


    /**
     * Méthode permettant de récupérer l'id du compte venant d'être ajouté
     * Source: https://stackoverflow.com/questions/22325641/retrofit-callback-get-response-body/30684715
     * @param response
     * @return
     */
    public String getResponsBody(InputStream response){
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(response));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  sb.toString();

    }




    /**
     * Fonction indiquant si le champ est vide
     *
     * @param editText
     * @return
     */

    public boolean isEmpty(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Méthode permettant de créer une AlertBox
     *
     * @param title
     * @param msg
     */

    private void createAlertBox(String title, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    /**
     * Méthode créant une alertBox pour valider l'inscription
     */

    private void alertBoxSignIn() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("INFORMATION");
        alertDialog.setMessage("Bravo ! Vous êtes maintenant inscrit(e) sur l'application !");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentProfil()).commit();
            }
        }).show();
    }

    /**
     * Fonction permettant d'hasher en SHA-265 un String
     * Source: http://www.java2s.com/Code/Android/Date-Type/byteArrayToHexString.htm
     *
     * @param data
     * @return
     */

    public static String getSHA(String data) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            digester.update(data.getBytes());
            byte[] messageDigest = digester.digest();
            return byteArrayToHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    /**
     * Fonction permettant de transformer un byteArray en String
     * Source: http://www.java2s.com/Code/Android/Date-Type/byteArrayToHexString.htm
     *
     * @param array
     * @return
     */

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

    /**
     * Fonction permettant de savoir si l'appareil est connecté à Internet ou est entrain de s'y connecter
     *
     * @return
     */

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Fonction vérifiant les deux mot de passe sont égaux
     *
     * @param pswd
     * @param pswd2
     * @return
     */

    public boolean isEqual(EditText pswd, EditText pswd2) {
        if (pswd2.getText().toString().equals(pswd.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fonction permettant de calculer l'âge
     *
     * @param date
     * @return
     */

    public Years ageCalcul(String date) {
        StringTokenizer st = new StringTokenizer(date, "/");
        String jour = st.nextToken();
        String mois = st.nextToken();
        String annee = st.nextToken();
        LocalDate birthday = new LocalDate(Integer.parseInt(annee), Integer.parseInt(mois), Integer.parseInt(jour));
        LocalDate today = new LocalDate();
        Years age = Years.yearsBetween(birthday, today);
        return age;
    }


}
