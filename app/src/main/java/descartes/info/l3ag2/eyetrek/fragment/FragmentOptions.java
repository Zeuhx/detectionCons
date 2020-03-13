package descartes.info.l3ag2.eyetrek.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.userAccount.MyRequest.MyRequest;
import descartes.info.l3ag2.eyetrek.userAccount.VolleySingleton;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Saad-Allah MAHI
 * modifications et supression du compte utilisateur
 */
public class FragmentOptions extends Fragment {
    //SharedPreferences = Cache
    private static final String PREFS = "app_prefs";
    private static final String DIDACTICIEL = "PREFERENCE";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor ;
    private MyRequest request;
    private EditText et_firstName,et_lastName,et_birthday,et_yearExp, et_email, et_password, et_passwordV;
    String deleteUrl = "http://www.ens.math-info.univ-paris5.fr/~ij00084/deleteUser.php?id=";
    RequestQueue deleteQueue,queue;


    public FragmentOptions() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        CardView delete = view.findViewById(R.id.delete);
        CardView update = view.findViewById(R.id.signup_button3);
        et_firstName = (EditText) view.findViewById(R.id.first_name3);
        et_lastName = (EditText) view.findViewById(R.id.last_name3);
        et_birthday = (EditText) view.findViewById(R.id.birthday3);
        et_yearExp = (EditText) view.findViewById(R.id.year_exp3);
        et_email = (EditText) view.findViewById(R.id.email_signup3);
        et_password = (EditText) view.findViewById(R.id.password_signup3);
        et_passwordV = (EditText) view.findViewById(R.id.password2_signup3);
        sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        deleteQueue = Volley.newRequestQueue(getContext());
        String id = sharedPreferences.getString("id", null);

        queue = VolleySingleton.getInstance(this.getContext()).getRequestQueue();
        request = new MyRequest(this.getContext(), queue);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = et_firstName.getText().toString();
                String lastName = et_lastName.getText().toString();
                String birthday = et_birthday.getText().toString();
                String yearExp = et_yearExp.getText().toString();
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                String passwordV = et_passwordV.getText().toString();


                if (firstName.length() > 0 && lastName.length() > 0 && birthday.length() > 0 && yearExp.length() > 0 && email.length() > 0 && password.length() > 0 && passwordV.length() > 0) {

                    /**
                     * execution de la method Update de la classe Myrequest qui nous permet
                     * de modifier les infos du compte utilisateur
                     */
                    request.Update(id,firstName, lastName, birthday,yearExp,email,password, passwordV, new MyRequest.UpdateCallback() {

                        @Override
                        public void onSuccess(String message) {
                            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentLog()).commit();
                        }

                        @Override
                        public void inputErrors(Map<String, String> errors) {
                            Toast.makeText(getActivity(), errors.get("email"), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String message) {
                            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();

                        }

                    });
                } else {
                    Toast.makeText(getActivity(), "veuillez remplir tous les champ", Toast.LENGTH_SHORT).show();
                }

            }

        });


        /**
         * suppression du compte utilisateur
         */
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
                dialog.setMessage("êtes-vous sûrs de vouloir supprimer votre compte ?");
                dialog.setTitle("Dialog Box");
                dialog.setPositiveButton("OUI",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                StringRequest request = new StringRequest(Request.Method.GET, deleteUrl+id, new com.android.volley.Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                    }
                                }, new com.android.volley.Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                    }
                                });
                                deleteQueue.add(request);
                                getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentLog()).commit();

                                editor=sharedPreferences.edit();
                                editor.clear().commit();

                            }
                        });
                dialog.setNegativeButton("NON",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(),"cancel",Toast.LENGTH_LONG).show();
                    }
                });
                AlertDialog alertDialog=dialog.create();
                alertDialog.show();



            }
        });

        return view;
    }



}
