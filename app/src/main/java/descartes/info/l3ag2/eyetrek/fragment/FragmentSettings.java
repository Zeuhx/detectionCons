package descartes.info.l3ag2.eyetrek.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import descartes.info.l3ag2.eyetrek.pojo.Account;
import descartes.info.l3ag2.eyetrek.service.ApiService;
import descartes.info.l3ag2.eyetrek.pojo.Accounts;
import descartes.info.l3ag2.eyetrek.R;
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
 * Ce fragment correspond au fragment de modification de profil
 */
public class FragmentSettings extends Fragment {

    //SharedPreferences = Cache
    private static final String PREFS = "PREFERENCES";
    private static final String DIDACTICIEL = "DIDACTICIEL";
    SharedPreferences sharedPreferences;
    private boolean change = false;

    public FragmentSettings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        EditText email = view.findViewById(R.id.email);
        EditText password1 = view.findViewById(R.id.password1);
        EditText password2 = view.findViewById(R.id.password2);
        TextView year_exp = view.findViewById(R.id.year);
        Button plus = view.findViewById(R.id.plus);
        Button minus = view.findViewById(R.id.minus);
        Button modify = view.findViewById(R.id.modify);
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String prefs_lastname = sharedPreferences.getString("last_name", null);
        String prefs_firstname = sharedPreferences.getString("first_name", null);
        String prefs_yearexp = sharedPreferences.getString("year_exp", null);
        String prefs_age = sharedPreferences.getString("birthday", null);
        String prefs_email = sharedPreferences.getString("email", null);
        year_exp.setText(prefs_yearexp);
        JsonObject update = new JsonObject();
        //DÉBUT
        //Modification de nb années d'expériences
        /**
         * Click sur le bouton "+"
         */
        plus.setOnClickListener((v) -> {
            int yearchange = Integer.parseInt(year_exp.getText().toString()) + 1;
            year_exp.setText(String.valueOf(yearchange));
            if (!(checkAge(prefs_age, String.valueOf(yearchange)))) {
                yearchange = Integer.parseInt(year_exp.getText().toString()) - 1;
                year_exp.setText(String.valueOf(yearchange));
            }
        });
        /**
         * Click sur le bouton "-"
         */
        minus.setOnClickListener((v) -> {
            int yearchange = Integer.parseInt(year_exp.getText().toString()) - 1;
            if (yearchange < 0) {
                createAlertBox("Erreur", "Les années d'expériences ne peuvent être inférieur à 0 !");
                year_exp.setText(String.valueOf(0));
            } else {
                year_exp.setText(String.valueOf(String.valueOf(yearchange)));
            }
        });
        /**
         * Click sur le bouton de modification
         */
        modify.setOnClickListener((v) -> {
            Account account = new Account();
            String check = "1";
            String prefs_id = sharedPreferences.getString("id", null);
            String prefs_birthday = sharedPreferences.getString("birthday", null);
            String prefs_password = sharedPreferences.getString("password", null);
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS, MODE_PRIVATE).edit();
            //Vérification des champs
            if (isEmpty(email) && isEmpty(password1) && (year_exp.getText().toString().equals(prefs_yearexp))) {
                createAlertBox("Information", "Vous n'avez modifié aucun champ, il n'y aura pas donc de changement affecté à votre profil");
            } else if (!(isEmpty(email)) && !(Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())) {
                email.setError("Veuillez entrer un email valide");
            } else if (!(isEmpty(email)) && email.getText().toString().equals(prefs_email)) {
                email.setError("Veuillez entrer un e-mail différent de l'actuel");
            } else if (!(isEmpty(password1)) && !(isEqual(password1, password2))) {
                password2.setError("Les deux champs 'Mot de passe' doivent correspondrent");
            } else {
                //Tout les formats "sont validés"
                if (!(isEmpty(email))) {
                    //Appel au pour vérifier si le mail existe déjà
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
                            //Si le mail n'existe pas déjà
                            else if (accounts.getAccounts().isEmpty()) {
                                //Instanciation d'un nouveau compte pour ensuite l'envoyer pour la modification
                                account.setId(Integer.parseInt(prefs_id));
                                account.setLastName(prefs_lastname);
                                account.setFirstName(prefs_firstname);
                                account.setBirthday(prefs_birthday);
                                account.setYearExp(Integer.parseInt(prefs_yearexp));
                                account.setEmail(email.getText().toString());
                                account.setPassword(prefs_password);
                                //MàJ du cache
                                //TODO: Ajouter les commits une fois le debug fait
                                editor.putString("email", email.getText().toString());
                                change = true;
                                //Pas besoin de faire un appel à la BDD pour le mot de passe et le nb d'années d'expérience
                                //Instanciation d'un nouveau Account pour ensuite l'ajouter à la modification
                                if (!(isEmpty(password1))) {
                                    account.setId(Integer.parseInt(prefs_id));
                                    account.setLastName(prefs_lastname);
                                    account.setFirstName(prefs_firstname);
                                    account.setBirthday(prefs_birthday);
                                    account.setYearExp(Integer.parseInt(prefs_yearexp));
                                    account.setEmail(prefs_email);
                                    account.setPassword(getSHA(password1.getText().toString()));
                                    //MàJ du cache
                                    editor.putString("password", getSHA(password1.getText().toString()));
                                    change = true;
                                }
                                //Instanciation d'un nouveau Account pour ensuite l'ajouter à la modification
                                if (!(year_exp.getText().toString().equals(prefs_yearexp))) {
                                    account.setId(Integer.parseInt(prefs_id));
                                    account.setLastName(prefs_lastname);
                                    account.setFirstName(prefs_firstname);
                                    account.setBirthday(prefs_birthday);
                                    account.setYearExp(Integer.parseInt(year_exp.getText().toString()));
                                    account.setEmail(prefs_email);
                                    account.setPassword(prefs_password);
                                    //MàJ du cache
                                    editor.putString("year_exp", year_exp.getText().toString());
                                    change = true;
                                }
                                if (change) {
                                    Log.e("Compte à modifier", account.toJson().toString());
                                    updateService(account);
                                }
                            } else {
                                createAlertBox("ERREUR", "Email déjà existant");
                                change = false;
                            }
                        }

                        @Override
                        public void onFailure(Call<Accounts> call, Throwable t) {
                            Log.e("ERROR", "MÉTHODE onFailure APPELLÉE");
                        }
                    });
                }
                //Pas besoin de faire un appel à la BDD pour le mot de passe et le nb d'années d'expérience
                //Instanciation d'un nouveau Account pour ensuite l'ajouter à la modification
                if (!(isEmpty(password1))) {
                    account.setId(Integer.parseInt(prefs_id));
                    account.setLastName(prefs_lastname);
                    account.setFirstName(prefs_firstname);
                    account.setBirthday(prefs_birthday);
                    account.setYearExp(Integer.parseInt(prefs_yearexp));
                    account.setEmail(prefs_email);
                    account.setPassword(getSHA(password1.getText().toString()));
                    //MàJ du cache
                    editor.putString("password", getSHA(password1.getText().toString()));
                    change = true;
                }
                //Instanciation d'un nouveau Account pour ensuite l'ajouter à la modification
                if (!(year_exp.getText().toString().equals(prefs_yearexp))) {
                    account.setId(Integer.parseInt(prefs_id));
                    account.setLastName(prefs_lastname);
                    account.setFirstName(prefs_firstname);
                    account.setBirthday(prefs_birthday);
                    account.setYearExp(Integer.parseInt(year_exp.getText().toString()));
                    account.setEmail(prefs_email);
                    account.setPassword(prefs_password);
                    //MàJ du cache
                    editor.putString("year_exp", year_exp.getText().toString());
                    change = true;
                }
                if (change) {
                    Log.e("Compte à modifier", account.toJson().toString());
                    updateService(account);
                }
            }
        });
        return view;
    }

    /**
     * Méthode permettant de mettre à jour un compte dans la BDD
     */

    public void updateService(Account account) {
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String prefs_id = sharedPreferences.getString("id", null);
        ApiService apiService = new Retrofit.Builder().baseUrl(ApiService.LINK).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), account.toJson2().toString());
        Call<ResponseBody> call = apiService.updateAccount(prefs_id, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                int statusCode = response.code();
                if ((statusCode == 400) || (statusCode == 404)) {
                    createAlertBox("ERREUR", "Serveur momentanément indisponible/n Veuillez nous excusez pour la gêne occasioné.");
                    Log.e("ERROR", "ERREUR AU NIVEAU DU LIEN");
                } else if (statusCode == 403) {
                    Log.e("ERROR", "FORBIDDEN");
                } else {
                    createAlertBox("Information", "Les informations de votre compte ont bien été modifiées ! ");
                    Log.e("UPDATED", "Le compte à bien été mis à jour");

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ERROR", "PROBLEME DE CONNEXION INTERNET");
            }
        });
    }

    /**
     * Fonction permettant de créer une AlertBox
     *
     * @param title
     * @param msg
     */

    private void createAlertBox(String title, String msg) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
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

    /**
     * Fonction permettant de vérifier si l'Age est valide
     *
     * @param age
     * @param year_exp
     * @return
     */
    public boolean checkAge(String age, String year_exp) {
        if (ageCalcul(age).getYears() <= Integer.parseInt(year_exp)) {
            createAlertBox("Erreur", "Vos années d'expériences ne peuvent être supérieur ou égal à votre âge !");
            return false;
        } else if (ageCalcul(age).getYears() - 6 < Integer.parseInt(year_exp)) {
            createAlertBox("Erreur", "Les années d'expériences ne comptabilisées qu'à partir de 6 ans !");
            return false;
        } else {
            return true;
        }
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
     * Méthode ajoutant à un objet Json un intitulé suivit d'une valeur
     *
     * @param jsonObject
     * @param field
     * @param value
     */
    public void addToJson(JsonObject jsonObject, String field, String value) {
        if (field.equals("year_exp")) {
            try {
                jsonObject.addProperty(field, Integer.parseInt(value));
            } catch (JsonIOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                jsonObject.addProperty(field, value);
            } catch (JsonIOException e) {
                e.printStackTrace();
            }
        }
    }

}
