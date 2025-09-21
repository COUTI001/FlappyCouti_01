package com.coutigames.entities;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.coutigames.main.Game;
import com.coutigames.world.World;


public class Player extends Entity{
	
	public boolean isPressed = false;
	public boolean isLeftPressed = false;
	public boolean isRightPressed = false;
	
	// Velocidade vertical (para subir/descer) - sempre fixa
	private double verticalSpeed = 2;
	// Velocidade horizontal (para movimento lateral) - afetada pelos power-ups
	private double horizontalSpeed = 2;
	
	public Player(int x, int y, int width, int height,double speed,BufferedImage sprite) {
		super(x, y, width, height,speed,sprite);
	}
	
	public void tick(){
		depth = 2;
		
		// Movimento vertical (subir/descer) - sempre com velocidade fixa
		if(!isPressed)
		{
			y+=verticalSpeed;
		}else {
			if(y > 0) { //Para que o flapy no passe do teto
			y-=verticalSpeed;
			}
		}
		
		// Movimento horizontal (esquerda/direita) - afetado pelos power-ups
		if(isLeftPressed && x > 0) {
			x -= horizontalSpeed;
		}
		if(isRightPressed && x < Game.WIDTH - width) {
			x += horizontalSpeed;
		}
		
		if(y > Game.HEIGHT) {
			System.out.println("Reset");
			World.restartGame();
			return;
		}
		
		//Testar colisão
		for(int i = 0; i < Game.entities.size(); i++) {
			Entity e = Game.entities.get(i);
			if(e != this)
			{
				if(Entity.isColidding(this, e)) {
					//Game over!
					World.restartGame();
					return;
				}
			}
		}
		
	}
	
	// Método para definir velocidade horizontal (usado pelos power-ups)
	public void setHorizontalSpeed(double speed) {
		this.horizontalSpeed = speed;
	}
	
	// Método para obter velocidade horizontal atual
	public double getHorizontalSpeed() {
		return this.horizontalSpeed;
	}
	

	

	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		if(!isPressed) {
			g2.rotate(Math.toRadians(20), this.getX() + width/2 ,this.getY() + height/2);
			g2.drawImage(sprite,this.getX(), this.getY(), null);
			g2.rotate(Math.toRadians(-20),this.getX() + width/2 ,this.getY() + height/2);
		}else {
			//g2.rotate(Math.toRadians(-20), this.getX() + width/2 ,this.getY() + height/2);
			g2.drawImage(sprite,this.getX(), this.getY(), null);
			//g2.rotate(Math.toRadians(20),this.getX() + width/2 ,this.getY() + height/2);
		}
	}
	


}
