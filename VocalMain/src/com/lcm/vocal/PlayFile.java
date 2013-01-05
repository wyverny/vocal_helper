package com.lcm.vocal;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class PlayFile implements Runnable {
	private File playFile;
	private AudioTrack audioTrack = null;
	
	public PlayFile(File playFile) {
		this.playFile = playFile;
	}

	public void audioStop() {
		if(audioTrack!=null) audioTrack.stop();
	}
	
	@Override
	public void run() {
        // Get the length of the audio stored in the file (16 bit so 2 bytes per short)
        // and create a short array to store the recorded audio.
        int musicLength = (int)(playFile.length()/2);
        short[] music = new short[musicLength];

        try {
          // Create a DataInputStream to read the audio data back from the saved file.
          InputStream is = new FileInputStream(playFile);
          BufferedInputStream bis = new BufferedInputStream(is);
          DataInputStream dis = new DataInputStream(bis);

          // Read the file into the music array.
          int i = 0;
          while (dis.available() > 0) {
            music[i] = dis.readShort();
            i++;
          }

          // Close the input streams.
          dis.close();     

          // Create a new AudioTrack object using the same parameters as the AudioRecord
          // object used to create the file.
          audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 
                                                 VocalMain.RECORDER_SAMPLERATE, 
                                                 AudioFormat.CHANNEL_CONFIGURATION_STEREO,//AudioFormat.CHANNEL_IN_STEREO, 
                                                 AudioFormat.ENCODING_PCM_16BIT, 
                                                 musicLength, 
                                                 AudioTrack.MODE_STREAM);
          // Start playback
          audioTrack.play();

          // Write the music buffer to the AudioTrack object
          audioTrack.write(music, 0, musicLength);
        } catch (Exception e) {
        	e.printStackTrace();
		} finally {
			if(audioTrack!=null) {
				audioTrack.stop();
				audioTrack = null;
			}
		}
	}
	
}
