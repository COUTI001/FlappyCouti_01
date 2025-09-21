package com.coutigames.world;

import com.coutigames.entities.Entity;
import com.coutigames.entities.Tubo;
import com.coutigames.main.Game;

public class TuboGenerator {

	public int time = 0;
	public int targetTime = 60;
	
	public void tick() {
		time++;
		
		// Ajustar o tempo de spawn baseado na velocidade do background
		// Velocidade normal = 1, targetTime = 60
		// Velocidade aumentada = 3, targetTime = 20 (mais rápido)
		// Velocidade reduzida = 0.3, targetTime = 200 (mais lento)
		int adjustedTargetTime = (int)(60 / Game.backgroundSpeed);
		
		if(time >= adjustedTargetTime) {
			// Dificuldade aumenta conforme a pontuação
			int dificuldade = (int)Game.score / 10;
			int minAltura = 20 + dificuldade;
			int maxAltura = 60 + dificuldade;
			if (maxAltura > Game.HEIGHT - 40) maxAltura = Game.HEIGHT - 40;
			if (minAltura > maxAltura - 10) minAltura = maxAltura - 10;
			int altura1 = Entity.rand.nextInt(maxAltura - minAltura) + minAltura;
			Tubo tubo1 = new Tubo(Game.WIDTH,1,29,altura1,1,Game.spritesheet.getSprite(32, 32, 16, 16));
			int altura2 = Entity.rand.nextInt(maxAltura - minAltura) + minAltura;
			Tubo tubo2 = new Tubo(Game.WIDTH, Game.HEIGHT - altura2,29, altura2,1, Game.spritesheet.getSprite(48, 32, 16, 16));
			Game.entities.add(tubo1);
			Game.entities.add(tubo2);
			time = 0;
		}
		
	}
	
}
