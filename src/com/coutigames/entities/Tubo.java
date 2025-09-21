package com.coutigames.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.coutigames.main.Game;

public class Tubo extends Entity{

	
	public Tubo(double x, double y, int width, int height, double speed, BufferedImage sprite) {
		super(x, y, width, height, speed, sprite);
	}

	public void tick() {
		// Tubo se move baseado na velocidade do background
		x -= Game.backgroundSpeed;
		if(x+width <= 0)
		{
			//System.out.println("Destruido!");
			Game.score+=0.5;
			Game.entities.remove(this);
			return;
		}
	}
	
	public void render(Graphics g) {
		if(sprite != null) {
			g.drawImage(sprite, this.getX(),this.getY(), width,height,null);
		}
		else {
			g.setColor(Color.green);
			g.fillRect(this.getX(), this.getY(), width, height);
		}
	}
	
}
