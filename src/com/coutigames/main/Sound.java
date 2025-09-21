package com.coutigames.main;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class Sound {

	private Clip clip;
	
	public static final Sound Music_1 = new Sound("/Music_1.wav");
	public static final Sound Voar = new Sound("/Voar.wav");
	public static final Sound Verde = new Sound("/Verde.wav");
	public static final Sound Azul = new Sound("/Azul.wav");
	public static final Sound Amarelo = new Sound("/Amarelo.wav");
	
	
	private Sound(String name) {
		try {
			URL url = Sound.class.getResource(name);
			if (url == null) {
				System.err.println("Arquivo de áudio não encontrado: " + name);
				return;
			}
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void play() {
		if (clip == null) return;
		if (clip.isRunning()) clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}
	
	public void loop() {
		if (clip == null) return;
		if (clip.isRunning()) clip.stop();
		clip.setFramePosition(0);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stop() {
		if (clip != null && clip.isRunning()) {
			clip.stop();
		}
	}
}
