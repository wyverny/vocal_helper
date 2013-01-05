package com.lcm.vocal;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Speaker implements Runnable {
	public static final int SPEAK = 0;
	protected static final String TAG = "Speaker";
//	private final Object mutex = new Object();
	private volatile boolean isRunning;
	private Context mContext;
	private AudioTrack audioTrack;
	private boolean headsetOn = false;
	private AudioHandler mHandler;

	public Speaker(Context context, boolean isRunning, boolean headsetOn) {
		this.isRunning = isRunning;
		this.mContext = context;
		this.headsetOn = headsetOn;
		
		int bufferSize = AudioRecord.getMinBufferSize(VocalMain.RECORDER_SAMPLERATE,
				VocalMain.RECORDER_CHANNELS, VocalMain.RECORDER_AUDIO_ENCODING);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,  
				VocalMain.RECORDER_SAMPLERATE,  
                VocalMain.RECORDER_CHANNELS, 
                VocalMain.RECORDER_AUDIO_ENCODING,  
                bufferSize,  
                AudioTrack.MODE_STREAM); 
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
		Log.e(TAG,"Speaker isRunning is now..." + this.isRunning);
	}

	public Handler getmHandler() {
		return mHandler;
	}

	public void setHeadsetOn(boolean headsetOn) {
		this.headsetOn = headsetOn;
	}

	@Override
	public void run() {
		Log.e(TAG,"Speaker starts..."); 
		Looper.prepare();
		mHandler = new AudioHandler();
		Looper.loop();
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
		try {
//			audioTrack.play();
			while(isRunning) {
				Log.e(TAG,"Speaker out of while isRunning...11");
				Thread.sleep(250);
			}
			Log.e(TAG,"Speaker out of while isRunning...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Log.e(TAG,"close process in speaker");
			audioTrack.stop();
		}
		Log.e(TAG,"Speaker stops...");
	}
	
	class AudioHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==SPEAK) {
				// if wiredHeadset is not on
//				if(audioManager.isWiredHeadsetOn()) {
//					Log.e(TAG,"wiredHeadsetIsOn -- " + msg.arg1 + ", " +msg.arg2 + ", "+ ((short[])msg.obj)[0]);
				if(headsetOn) {
					audioTrack.write((byte[])msg.obj, 0, msg.arg1);
					audioTrack.play();
				}
//				}
//				Log.e(TAG, "data is read in Speaker");
			}
		}
	}
}
