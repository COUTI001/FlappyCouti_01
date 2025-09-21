package com.coutigames.graficos;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import com.coutigames.main.Game;

public class UI {

	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font("arial",Font.BOLD,22));
		g.drawString("Score: " + (int)Game.score,20,20);
		// High Score no topo direito
		g.setColor(Color.BLACK);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
		String hs = "Record Holder: " + Game.highScoreName + "  |  Score: " + Game.highScore;
		g.drawString(hs, Game.WIDTH*Game.SCALE - 25 - g.getFontMetrics().stringWidth(hs), 20);
		// Tela de entrada de nome do recordista
		if (Game.aguardandoNome) {
			g.setColor(new Color(0,0,0,200));
			g.fillRect(0, 0, Game.WIDTH*Game.SCALE, Game.HEIGHT*Game.SCALE);
			g.setColor(Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
			String msg = "New Record!";
			g.drawString(msg, Game.WIDTH*Game.SCALE/2 - g.getFontMetrics().stringWidth(msg)/2, Game.HEIGHT*Game.SCALE/2 - 40);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
			String msg2 = "Enter your name:";
			g.drawString(msg2, Game.WIDTH*Game.SCALE/2 - g.getFontMetrics().stringWidth(msg2)/2, Game.HEIGHT*Game.SCALE/2);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
			g.setColor(Color.YELLOW);
			g.drawString(Game.nomeDigitado + "_", Game.WIDTH*Game.SCALE/2 - g.getFontMetrics().stringWidth(Game.nomeDigitado+"_")/2, Game.HEIGHT*Game.SCALE/2 + 40);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("Press ENTER to Confirm", Game.WIDTH*Game.SCALE/2 - 90, Game.HEIGHT*Game.SCALE/2 + 70);
		}
	}
	
}
