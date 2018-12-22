package com.example.gos.chat;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener{

    private FirebaseDatabase database;
    private DatabaseReference dbRef,groupContactsRef;
    AudioManager audio;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private ToggleButton toggleButton;
    private FirebaseListAdapter<ChatMessage> adapter;
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private String groupName, currUserName;
    TextView viewRecord, txtMsg,dateView;
    private int viewPartMsgLimitChars = 50,groupCount;
    private int countParticalMsgToView = 0;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    ArrayList<String> namesList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewRecord = (TextView) findViewById(R.id.view_record);
        txtMsg = (TextView) findViewById(R.id.message_text);
        dateView = findViewById(R.id.date_view);
        toggleButton = findViewById(R.id.toggleButton1);

        user = auth.getCurrentUser();
        groupName = getIntent().getExtras().get("group_name").toString();
        groupCount = getIntent().getIntExtra("group_count",0);
        currUserName = user.getDisplayName();

        database = FirebaseDatabase.getInstance();
        groupContactsRef = database.getReference().child("GroupContacts").child(""+groupCount).child(groupName);
        dbRef = database.getReference().child("Groups").child(""+groupCount).child(groupName);
        setTitle(""+groupName);

        groupContactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String mission = snapshot.getKey();
                        namesList.add(mission);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        displayChatMessages();

        audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0,  AudioManager.ADJUST_MUTE);

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    ActivityCompat.requestPermissions
                            (MainActivity.this,
                                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                                    REQUEST_RECORD_PERMISSION);
                }
                else {
                    pushAndDestroy();

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        menu.add(Menu.NONE, 0, Menu.NONE, "View RSS").setIcon(R.drawable.picgif)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, 0, Menu.NONE, "View ddf").setIcon(R.drawable.picgif)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    public void pushAndDestroy(){
        if(!viewRecord.getText().equals("")) {
            dbRef.push().setValue(new ChatMessage("" + viewRecord.getText().toString(),
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
            viewRecord.setText("");
        }
        speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        speech.setRecognitionListener(MainActivity.this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        viewRecord.setText("");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        dbRef.push().setValue(new ChatMessage(""+matches.get(0).toString(),
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
        );
        if (toggleButton.isChecked()){
            speech.startListening(recognizerIntent);
        }
        else {
            speech.stopListening();
        }
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        if (!toggleButton.isChecked()){
            speech.stopListening();
        }
    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message_layout, dbRef) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                String userName = model.getMessageUser().toString();

                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageUser.setTextColor(getResources().getColor(R.color.my_user));
                messageUser.setText(model.getMessageUser());
                messageText.setText(""+'\n'+model.getMessageText()+" "+'\n');
                // Format the date before showing it
                messageTime.setText(DateFormat.format("HH:mm", model.getMessageTime()));

                dateView.setText(DateFormat.format("dd/MM/yyyy", model.getMessageTime()));

                if(!userName.equals(currUserName)){
                    messageUser.setTextColor(getResources().getColor(R.color.not_my_user));
                }
            }
        };

        listOfMessages.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }
    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            //speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        //toggleButton.setChecked(false);
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle results)
    {
        Log.i(LOG_TAG, "onPartialResults");
        ArrayList<String> partResult = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        viewRecord.setText(""+partResult.get(0).toString());

        if (countParticalMsgToView + partResult.get(0).toString().length() < viewPartMsgLimitChars)
        {
            countParticalMsgToView += partResult.get(0).toString().length();
            viewRecord.setText(""+partResult.get(0).toString());
        }
        else{
            viewRecord.setText("");
            countParticalMsgToView = partResult.get(0).toString().length();
            viewRecord.setText(partResult.get(0).toString());
        }
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}