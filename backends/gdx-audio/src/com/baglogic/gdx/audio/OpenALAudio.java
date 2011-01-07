
package com.baglogic.gdx.audio;

import static org.lwjgl.openal.AL10.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;

public class OpenALAudio implements Audio {
	// BOZO
	public AudioDevice newAudioDevice (boolean isMono) {
		return null;
	}

	// BOZO
	public AudioRecorder newAudioRecoder (int samplingRate, boolean isMono) {
		return null;
	}

	private int[] streams;

	Array<OpenALMusic> music = new Array(false, 1, OpenALMusic.class);

	public OpenALAudio (int simultaneousStreams) {
		try {
			AL.create();
		} catch (LWJGLException ex) {
			throw new GdxRuntimeException("Error initializing OpenAL.", ex);
		}

		IntArray streams = new IntArray(false, simultaneousStreams);
		for (int i = 0; i < simultaneousStreams; i++) {
			int streamID = alGenSources();
			if (alGetError() != AL_NO_ERROR) break;
			streams.add(streamID);
		}
		this.streams = streams.items;

		FloatBuffer orientation = (FloatBuffer)BufferUtils.createFloatBuffer(6)
			.put(new float[] {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).flip();
		alListener(AL_ORIENTATION, orientation);
		FloatBuffer velocity = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).flip();
		alListener(AL_VELOCITY, velocity);
		FloatBuffer position = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {0.0f, 0.0f, 0.0f}).flip();
		alListener(AL_POSITION, position);
	}

	public OpenALSound newSound (FileHandle file) {
		String extension = file.extension();
		if (extension.equals("ogg")) return new OggSound(this, file);
		throw new GdxRuntimeException("Unknown file extension for sound: " + file);
	}

	public OpenALMusic newMusic (FileHandle file) {
		String extension = file.extension();
		if (extension.equals("ogg")) return new OggMusic(this, file);
		throw new GdxRuntimeException("Unknown file extension for music: " + file);
	}

	int getIdleStreamID () {
		for (int i = 0, n = streams.length; i < n; i++) {
			int streamID = streams[i];
			int state = alGetSourcei(streamID, AL_SOURCE_STATE);
			if (state != AL_PLAYING && state != AL_PAUSED) return streamID;
		}
		return -1;
	}

	public void update () {
		for (int i = 0; i < music.size; i++)
			music.items[i].update();
	}

	public void dispose () {
		for (int i = 0, n = streams.length; i < n; i++) {
			int streamID = streams[i];
			int state = alGetSourcei(streamID, AL_SOURCE_STATE);
			if (state != AL_STOPPED) alSourceStop(streamID);
			alDeleteSources(streamID);
		}
	}
}