package com.example.gos.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    EditText userEmailEt, passwordEt;
    FloatingActionButton loginBtn;
    TextView appName;
    ImageButton backBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.login_activity,container,false);

        userEmailEt = (EditText) view.findViewById(R.id.user_email);
        passwordEt = (EditText) view.findViewById(R.id.user_pwd);
        loginBtn = (FloatingActionButton) view.findViewById(R.id.login_button);
        appName = (TextView) view.findViewById(R.id.app_name_login);
        backBtn = (ImageButton) view.findViewById(R.id.back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).setViewpager(1);
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userEmail = userEmailEt.getText().toString();
                    String password = passwordEt.getText().toString();

                    if(!userEmail.equals("") && !password.equals("")) {
                        auth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getActivity(), ChatsGroupsActivity.class);
                                    startActivity(intent);
                                    ((HomeActivity)getActivity()).finish();
                                } else {
                                    Toast.makeText(getActivity(), "Wrong details or user not Exist", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(getActivity(), "Please fill all details", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            Intent intent = new Intent(getActivity(), ChatsGroupsActivity.class);
            startActivity(intent);
            ((HomeActivity)getActivity()).finish();
        }

        return view;
    }
}
