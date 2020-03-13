package descartes.info.l3ag2.eyetrek.fragment;


import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import descartes.info.l3ag2.eyetrek.pojo.Account;
import descartes.info.l3ag2.eyetrek.pojo.Accounts;
import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Ayaz ABDUL CADER
 * Ce fragment est le fragment de connexion
 */

public class FragmentSignIn extends Fragment {
    private static final String PREFS = "PREFERENCES";

    public FragmentSignIn() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin, container, false);

        Button signin = view.findViewById(R.id.signin_button);
        final EditText password = view.findViewById(R.id.password);
        final EditText email = view.findViewById(R.id.email);

        signin.setOnClickListener((v) -> {
            if (!(isConnected())) {
                createAlertBox("ERREUR", "Veuillez activer votre connexion internet pour vous connecter !");
            } else if (email.getText().toString().isEmpty()) {
                email.setError("Veuillez remplir ce champ");
            } else if (password.getText().toString().isEmpty()) {
                password.setError("Veuillez remplir ce champ");
            } else if (!(Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches())) {
                email.setError("Veuillez rentrer une adresse email valide");
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
                            createAlertBox("ERREUR", "L'Email n'existe pas !");
                        } else {
                            for (Account account : accounts.getAccounts()) {
                                //Mot de passe correspondant
                                if (getSHA(password.getText().toString()).equals(account.getPassword())) {
                                    //Ajout du mail dans le cache
                                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS, MODE_PRIVATE).edit();
                                    editor.putString("email", email.getText().toString()).commit();
                                    editor.putString("last_name",account.getLastName()).commit();
                                    editor.putString("first_name",account.getFirstName()).commit();
                                    editor.putString("year_exp", account.getYearExp().toString()).commit();
                                    editor.putString("birthday",account.getBirthday()).commit();
                                    editor.putString("id",account.getId().toString()).commit();
                                    editor.putString("password",account.getPassword()).commit();
                                    editor.commit();
                                    //Redirection vers la MainActivity et bloquage du bouton Back
                                    getFragmentManager().beginTransaction().replace(R.id.contenu_fragment, new FragmentProfil()).commit();
                                }

                                //Mot de passe erroné
                                else {
                                    createAlertBox("ERREUR", "Mauvais mot de passe !");
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Accounts> call, Throwable t) {
                        Log.e("ERROR", "MÉTHODE onFailure APPELLÉE");
                    }
                });
            }
        });
        return view;
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
     * Fonction permettant de créer une AlertBox
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
     * Fonction permettant de savoir si l'appareil est connecté à Internet ou est entrain de s'y connecter
     *
     * @return
     */

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


}
