package com.example.speech_text;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private EditText editText;
    private ImageView micButton;
    private Button annotate;
    private TextView userName;
    private TextView notify;
    private Boolean isPressed;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        requestQueue = Volley.newRequestQueue(this);
        userName = findViewById(R.id.user);
        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        annotate = findViewById(R.id.annotate);
        notify = findViewById(R.id.notify);
        isPressed = false;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        userName.setText("User: "+LoginActivity.username);
        final MediaPlayer mic_start = MediaPlayer.create(this, R.raw.discord_sounds);
        final MediaPlayer mic_stop = MediaPlayer.create(this, R.raw.discord_leave);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        annotate.setEnabled(false);
        notify.setVisibility(View.INVISIBLE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if the EditText is empty or not
                boolean isEditTextEmpty = s.toString().trim().isEmpty();

                // Enable or disable the button based on the EditText's text length
                annotate.setEnabled(!isEditTextEmpty);
                if(isEditTextEmpty == true){
                    annotate.setBackgroundColor(Color.parseColor("#CE93D8"));
                }
                else {
                    annotate.setBackgroundColor(Color.parseColor("#9C27B0"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                micButton.setImageResource(R.drawable.ic_mic);
                mic_stop.start();
                Log.e("MicStop","Mic Stop");
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                editText.setText(data.get(0));
                isPressed = false;
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    isPressed = !isPressed;
                    if(isPressed == true){
                        micButton.setImageResource(R.drawable.ic_mic_on);
                        speechRecognizer.startListening(speechRecognizerIntent);
                        mic_start.start();
                        Log.e("StartListening", "Start Listening");
                    }
                    else {
                        speechRecognizer.stopListening();
                        editText.setText("");
                        editText.setHint("Tap to Speak");
                        micButton.setImageResource(R.drawable.ic_mic);
                        mic_stop.start();
                    }
                }
                return false;
            }
        });

        annotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendJsonValue();
            }
        });


    }

    public void setNotify(String color_val, String context) {
        notify.setVisibility(View.VISIBLE);
        notify.setText(context);
        notify.setBackgroundColor(Color.parseColor(color_val));
        notify.postDelayed(new Runnable() {
            @Override
            public void run() {
                notify.setVisibility(View.INVISIBLE);
            }
        }, 5000);
    }

    private void sendJsonValue() {
        String url = "https://10.5.20.174:3000/label";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = dateFormat.format(new Date());
        String jsonString = String.format("{\"customer\":\"%s\", \"ts\":\"%s\", \"label\":\"%s\"}", LoginActivity.username, currentDateAndTime, editText.getText());
        HttpsTrustManager.allowAllSSL();
        try {
            JSONObject jsonBody = new JSONObject(jsonString);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle successful response
                            Log.d("Response", response.toString());
                            editText.setText("");
                            editText.setHint("Tap to Speak");
                            setNotify("#009933", response.toString());
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error response
                            Log.e("Error", error.toString());
                            setNotify("#cc0000", error.toString());

                        }
                    });

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("NetworkError","Internet Connectivity Issue");
            setNotify("#e6b800", "Internet Connectivity Issue");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }
}