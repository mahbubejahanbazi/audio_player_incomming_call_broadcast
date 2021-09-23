package ir.mjahanbazi.audioplayerincommingcallbroadcast;

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
