package com.example.gos.chat;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentHome extends android.support.v4.app.Fragment {
    private static final String TAG = "homePageFragement";

    private Button LoginBtn;
    private Button CreateAccountBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_layout,container,false);
        LoginBtn = (Button)view.findViewById(R.id.login_btn);
        CreateAccountBtn = (Button)view.findViewById(R.id.register_btn);

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).setViewpager(0);
            }
        });

        CreateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).setViewpager(2);
            }
        });

        return view;
    }
}
