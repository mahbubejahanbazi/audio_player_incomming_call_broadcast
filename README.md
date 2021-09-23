# Stop Playing Audio When Incoming Call Is Received
## audio player

Description of the application
A simple audio player that have the base player keys(play / pause and progress bar), in addition the project implemented some specific features.
- A basic audio player
- BroadcastReceiver is used in the applications to detect incoming calls.
- The Applications also request for permissions to read phone state.

 

## Tech Stack

Java

<p align="center">
  <img src="https://github.com/mahbubejahanbazi/audio_player_incomming_call_broadcast/blob/main/images/request_permission.jpg" />
</p>

<p align="center">
  <img src="https://github.com/mahbubejahanbazi/audio_player_incomming_call_broadcast/blob/main/images/default.jpg" />
</p>

<p align="center">
  <img src="https://github.com/mahbubejahanbazi/audio_player_incomming_call_broadcast/blob/main/images/playing.jpg" />
</p>

## Source code
#### Register permission for reading phone state
AndroidManifest.xml
```xml
 <?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="ir.mjahanbazi.audioplayerincommingcallbroadcast">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/Theme.AudioPlayerIncommingCallBroadcast">
        <activity
            android:name="ir.mjahanbazi.audioplayerincommingcallbroadcast.MainActivity"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
</manifest>
```
AudioPlayer.java
```java
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class AudioPlayer extends androidx.constraintlayout.widget.ConstraintLayout {
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private TextView fileName;
    private TextView fileTime;
    private SeekBar seekBar;
    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
            refreshGui();
        }
    };
    private boolean playing = false;
    private boolean werePlaying = false;
    private Runnable UpdateAudioTime = new Runnable() {
        public void run() {
            while (true) {
                fileTime.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshGui();
                    }
                }, 100);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
                if (!playing) {
                    break;
                }
            }
        }
    };
    private OnClickListener onClickListenerPauseButton = new OnClickListener() {
        public void onClick(View v) {
            werePlaying = false;
            pauseAudio();
        }
    };
    private OnClickListener onClickListenerPlayerButton = new OnClickListener() {
        public void onClick(View arg0) {
            playAudio();
        }
    };
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            playButton.setVisibility(VISIBLE);
            pauseButton.setVisibility(GONE);
            mediaPlayer.seekTo(0);
            seekBar.setProgress(0);
            mediaPlayer.pause();
        }
    };

    public AudioPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }


    public AudioPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void pauseAudio() {
        playing = false;
        pauseButton.setVisibility(INVISIBLE);
        playButton.setVisibility(VISIBLE);
        mediaPlayer.pause();
    }

    public void playAudio() {
        playing = true;
        new Thread(UpdateAudioTime).start();
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        mediaPlayer.start();
        pauseButton.setVisibility(VISIBLE);
        playButton.setVisibility(INVISIBLE);
        refreshGui();
    }

    private void init(Context context) {
        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflator.inflate(R.layout.file_audio, this);
        playButton = (ImageButton) findViewById(R.id.file_audio_play);
        pauseButton = (ImageButton) findViewById(R.id.file_audio_pause);
        fileName = (TextView) findViewById(R.id.file_audio_name);
        seekBar = (SeekBar) findViewById(R.id.file_audio_seekBar);
        fileTime = (TextView) findViewById(R.id.file_audio_time);
        playButton.setOnClickListener(onClickListenerPlayerButton);
        pauseButton.setOnClickListener(onClickListenerPauseButton);
    }


    private String getTimeStr(int time) {
        String minutes = "";
        String seconds = "";
        final long sec = TimeUnit.MILLISECONDS.toSeconds((long) time)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) time));
        if (sec < 10) {
            seconds = "0" + sec;
        } else {
            seconds = sec + "";
        }
        minutes = "" + TimeUnit.MILLISECONDS.toMinutes((long) time);
        return String.format("%s:%s", minutes, seconds);
    }


    private void refreshGui() {
        if (mediaPlayer == null) {
            return;
        }
        int length = mediaPlayer.getDuration();
        int current = mediaPlayer.getCurrentPosition();
        fileTime.setText(String.format("%s / %s",
                getTimeStr(current),
                getTimeStr(length)));
        seekBar.setProgress((int) current);
    }

    public void setAudio(int resid) {
        fileName.setText(getResources().getResourceEntryName(resid));
        mediaPlayer = MediaPlayer.create(getContext(), resid);
        intiMedia();
    }

    public void setAudio(Uri uri) {
        fileName.setText(getFileName(uri));
        mediaPlayer = MediaPlayer.create(getContext(), uri);
        intiMedia();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void intiMedia() {
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        fileTime.setText(String.format("0:00 / %s", getTimeStr(mediaPlayer.getDuration())));
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isWerePlaying() {
        return werePlaying;
    }

    public void setWerePlaying(boolean werePlaying) {
        this.werePlaying = werePlaying;
    }

}
```
CallReceiver.java
```java
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity activity = (MainActivity) context;
        String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE) &&
                activity.audioPlayer.isWerePlaying()) {
            activity.audioPlayer.playAudio();
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) &&
                activity.audioPlayer.isPlaying()) {
            activity.audioPlayer.setWerePlaying(true);
            activity.audioPlayer.pauseAudio();
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING) &&
                activity.audioPlayer.isPlaying()) {
            activity.audioPlayer.setWerePlaying(true);
            activity.audioPlayer.pauseAudio();
        }
    }
}
```
MainActivity.java
```java
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    public AudioPlayer audioPlayer;
    private final int REQUEST_CODE_READ_PHONE_STATE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioPlayer = findViewById(R.id.activity_main_audio_player);
        audioPlayer.setAudio(R.raw.audio_file_example);
        if (this.checkCallingPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_CODE_READ_PHONE_STATE_PERMISSIONS);
        }
        IntentFilter intent = new IntentFilter();
        intent.addAction("android.intent.action.PHONE_STATE");
        CallReceiver call = new CallReceiver();
        registerReceiver(call, intent);
    }
}
```
## Contact

mjahanbazi@protonmail.com