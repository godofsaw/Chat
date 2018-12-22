package com.example.gos.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pl.droidsonroids.gif.GifTextView;

/**
 * Created by Gos on 6/4/2018.
 */

public class ChatsGroupsActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_rooms = new ArrayList<>();
    private String user_name="";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    private DatabaseReference contactChatsRef ;
    private FirebaseDatabase database;
    private FloatingActionButton add_group_btn;
    GifTextView Gif;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list_layout);
        listView = findViewById(R.id.listView);
        add_group_btn = findViewById(R.id.fab_group_btn);
        Gif=findViewById(R.id.spinner_gif);

        user = auth.getCurrentUser();
        user_name=user.getDisplayName();
        database = FirebaseDatabase.getInstance();
        contactChatsRef = database.getReference().child("ContactChats").child(""+user_name);

        Gif.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                Gif.setVisibility(View.INVISIBLE);
            }
        }, 1300);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list_of_rooms);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        //adding group
        add_group_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(),ContactsListActivity.class);
                startActivity(intent1);
            }
        });

        contactChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext())
                {
                    set.add(((DataSnapshot)i.next()).getValue().toString());
                }
                list_of_rooms.clear();
                list_of_rooms.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String groupName = ((TextView)view).getText().toString();

                contactChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Iterator childrenList = dataSnapshot.getChildren().iterator();
                        while(childrenList.hasNext()){
                            DatabaseReference keyGroupRef = ((DataSnapshot)childrenList.next()).getRef();
                            keyGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int groupCount=0;
                                    if(groupName.equals(dataSnapshot.getValue().toString())){
                                        groupCount = Integer.parseInt(dataSnapshot.getKey());
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        intent.putExtra("group_name",groupName);
                                        intent.putExtra("group_count",groupCount);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case R.id.action_settings: {
                Intent intent = new Intent(ChatsGroupsActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.action_logout:
            {
                Intent intent = new Intent(ChatsGroupsActivity.this,HomeActivity.class);
                startActivity(intent);
                auth.signOut();
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}