package descartes.info.l3ag2.eyetrek.userAccount;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import java.util.Map;

import descartes.info.l3ag2.eyetrek.R;
import descartes.info.l3ag2.eyetrek.fragment.FragmentLog;
import descartes.info.l3ag2.eyetrek.fragment.Profil;
import descartes.info.l3ag2.eyetrek.userAccount.MyRequest.MyRequest;

/**
 * Created by Saad-Allah MAHI
 * ce fragement nous permet de se connecter
 */
public class SignIn extends Fragment {
    private EditText et_email,et_password;
    private RequestQueue queue;
    private MyRequest request;
    private CardView btn_login;
    private Handler handler;
    private SessionManager sessionManager ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signin2, container, false);

        btn_login = (CardView) view.findViewById(R.id.signin_button7);
        et_email = (EditText) view.findViewById(R.id.email7);
        et_password = (EditText) view.findViewById(R.id.password7);

        queue = VolleySingleton.getInstance(this.getContext()).getRequestQueue();
        request = new MyRequest(this.getContext(), queue);
        handler = new Handler();
        sessionManager = new SessionManager(this.getContext());

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if (email.length() > 0 && password.length() > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            request.connection(email, password, new MyRequest.LoginCallback() {

                                @Override
                                public void onSuccess(String id, String email) {
                                    sessionManager.insertUser(id,email);
                                    getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new Profil()).commit();
                                   //
                                }

                                @Override
                                public void onError(String message) {
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }, 100);
                } else {
                    Toast.makeText(getActivity(), "veuillez remplir les champs", Toast.LENGTH_SHORT).show();

                }


            }
        });

        return view ;
    }

}




