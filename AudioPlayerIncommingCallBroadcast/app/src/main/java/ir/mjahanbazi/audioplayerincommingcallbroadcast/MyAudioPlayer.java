package ir.mjahanbazi.audioplayerincommingcallbroadcast;

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

public class MyAudioPlayer extends androidx.constraintlayout.widget.ConstraintLayout {
    private MediaPlayer mediaPlayer;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private TextView fileName;
    private TextView fileTime;
    private SeekBar seekBar;
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
            werePlaying=false;
            pauseAudio();
        }
    };

    public void pauseAudio() {
        playing = false;
        pauseButton.setVisibility(INVISIBLE);
        playButton.setVisibility(VISIBLE);
        mediaPlayer.pause();
    }

    private OnClickListener onClickListenerPlayerButton = new OnClickListener() {
        public void onClick(View arg0) {
            playAudio();
        }
    };


    public void playAudio() {
        playing = true;
        new Thread(UpdateAudioTime).start();
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        mediaPlayer.start();
        pauseButton.setVisibility(VISIBLE);
        playButton.setVisibility(INVISIBLE);
        refreshGui();
    }

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


    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            playButton.setVisibility(VISIBLE);
            pauseButton.setVisibility(GONE);
            mediaPlayer.seekTo(0);
            seekBar.setProgress(0);
            mediaPlayer.pause();
        }
    };

    public MyAudioPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MyAudioPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
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
