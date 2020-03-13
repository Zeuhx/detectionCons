package descartes.info.l3ag2.eyetrek.userAccount;


import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import descartes.info.l3ag2.eyetrek.fragment.FragmentSignIn;
import descartes.info.l3ag2.eyetrek.userAccount.MyRequest.MyRequest;

/**
 * Created by Saad-Allah MAHI
 * ce fragement nous permet de s'inscrire
 */
public class SignUp extends Fragment {

    private CardView btn_register;
    private RequestQueue queue;
    private MyRequest request;
    private EditText et_firstName,et_lastName,et_birthday,et_yearExp, et_email, et_password, et_passwordV;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup2, container, false);

        btn_register = (CardView) view.findViewById(R.id.signup_button2);
        et_firstName = (EditText) view.findViewById(R.id.first_name2);
        et_lastName = (EditText) view.findViewById(R.id.last_name2);
        et_birthday = (EditText) view.findViewById(R.id.birthday2);
        et_yearExp = (EditText) view.findViewById(R.id.year_exp2);
        et_email = (EditText) view.findViewById(R.id.email_signup2);
        et_password = (EditText) view.findViewById(R.id.password_signup2);
        et_passwordV = (EditText) view.findViewById(R.id.password2_signup2);
        queue = VolleySingleton.getInstance(this.getContext()).getRequestQueue();
        request = new MyRequest(this.getContext(), queue);

        btn_register.setOnClickListener(new View.OnClickListener() {
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


                    request.Register(firstName, lastName, birthday,yearExp,email,password, passwordV, new MyRequest.RegisterCallback() {

                        @Override
                        public void onSuccess(String message) {
                            getFragmentManager().beginTransaction().add(R.id.contenu_fragment, new FragmentLog()).commit();
                        }

                        @Override
                        public void inputErrors(Map<String, String> errors) {
                            System.out.println(errors);

                          if(errors.containsKey("lastname")){
                              Toast.makeText(getActivity(), errors.get("lastname"), Toast.LENGTH_SHORT).show();
                          }
                            if(errors.containsKey("firstname")){
                                Toast.makeText(getActivity(), errors.get("firstname"), Toast.LENGTH_SHORT).show();
                            }
                            if(errors.containsKey("birthday")){
                                Toast.makeText(getActivity(), errors.get("birthday"), Toast.LENGTH_SHORT).show();
                            }
                            if(errors.containsKey("email")){
                                Toast.makeText(getActivity(), errors.get("email"), Toast.LENGTH_SHORT).show();
                            }
                            if(errors.containsKey("yearexp")){
                                Toast.makeText(getActivity(), errors.get("yearexp"), Toast.LENGTH_SHORT).show();
                            }
                            if(errors.containsKey("password")){
                                Toast.makeText(getActivity(), errors.get("password"), Toast.LENGTH_SHORT).show();
                            }


                          /*
                            Toast.makeText(getActivity(), errors.get("lastname"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), errors.get("firstname"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), errors.get("birthday"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), errors.get("yearexp"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), errors.get("password"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(getActivity(), errors.get("email"), Toast.LENGTH_SHORT).show();

*/

                        }
                        @Override
                        public void onError(String message) {

                            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();

                        }

                    });
                } else {
                    Toast.makeText(getActivity(), "veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                }

            }

        });

        return view ;
    }

}