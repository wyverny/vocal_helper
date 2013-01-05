package com.lcm.vocal;

/*
 * this source code, confered from below
 * http://www.google.com/codesearch/p?hl=ko#2SLD6EDsx4E/trunk/AudioRecorder.2/&q=audio%20package:http://krvarma-android-samples%5C.googlecode%5C.com
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class VocalMain extends Activity {
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	public static final int RECORDER_SAMPLERATE = 44100;
	public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
	public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private AudioManager audioManager;
	
	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private Speaker speaker = null;
	private Thread speakingThread = null;
	private boolean isRecording = false;
	
	private HeadsetReceiver headsetReceiver;
	
	private WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setButtonHandlers();
		enableButtons(false);

		audioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
		bindReceiver();
		
		webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;//super.shouldOverrideUrlLoading(view, url);
			}
        });
        WebSettings set = webView.getSettings();
        set.setJavaScriptEnabled(true);
        set.setBuiltInZoomControls(true);
        webView.loadUrl("http://www.gasazip.com");
        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		showDialog(id)
	}
	
	@Override
	protected void onPause() {
		enableButtons(false);
		stopRecording();
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		releaseReceiver();
		super.onDestroy();
	}
	
	private void setButtonHandlers() {
		((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.play_btn)).setOnClickListener(btnClick);
		
		((Button)findViewById(R.id.go_btn)).setOnClickListener(webViewClickListener);
        ((Button)findViewById(R.id.back_btn)).setOnClickListener(webViewClickListener);
        ((Button)findViewById(R.id.forward_btn)).setOnClickListener(webViewClickListener);
	}

	private void enableButton(int id, boolean isEnable) {
		((Button) findViewById(id)).setEnabled(isEnable);
	}

	private void enableButtons(boolean isRecording) {
		enableButton(R.id.btnStart, !isRecording);
		enableButton(R.id.btnStop, isRecording);
	}

	public static String getFolder() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);
		return file.getAbsolutePath();
	}
	public static String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + new Date(System.currentTimeMillis()).toString() + AUDIO_RECORDER_FILE_EXT_WAV);
	}

	private String getTempFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	private void startRecording() {
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);

		recorder.startRecording();

		isRecording = true;

		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();

		// start speaking thread
		 speaker = new Speaker(this, isRecording, audioManager.isWiredHeadsetOn());
		 speakingThread = new Thread(speaker,"Speaker Thread");
		 speakingThread.start();
	}

	private void writeAudioDataToFile() {
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int read = 0;

		if (null != os) {
//			int iter = 0;
//			int mSize = 0;
//			byte[] mData = null; 
			while (isRecording) {
				read = recorder.read(data, 0, bufferSize);
				
				// send audio data to speakingThread
				Message msg = new Message();
				msg.what = Speaker.SPEAK;
				msg.arg1 = read;
				msg.obj = data;
				speaker.getmHandler().sendMessage(msg);

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void stopRecording() {
		if (null != recorder) {
			isRecording = false;

			recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;

			 speaker.setRunning(false);
			 speaker = null;
			 speakingThread = null;
		}

		// should this be needed??
		copyWaveFile(getTempFilename(), getFilename());
		deleteTempFile();
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());

		file.delete();
	}

	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 2;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

		byte[] data = new byte[bufferSize];

		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			AppLog.logString("File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);

			while (in.read(data) != -1) {
				out.write(data);
			}

			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}

	private OnClickListener btnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnStart: {
				AppLog.logString("Start Recording");

				enableButtons(true);
				startRecording();

				break;
			}
			case R.id.btnStop: {
				AppLog.logString("Stop Recording");

				enableButtons(false);
				stopRecording();

				break;
			}
			case R.id.play_btn: {
				Intent intent = new Intent(VocalMain.this,RecordedFilesActivity.class);
				startActivity(intent);
			}
			}
		}
	};
	
	private OnClickListener webViewClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.go_btn:
				String url;
                EditText addr = (EditText)findViewById(R.id.urlTextView);
                url = addr.getText().toString();
                if(!url.startsWith("http://")) url = "http://" + url;
                webView.loadUrl(url);
                Toast.makeText(VocalMain.this, "directing to " + url, Toast.LENGTH_SHORT).show();
                NaverConnect nc = new NaverConnect();
                try {
					nc.logout();
				} catch (Exception e) {	e.printStackTrace(); }
                break;
			case R.id.back_btn:
				if (webView.canGoBack()) {
					webView.goBack();
				}
				break;
			case R.id.forward_btn:
				if (webView.canGoForward()) {
                    webView.goForward();
				}
				break;
			}
		}
	};
	
	private class HeadsetReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
//			Toast.makeText(OriginalAudioTestActivity.this, "state: " + state, Toast.LENGTH_SHORT).show();
			boolean state = (intent.getIntExtra("state", 0)==0)? false : true;
			if(speaker!=null) {
				speaker.setHeadsetOn(state);
			}
		}
	}
	
	private void bindReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		headsetReceiver = new HeadsetReceiver();
		registerReceiver(headsetReceiver, filter);
	}
	
	private void releaseReceiver() {
		unregisterReceiver(headsetReceiver);
	}
}