package com.example.speach2text;

import static com.example.speach2text.Function.wishMe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {
    private static String GROQ_API_KEY = "";
    private SpeechRecognizer recognizer;
    private TextView textUserSpeech;
    private TextView textJarvisResponse;
    private LinearLayout musicPanel;
    private TextToSpeech tts;
    private MediaPlayer player;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler(android.os.Looper.getMainLooper());
        executorService = java.util.concurrent.Executors.newSingleThreadExecutor();

        findById();
        initSpeechRecognizer();
        initTextToSpeech();
        readApiKeyFromFile();
    }

    private void readApiKeyFromFile() {
        try {
            InputStream is = getAssets().open("grop_api.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            GROQ_API_KEY = br.readLine();
            br.close();
        } catch (IOException e) {
            Log.e("JARVIS_AI", "Could not read API key from assets", e);
        }
    }

    private void initTextToSpeech() {
        tts = new TextToSpeech(this, i -> {
            if (tts.getEngines().isEmpty()) {
                Toast.makeText(MainActivity.this, "Engine is not Available", Toast.LENGTH_SHORT).show();
            } else {
                String s = wishMe();
                speak(s + ", I'm JARVIS AI mark one");
            }
        });
    }

    private void speak(String msg) {
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void findById() {
        textUserSpeech = findViewById(R.id.textUserSpeech);
        textJarvisResponse = findViewById(R.id.textJarvisResponse);
        musicPanel = findViewById(R.id.musicPanel);
    }

    private void initSpeechRecognizer() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { textUserSpeech.setText("Listening..."); }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() { textUserSpeech.setText("Processing..."); }
                @Override public void onError(int error) { textUserSpeech.setText("Error occurred. Try again."); }
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (data != null && !data.isEmpty()) {
                        String recognizedText = data.get(0);
                        textUserSpeech.setText(recognizedText);
                        response(recognizedText);
                    }
                }
            });
        }
    }

    private void updateJarvisUI(String message) {
        textJarvisResponse.setText(message);
        speak(message);
    }

    public void startRecording(View view) {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        if (recognizer != null) {
            recognizer.startListening(intent);
        }
    }

    private void askRealAI(String userPrompt) {
        executorService.execute(() -> {
            try {
                URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("model", "llama-3.3-70b-versatile");

                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("role", "user");
                message.put("content", "You are JARVIS, a helpful and witty AI. Keep responses brief. User asks: " + userPrompt);
                messages.put(message);
                payload.put("messages", messages);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = br.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                br.close();

                JSONObject jsonResponse = new JSONObject(responseStrBuilder.toString());
                String aiTextResponse = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                mainHandler.post(() -> updateJarvisUI(aiTextResponse));

            } catch (Exception e) {
                Log.e("JARVIS_AI", "Error calling AI", e);
                mainHandler.post(() -> updateJarvisUI("I'm sorry sir, I'm having trouble connecting to the cloud."));
            }
        });
    }

    private void response(String msg) {
        String msgs = msg.toLowerCase(Locale.ROOT);

        if (msgs.contains("hi")) {
            updateJarvisUI("Hello Sir, Jarvis at your service. Please tell me how can I help you?");
            return;
        }

        if (msgs.contains("time")) {
            Date date = new Date();
            String time = DateUtils.formatDateTime(this, date.getTime(), DateUtils.FORMAT_SHOW_TIME);
            updateJarvisUI("The current time is " + time);
            return;
        }

        if (msgs.contains("date")) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dt = new SimpleDateFormat("dd MMMM yyyy");
            Calendar cal = Calendar.getInstance();
            String todays_Date = dt.format(cal.getTime());
            updateJarvisUI("The date today is " + todays_Date);
            return;
        }

        // --- SMART CATEGORIZED REMINDERS ---
        if (msgs.contains("remember")) {
            String category = "general";
            if (msgs.contains("work")) category = "work";
            else if (msgs.contains("home")) category = "home";
            else if (msgs.contains("shopping")) category = "shopping";

            String note = msgs.replace("jarvis remember that", "").replace("remember", "").trim();
            writeToFile(category + ".txt", "- " + note);
            updateJarvisUI("Memory updated. I have added that to your " + category + " file.");
            return;
        }

        if (msgs.contains("know") || (msgs.contains("what") && msgs.contains("reminders"))) {
            String category = "general";
            if (msgs.contains("work")) category = "work";
            else if (msgs.contains("home")) category = "home";
            else if (msgs.contains("shopping")) category = "shopping";

            String data = readFromFile(category + ".txt");
            if (data.trim().isEmpty()) {
                updateJarvisUI("Sir, your " + category + " file is currently empty.");
            } else {
                updateJarvisUI("Here is your " + category + " list:\n" + data);
            }
            return;
        }

        if (msgs.contains("play")) {
            updateJarvisUI("Initiating audio playback.");
            play();
            return;
        }

        if (msgs.contains("pause")) {
            updateJarvisUI("Audio paused.");
            pause();
            return;
        }

        if (msgs.contains("stop")) {
            updateJarvisUI("Audio system offline.");
            stop();
            return;
        }

        updateJarvisUI("Processing query through primary AI servers...");
        askRealAI(msg);
    }

    public void toggleMusic(View view) {
        if (player != null) {
            if (player.isPlaying()) pause();
            else player.start();
        }
    }

    public void stopMusicCommand(View view) {
        stop();
    }

    private void play() {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.song);
            player.setOnCompletionListener(mediaPlayer -> stopPlayer());
        }
        player.start();
        musicPanel.setVisibility(View.VISIBLE);
    }

    private void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    private void stop() {
        stopPlayer();
    }

    private void stopPlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        musicPanel.setVisibility(View.GONE);
    }

    private void writeToFile(String filename, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(filename, Context.MODE_APPEND));
            outputStreamWriter.write(data + "\n");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString).append("\n");
                }
                inputStream.close();
            }
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            Log.e("Exception", "Can not read file: " + e.toString());
        }
        return stringBuilder.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSpeechRecognizer();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) recognizer.destroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        stopPlayer();
    }
}