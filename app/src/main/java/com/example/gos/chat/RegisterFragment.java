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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private String userName;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    EditText userEmailEt, passwordEt,usernameET;
    FloatingActionButton registerBtn;
    private DatabaseReference contactsRef,contactChatRef;
    TextView appName;
    ImageButton backBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_activity,container,false);

        userEmailEt = view.findViewById(R.id.user_email);
        passwordEt = view.findViewById(R.id.user_pwd);
        usernameET = view.findViewById(R.id.user_name);
        registerBtn = view.findViewById(R.id.next_register_btn);
        appName = view.findViewById(R.id.app_name_login);
        backBtn = (ImageButton) view.findViewById(R.id.back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).setViewpager(1);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = userEmailEt.getText().toString();
                String password = passwordEt.getText().toString();
                userName = usernameET.getText().toString();

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmailEt.getText().toString()).matches() || userEmail.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a valid Email ", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!onlyLettersSpaces(usernameET.getText().toString())|| userName.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a valid Name ", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!passwordEt.getText().toString().matches("[a-zA-Z0-9]*") || password.equals("")) {
                    Toast.makeText(getActivity(), "Please enter a valid Password ", Toast.LENGTH_LONG).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(userEmail, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    user = auth.getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName)
                                            .build();
                                    user.updateProfile(profileUpdates);

                                    contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
                                    Map<String, Object> map1 = new HashMap<String, Object>();
                                    map1.put(userName, new ContactDetails(userName, user.getEmail().toString(),
                                                    userName+".jpg"));
                                    contactsRef.updateChildren(map1);

                                    contactChatRef = FirebaseDatabase.getInstance().getReference().child("ContactChats");
                                    Map<String, Object> map2 = new HashMap<String, Object>();
                                    map2.put(userName,user.getUid());
                                    contactChatRef.updateChildren(map2);

                                    Intent intent = new Intent(getActivity(), ChatsGroupsActivity.class);
                                    startActivity(intent);
                                    ((HomeActivity)getActivity()).finish();
                                }
                                else
                                    Toast.makeText(getActivity(), "failed ", Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });

        return view;
    }

    public static boolean onlyLettersSpaces(String s){
        for(int i=0;i<s.length();i++){
            char ch = s.charAt(i);
            if (Character.isLetter(ch) || ch == ' ') {
                continue;
            }
            return false;
        }
        return true;
    }
}

