package com.example.gos.chat;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ContactsListActivity extends AppCompatActivity {

    private FirebaseListAdapter<ContactDetails> adapter;
    private DatabaseReference contactRef,groupContactsRef,contactChatsRef,groupRef,dbRef;
    private FirebaseDatabase database;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    private ArrayList<ContactDetails> dynamContactList = new ArrayList<>();
    ListView listView;
    private int groupCount=0;
    private FloatingActionButton createGroupBtn;
    private ImageView checkedImage;
    private EditText searchText;
    private RecycleAdapter recycleAdapter;
    private String picLocation="";
    private RecyclerView recyclerView;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference imagesRef ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_list);

        listView=findViewById(R.id.listView);
        recyclerView = findViewById(R.id.listViewdynam);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        createGroupBtn = findViewById(R.id.crate_group_btn);
        searchText = findViewById(R.id.search_text);
        searchText.setVisibility(View.INVISIBLE);

        user = auth.getCurrentUser();

        recycleAdapter = new RecycleAdapter(dynamContactList);
        recyclerView.setAdapter(recycleAdapter);

        database = FirebaseDatabase.getInstance();

        displayContactsList();

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listView = findViewById(R.id.listView);
                Query query = dbRef.orderByChild("userName").startAt(searchText.getText().toString())
                        .endAt(searchText.getText().toString()+"\uf8ff");
                adapter = new FirebaseListAdapter<ContactDetails>(ContactsListActivity.this, ContactDetails.class,
                        R.layout.contact_layout, query) {
                    @Override
                    protected void populateView(View v, ContactDetails model, int position) {
                        // Get references to the views of contact_layout.xml
                        TextView userName = (TextView)v.findViewById(R.id.contact_name);
                        TextView userMail = (TextView)v.findViewById(R.id.contact_email);
                        final ImageView userImage=(ImageView)v.findViewById(R.id.contact_image);

                        userMail.setText(model.getUserEmail().toString());
                        userName.setText(""+'\n'+model.getUserName()+" "+'\n');

                        loadImageFromStorage(picLocation,model.getUserName(),userImage);
                    }
                };
                listView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView name = view.findViewById(R.id.contact_name);
                TextView email = view.findViewById(R.id.contact_email);
                ImageView image = view.findViewById(R.id.contact_image);
                checkedImage = view.findViewById(R.id.checked_image);
                boolean existFlag=true;

                String strname = name.getText().toString();
                strname = strname.substring(1,strname.length()-2);

                String stremail = email.getText().toString();

                for(int j =0;j<recyclerView.getChildCount();j++) {
                    View tempView = recyclerView.getChildAt(j);
                    TextView textview = tempView.findViewById(R.id.contact_email);
                    String tmpname = textview.getText().toString();
                    if(!tmpname.equals(stremail))
                    {
                        continue;
                    }
                    else {
                        existFlag=false;
                        break;
                    }
                }
                if (existFlag) {
                    dynamContactList.add(new ContactDetails(strname, stremail,image));
                    recycleAdapter.notifyDataSetChanged();
                }

                if(checkedImage.getVisibility() == View.INVISIBLE) {
                    checkedImage.setVisibility(View.VISIBLE);
                }
                else {
                    checkedImage.setVisibility(View.INVISIBLE);
                    for(int i =0;i<recyclerView.getChildCount();i++){
                        View dynamview = recyclerView.getChildAt(i);
                        TextView textview = dynamview.findViewById(R.id.contact_email);
                        String dynammail = textview.getText().toString();
                        if(stremail.equals(dynammail)){
                            dynamContactList.remove(i);
                            recycleAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactsListActivity.this);
                View dialogView  = getLayoutInflater().inflate(R.layout.dialog_layout,null);
                final EditText groupName = dialogView.findViewById(R.id.group_name);
                builder.setView(dialogView).setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        groupRef = database.getReference().child("Groups");

                        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                groupCount = (int)dataSnapshot.getChildrenCount();
                                groupCount++;

                                final String groupNameStr = groupName.getText().toString();
                                Map<String,Object> map = new HashMap<String, Object>();
                                map.put(""+groupCount,groupNameStr);
                                groupRef.updateChildren(map);

                                contactRef = database.getReference().child("Contacts");
                                contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterator childrenListEmail = dataSnapshot.getChildren().iterator();
                                        Iterator childrenListUid = dataSnapshot.getChildren().iterator();

                                        while (childrenListEmail.hasNext()) {
                                            String refEmail = ((DataSnapshot) childrenListEmail.next()).child("userEmail").getValue().toString();
                                            String refUid = ((DataSnapshot) childrenListUid.next()).child("userUid").getValue().toString();

                                            int recycleCount = recyclerView.getChildCount();
                                            int dynamCOunt = dynamContactList.size();
                                            for (int j = 0; j < dynamCOunt; j++) {
                                                String userEmail = dynamContactList.get(j).getUserEmail();

                                                if (userEmail.equals(refEmail)){

                                                    contactChatsRef = database.getReference().child("ContactChats").child(refUid);
                                                    Map<String, Object> map1 = new HashMap<String, Object>();
                                                    map1.put("" + groupCount, groupNameStr);
                                                    contactChatsRef.updateChildren(map1);

                                                    groupContactsRef = database.getReference().child("GroupContacts").child("" + groupCount).child(groupNameStr);
                                                    Map<String, Object> map2 = new HashMap<String, Object>();
                                                    map2.put(refUid, userEmail);
                                                    groupContactsRef.updateChildren(map2);

                                                    break;
                                                }
                                            }
                                        }

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.putExtra("group_name", groupNameStr);
                                        intent.putExtra("group_count", groupCount);
                                        startActivity(intent);

                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }).create().show();
            }
        });
    }

    private void displayContactsList() {
        dbRef = database.getReference().child("Contacts");

        adapter = new FirebaseListAdapter<ContactDetails>(this, ContactDetails.class,
                R.layout.contact_layout, dbRef) {
            @Override
            protected void populateView(View v, final ContactDetails model, int position) {
                // Get references to the views of contact_layout.xml
                TextView userName = (TextView)v.findViewById(R.id.contact_name);
                TextView userMail = (TextView)v.findViewById(R.id.contact_email);
                final ImageView userImage=(ImageView)v.findViewById(R.id.contact_image);

                userMail.setText(model.getUserEmail().toString());
                userName.setText(""+'\n'+model.getUserName()+" "+'\n');

                imagesRef = storageRef.child("images").child(model.getUserName()+".jpg");
                final long ONE_MEGABYTE = 1024 * 1024;

                imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        userImage.setImageBitmap(bmp);
                        picLocation = saveToInternalStorage(bmp,model.getUserName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        userImage.setImageResource(R.drawable.blank_user_image);
                    }
                });
            }
        };

        listView.setAdapter(adapter);
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,name+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private void loadImageFromStorage(String path,String name,ImageView img)
    {
        try {
            File f=new File(path, name+".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            img.setImageResource(R.drawable.blank_user_image);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search: {
                if(searchText.getVisibility() == View.INVISIBLE) {
                    searchText.setVisibility(View.VISIBLE);
                }
                else {
                    searchText.setVisibility(View.INVISIBLE);
                }
            }
            break;
            case R.id.action_settings:
            {
                Intent intent = new Intent(ContactsListActivity.this,SettingsActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_logout:
            {
                Intent intent = new Intent(ContactsListActivity.this,HomeActivity.class);
                startActivity(intent);
                auth.signOut();
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyViewHolder> {

        private ArrayList<ContactDetails> contactList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView name, email;
            public ImageView delete,image;

            public MyViewHolder(final View view) {
                super(view);
                name = (TextView) view.findViewById(R.id.contact_name);
                email = (TextView) view.findViewById(R.id.contact_email);
                image = (ImageView) view.findViewById(R.id.contact_image);
                delete = (ImageView) view.findViewById(R.id.delete_view);

                loadImageFromStorage(picLocation, name.getText().toString(), image);
            }
        }

        public RecycleAdapter(ArrayList<ContactDetails> contactList) {
            this.contactList = contactList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contact_dynam_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final ContactDetails contact = contactList.get(position);
            holder.name.setText(contact.getUserName());
            holder.email.setText(contact.getUserEmail());

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dynamContactList.remove(position);
                    recycleAdapter.notifyDataSetChanged();
                    for(int i =0;i<listView.getChildCount();i++){
                        View view = listView.getChildAt(i);
                        TextView textview = view.findViewById(R.id.contact_email);
                        String strname = textview.getText().toString();
                        if(strname.equals(contact.getUserEmail())){
                            checkedImage = view.findViewById(R.id.checked_image);
                            checkedImage.setVisibility(View.INVISIBLE);
                            break;
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }
    }
}