package ir.mjahanbazi.audioplayerincommingcallbroadcast;

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
