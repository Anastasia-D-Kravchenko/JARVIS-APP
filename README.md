# J.A.R.V.I.S. - AI Voice Assistant (Android) 🤖

A sleek, voice-activated virtual assistant for Android featuring a custom **White & Orange UI**. This application combines native Android speech recognition with the lightning-fast **Groq Cloud API (Llama 3.3)** to create a highly responsive and conversational AI experience.

## ✨ Key Features

* **Smart Speech Interruption:** The "ACTIVATE MIC" button doubles as an interrupt switch. If JARVIS is speaking, tapping the button will instantly mute him and start listening for your next command.
* **Real AI Integration:** General questions and conversational prompts are automatically routed to the Groq API (using the `llama-3.3-70b-versatile` model) for brilliant, witty, and contextual answers.
* **Categorized Memory System:** Tell JARVIS to "remember" something for *work*, *home*, or *shopping*. It automatically categorizes and appends these notes into dedicated internal text files, which can be recalled later.
* **Integrated Media Player:** Say *"Play"* to trigger local music playback. This activates a dynamic UI control panel that allows you to pause, play, and stop the music both visually and via voice.
* **Local System Commands:** Instantly executes hardcoded commands for checking the time and date without needing an internet connection.
* **Contextual Greetings:** JARVIS greets you dynamically based on the time of day (Morning, Afternoon, Evening, or Night) upon startup.

## 🛠️ Tech Stack
* **Language:** Java
* **Platform:** Android
* **Speech-to-Text:** Android `SpeechRecognizer`
* **Text-to-Speech:** Android `TextToSpeech` (TTS)
* **Networking:** `HttpURLConnection` (Native Java) via `ExecutorService` background threading.
* **AI Provider:** Groq Cloud (Llama 3.3 70B model).

## 🚀 Setup & Installation

To run this project on your own device or emulator, you must set up a few local resources that are excluded from the source code for security reasons.

### 1. Configure the API Key
This app uses Groq for its AI brain. You need a free API key to make it work.
1. Go to [Groq Cloud Console](https://console.groq.com/) and generate an API Key.
2. In Android Studio, navigate to `app/src/main/` and create a folder named `assets`.
3. Inside the `assets` folder, create a text file exactly named `grop_api.txt`.
4. Paste your Groq API key on the very first line of this file and save it.

### 2. Add the Music File
1. Navigate to `app/src/main/res/`.
2. Create a new Android Resource Directory named `raw`.
3. Place an MP3 file inside this folder and name it exactly `song.mp3`.

### 3. Permissions Checklist
Ensure your `AndroidManifest.xml` includes these permissions before building:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />

<img width="512" height="512" alt="freepik__make_icon_for_jarvis copy" src="https://github.com/user-attachments/assets/1ed36ae8-1b61-4422-a8eb-4cc8084bc0ce" />


