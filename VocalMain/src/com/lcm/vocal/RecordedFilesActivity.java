package com.lcm.vocal;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class RecordedFilesActivity extends ListActivity {
	private static final String TAG = "RecordedFilesActivity";
	private ArrayList<String> list;
	private ArrayAdapter<String> adapter;
	private ListView listView;
	private EditText inputText;
	private Button inputButton;
	private PlayFile playFile = null;
	private Thread playThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.filelist);
		
		listView = (ListView)findViewById(android.R.id.list);
		Button playButton = (Button)findViewById(R.id.file_play_btn);
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int selected = listView.getSelectedItemPosition();
				if(selected!=ListView.INVALID_POSITION) {
					playFile(list.get(selected));
				}
			}
		});
		Button stopButton = (Button)findViewById(R.id.file_stop_btn);
		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(playFile!=null) {
					playFile.audioStop();
					playThread.interrupt();
					playFile = null;
				}
			}
		});

		list = getFileList();
		Log.e(TAG,"file list: " + list.size());
		
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);

		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String fileName = list.get(position);
		playFile(fileName);
	}
	
	private void playFile(String fileName) {
		if(playFile!=null) {
			playFile.audioStop();
			playThread.interrupt();
			playFile = null;
		}
		Toast.makeText(RecordedFilesActivity.this, "Start playing " + fileName, Toast.LENGTH_SHORT).show();
		playFile = new PlayFile(new File(fileName));
		playThread = new Thread(playFile);
		playThread.start();
	}

	private ArrayList<String> getFileList() {
		ArrayList<String> files = new ArrayList<String>();
		
		File recordFolder = new File(VocalMain.getFolder());
		Log.e(TAG,"folder: " + VocalMain.getFolder());
		if(recordFolder==null) return files;
		
		if(recordFolder.listFiles().length > 0) {
			for(File file : recordFolder.listFiles()) {
				files.add(file.getName());
			}
		}
		
		return files;
	}

}
