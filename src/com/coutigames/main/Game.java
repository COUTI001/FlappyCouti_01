/*** Produzido por André Luiz Coutinho
 * 
 */

package com.coutigames.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.coutigames.entities.Entity;
import com.coutigames.entities.Player;
import com.coutigames.graficos.Spritesheet;
import com.coutigames.graficos.UI;
import com.coutigames.world.TuboGenerator;
import com.coutigames.world.World;

public class Game extends Canvas implements Runnable,KeyListener,MouseListener,MouseMotionListener{

	private static final long serialVersionUID = 1L;
	public static JFrame frame;
	private static Game gameInstance; // Referência estática para a instância
	private Thread thread;
	private boolean isRunning = true;
	public static final int WIDTH = 320;
	public static final int HEIGHT = 220;
	public static final int SCALE = 3;
	
	private BufferedImage image;
	

	public static List<Entity> entities;
	public static Spritesheet spritesheet;
	public static Player player;
	
	public static TuboGenerator tubogenerator;
	
	public UI ui;
	
	public static double score = 0;
	
	// Estados do jogo
	public static enum GameState {
		MENU,
		INSTRUCTIONS,
		ABOUT,
		JOGO,
		PAUSED
	}
	
	public static GameState estado = GameState.MENU;
	
	// Opções do menu
	private String[] menuOptions = {"New Game", "Instructions", "About", "Music: ON", "Effects: ON", "Exit"};
	// Posições Y de cada item do menu (pode ser alterado para mudar a posição de cada item)
	private int[] menuOptionsY = {HEIGHT/2 - 50, HEIGHT/2 - 30, HEIGHT/2 -10, HEIGHT/2 + 8, HEIGHT/2 + 28, HEIGHT/2 + 50};
	private int selectedOption = 0;
	
	// Classe interna para nuvens
	private class Cloud {
		int x, y, width, height, speed;
		public Cloud(int x, int y, int width, int height, int speed) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.speed = speed;
		}
		public void tick() {
			// Nuvem se move com velocidade fixa (independente do background)
			x -= speed;
			if(x + width < 0) {
				x = WIDTH + (int)(Math.random()*100);
				y = (int)(Math.random()*(HEIGHT/2));
				speed = 1 + (int)(Math.random()*2);
			}
		}
		public void render(Graphics g) {
			g.setColor(new Color(255,255,255,180));
			g.fillRoundRect(x, y, width, height, 20, 20);
			g.fillRoundRect(x+width/3, y+height/3, width/2, height/2, 15, 15);
		}
	}
	// Lista de nuvens
	private List<Cloud> clouds = new ArrayList<>();
	
	// Classe para power-ups
	private class PowerUp {
		int x, y, width, height, type;
		boolean active;
		BufferedImage sprite;
		
		public PowerUp(int x, int y, int type) {
			this.x = x;
			this.y = y;
			this.width = 15;
			this.height = 15;
			this.type = type;
			this.active = true;
			
			// Definir sprite baseado no tipo
			if(type == 0) { // velocidade
				this.sprite = spritesheet.getSprite(65, 33, 15, 15);
			} else if(type == 1) { // desaceleração
				this.sprite = spritesheet.getSprite(81, 33, 15, 15);
			} else if(type == 2) { // escurecimento
				this.sprite = spritesheet.getSprite(97, 33, 15, 15);
			}
		}
		
		public void tick() {
			// Power-up se move com velocidade fixa (independente do background)
			x -= 2;
			if(x + width < 0) {
				active = false;
			}
		}
		
		public void render(Graphics g) {
			g.drawImage(sprite, x, y, width, height, null);
		}
		
		public boolean checkCollision(Player player) {
			return x < player.getX() + player.getWidth() &&
				   x + width > player.getX() &&
				   y < player.getY() + player.getHeight() &&
				   y + height > player.getY();
		}
	}
	
	// Lista de power-ups ativos
	private List<PowerUp> powerUps = new ArrayList<>();
	
	// Velocidade do background (afetada pelos power-ups)
	public static double backgroundSpeed = 1.5;
	
	// Efeitos temporários
	private int speedEffectTimer = 0;
	private int slowEffectTimer = 0;
	private int darkEffectTimer = 0;
	private boolean speedBoostActive = false;
	private boolean slowEffectActive = false;
	private boolean darkEffectActive = false;
	
	// Timer para spawn de power-ups
	private int powerUpSpawnTimer = 0;
	
	// High Score
	public static int highScore = 0;
	public static String highScoreName = "---";
	private static final String HIGHSCORE_FILE = "highscore.dat";
	// Controle de entrada de nome
	public static boolean aguardandoNome = false;
	public static String nomeDigitado = "";
	
	// Adicionar variável para armazenar o score aguardando registro
	public static int aguardandoScore = 0;
	
	// Controle de som
	public static boolean musicOn = true;
	public static boolean effectsOn = true;
	
	public Game(){
		Sound.Music_1.loop();
		gameInstance = this; // Inicializar referência estática
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
		initFrame();
		image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		
		//Inicializando objetos.
		spritesheet = new Spritesheet("/spritesheet.png");
		entities = new ArrayList<Entity>();
		player = new Player(WIDTH/2 - 30,HEIGHT/2,16,16,2,spritesheet.getSprite(0,0,16,16));
		tubogenerator = new TuboGenerator();
		ui = new UI();
		
		entities.add(player);
		
		// Inicializar nuvens
		for(int i=0; i<5; i++) {
			int cx = (int)(Math.random()*WIDTH);
			int cy = (int)(Math.random()*(HEIGHT/2));
			int cw = 40 + (int)(Math.random()*30);
			int ch = 15 + (int)(Math.random()*10);
			int cspeed = 1 + (int)(Math.random()*2);
			clouds.add(new Cloud(cx, cy, cw, ch, cspeed));
		}
		
		loadHighScore();
	}
	
	public void initFrame(){
		frame = new JFrame("Flappy Bird_Couti");
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public synchronized void start(){
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}
	
	public synchronized void stop(){
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
		Game game = new Game();
		game.start();
	}
	
	public void tick(){
		if (aguardandoNome) {
			// Não atualiza nada do jogo enquanto aguarda nome
			return;
		}
		if(estado == GameState.JOGO) {
			// Atualizar power-ups
			for(int i = powerUps.size() - 1; i >= 0; i--) {
				PowerUp p = powerUps.get(i);
				p.tick();
				
				// Verificar colisão com player
				if(p.active && p.checkCollision(player)) {
					activatePowerUp(p.type);
					p.active = false;
				}
				
				// Remover power-ups inativos
				if(!p.active) {
					powerUps.remove(i);
				}
			}
			
			// Spawn de power-ups aleatórios
			powerUpSpawnTimer++;
			if(powerUpSpawnTimer > 300) { // A cada ~5 segundos (60 FPS * 5)
				spawnRandomPowerUp();
				powerUpSpawnTimer = 0;
			}
			
			// Atualizar efeitos temporários
			updateEffects();
			
			for(Cloud c : clouds) c.tick();
			tubogenerator.tick();
			for(int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				e.tick();
			}
		}
		// No MENU, PAUSED, INSTRUCTIONS, ABOUT não faz nada
	}
	
	private void spawnRandomPowerUp() {
		int type = (int)(Math.random() * 3); // 0, 1 ou 2
		int x = WIDTH + 50;
		int y = 50 + (int)(Math.random() * (HEIGHT - 100));
		powerUps.add(new PowerUp(x, y, type));
	}
	
	private void activatePowerUp(int type) {
		// Desativar todos os power-ups anteriores primeiro
		speedEffectTimer = 0;
		slowEffectTimer = 0;
		darkEffectTimer = 0;
		speedBoostActive = false;
		slowEffectActive = false;
		darkEffectActive = false;
		
		// Ativar o novo power-up
		if(type == 0) { // velocidade
			speedEffectTimer = 600; // 10 segundos (60 FPS * 10)
			speedBoostActive = true;
			if(effectsOn) Sound.Verde.play();
		} else if(type == 1) { // desaceleração
			slowEffectTimer = 600;
			slowEffectActive = true;
			if(effectsOn) Sound.Amarelo.play();
		} else if(type == 2) { // escurecimento
			darkEffectTimer = 600;
			darkEffectActive = true;
			if(effectsOn) Sound.Azul.play();
		}
	}
	
	// Método para resetar todos os power-ups (chamado quando o jogo reinicia)
	public static void resetPowerUps() {
		gameInstance.speedEffectTimer = 0;
		gameInstance.slowEffectTimer = 0;
		gameInstance.darkEffectTimer = 0;
		gameInstance.speedBoostActive = false;
		gameInstance.slowEffectActive = false;
		gameInstance.darkEffectActive = false;
		gameInstance.backgroundSpeed = 1; // Voltar velocidade normal
	}
	
	private void updateEffects() {
		// Atualizar timer de velocidade
		if(speedEffectTimer > 0) {
			speedEffectTimer--;
			if(speedEffectTimer == 0) {
				speedBoostActive = false;
			}
		}
		
		// Atualizar timer de desaceleração
		if(slowEffectTimer > 0) {
			slowEffectTimer--;
			if(slowEffectTimer == 0) {
				slowEffectActive = false;
			}
		}
		
		// Atualizar timer de escurecimento
		if(darkEffectTimer > 0) {
			darkEffectTimer--;
			if(darkEffectTimer == 0) {
				darkEffectActive = false;
			}
		}
		
		// Aplicar efeitos de velocidade no player
		applySpeedEffects();
		
	}
	
	private void applySpeedEffects() {
		if(speedBoostActive) {
			// Aumentar velocidade do background (cria ilusão de aceleração)
			backgroundSpeed = 2.5; // Background mais rápido
		} else if(slowEffectActive) {
			// Diminuir velocidade do background (cria ilusão de desaceleração)
			backgroundSpeed = 1.3; // Background mais lento
		} else {
			// Velocidade do background normal
			backgroundSpeed = 1.9;
		}
	}
	
	// Função utilitária para interpolar cores
	private Color lerpColor(Color c1, Color c2, double t) {
		int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * t);
		int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
		int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
		return new Color(r, g, b);
	}
	
	public void render(){
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = image.getGraphics();
		// Cor de fundo dinâmica baseada na pontuação
		Color startColor = new Color(0,255,255); // azul claro
		Color endColor = new Color(255,128,0);   // laranja
		double t = Math.min(score/100.0, 1.0); // score 0 a 100 faz a transição
		Color bg = lerpColor(startColor, endColor, t);
		g.setColor(bg);
		g.fillRect(0, 0,WIDTH,HEIGHT);
		
		// Desenhar nuvens no fundo
		for(Cloud c : clouds) c.render(g);
		
		// Se aguardando nome, só renderiza a UI (tela de nome)
		if (aguardandoNome) {
			ui.render(g);
			g.dispose();
			g = bs.getDrawGraphics();
			g.drawImage(image, 0, 0,WIDTH*SCALE,HEIGHT*SCALE,null);
			ui.render(g);
			bs.show();
			return;
		}
		
		if(estado == GameState.JOGO) {
			Collections.sort(entities,Entity.nodeSorter);
			for(int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				e.render(g);
			}
			
			// Renderizar power-ups
			for(PowerUp p : powerUps) {
				p.render(g);
			}
			
			// Efeito de escurecimento
			if(darkEffectActive) {
				g.setColor(new Color(0, 0, 0, 120));
				g.fillRect(0, 0, WIDTH, HEIGHT);
			}
			
			// Exibir high score
			g.setColor(Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
			String hs = "Record: " + highScore + " - " + highScoreName;
			g.drawString(hs, WIDTH*SCALE - 10 - g.getFontMetrics().stringWidth(hs), 20);
		} else if(estado == GameState.PAUSED) {
			// Renderiza o jogo normalmente por baixo
			Collections.sort(entities,Entity.nodeSorter);
			for(int i = 0; i < entities.size(); i++) {
				Entity e = entities.get(i);
				e.render(g);
			}
			// Sobrepõe tela de pausa
			g.setColor(new Color(0,0,0,180));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 32));
			g.drawString("PAUSED", WIDTH/2 - 60, HEIGHT/2);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
			g.drawString("Press ESC to Continue", WIDTH/2 - 65, HEIGHT/2 + 30);
		} else if(estado == GameState.MENU) {
			// Menu principal
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
			g.drawString("Flappy Couti", WIDTH/2 - 60, HEIGHT/2 - 80);//Posição do Nome
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14)); // Fonte menor
			for(int i = 0; i < menuOptions.length; i++) {
				if(i == selectedOption) {
					g.setColor(Color.YELLOW);
				} else {
					g.setColor(Color.WHITE);
				}
				int larguraTexto = g.getFontMetrics().stringWidth(menuOptions[i]);
				g.drawString(menuOptions[i], (WIDTH - larguraTexto)/2, menuOptionsY[i]); // Centralizado corretamente
			}
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("Use ARROWS to Navigate and ENTER to Select", WIDTH/2 - 105, HEIGHT - 20);
		} else if(estado == GameState.INSTRUCTIONS) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
			g.drawString("Instructions", WIDTH/2 - 60, 40);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
			g.drawString("-Use SPACEBAR to Fly.", 30, 60);
			g.drawString("- Use the ARROWS to move the Bird.", 30, 75);
			g.drawString("- Dodge the pipes and survive as long as possible!", 30, 90);
			g.drawString("- Your score increases the longer you play..", 30, 105);
			g.drawString("- Yellow powerup, slows down..", 30, 120);
			g.drawString("- Green powerup, increases speed..", 30, 135);
			g.drawString("- Blue powerup, dims the screen..", 30, 150);
			
			
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("Press ESC to return to the menu", WIDTH/2 - 80, HEIGHT - 20);
		} else if(estado == GameState.ABOUT) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setColor(Color.WHITE);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
			g.drawString("About", WIDTH/2 - 25, 40);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
			g.drawString("Simple Flappy Bird clone made by André Luiz Coutinho.", 30, 90);
			g.drawString("Contact: couticomcoutinho@gmail.com", 30, 120);
			g.drawString("Version: 1.0", 30, 150);
			g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("Press ESC to return to the menu", WIDTH/2 - 90, HEIGHT - 20);
		}
		/***/
		g.dispose();
		g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0,WIDTH*SCALE,HEIGHT*SCALE,null);
		ui.render(g);
		bs.show();
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		int frames = 0;
		double timer = System.currentTimeMillis();
		requestFocus();
		while(isRunning){
			long now = System.nanoTime();
			delta+= (now - lastTime) / ns;
			lastTime = now;
			if(delta >= 1) {
				tick();
				render();
				frames++;
				delta--;
			}
			
			if(System.currentTimeMillis() - timer >= 1000){
				System.out.println("FPS: "+ frames);
				frames = 0;
				timer+=1000;
			}
			
		}
		
		stop();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (aguardandoNome) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (nomeDigitado.trim().isEmpty()) nomeDigitado = "Player";
				highScore = aguardandoScore; // Usa o score capturado no momento da derrota
				highScoreName = nomeDigitado;
				saveHighScore();
				aguardandoNome = false;
				aguardandoScore = 0;
				// Reiniciar o jogo após salvar o nome
				World.restartGame();
				return;
			} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && nomeDigitado.length() > 0) {
				nomeDigitado = nomeDigitado.substring(0, nomeDigitado.length() - 1);
			} else if (nomeDigitado.length() < 12) {
				char c = e.getKeyChar();
				if (Character.isLetterOrDigit(c) || c == ' ') {
					nomeDigitado += c;
				}
			}
			return;
		}
		if(estado == GameState.JOGO) {
			if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				player.isPressed = true;
				if(effectsOn) Sound.Voar.play();
			} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				player.isLeftPressed = true;
			} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				player.isRightPressed = true;
			} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				estado = GameState.PAUSED;
			}
		} else if(estado == GameState.PAUSED) {
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				estado = GameState.JOGO;
			}
		} else if(estado == GameState.MENU) {
			if(e.getKeyCode() == KeyEvent.VK_UP) {
				selectedOption--;
				if(selectedOption < 0) selectedOption = menuOptions.length - 1;
			} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
				selectedOption++;
				if(selectedOption >= menuOptions.length) selectedOption = 0;
			} else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				if(selectedOption == 0) { // Novo Jogo
					estado = GameState.JOGO;
					score = 0;
					entities.clear();
					player = new Player(WIDTH/2 - 30,HEIGHT/2,16,16,2,spritesheet.getSprite(0,0,16,16));
					entities.add(player);
					tubogenerator = new TuboGenerator();
				} else if(selectedOption == 1) { // Instruções
					estado = GameState.INSTRUCTIONS;
				} else if(selectedOption == 2) { // Sobre
					estado = GameState.ABOUT;
				} else if(selectedOption == 3) { // Música ON/OFF
					musicOn = !musicOn;
					menuOptions[3] = "Music: " + (musicOn ? "ON" : "OFF");
					if(musicOn) Sound.Music_1.loop(); else Sound.Music_1.stop();
				} else if(selectedOption == 4) { // Efeitos ON/OFF
					effectsOn = !effectsOn;
					menuOptions[4] = "Effects: " + (effectsOn ? "ON" : "OFF");
				} else if(selectedOption == 5) { // Sair
					System.exit(0);
				}
			}
		} else if(estado == GameState.INSTRUCTIONS || estado == GameState.ABOUT) {
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				estado = GameState.MENU;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(estado == GameState.JOGO) {
			if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				player.isPressed = false;
				
			} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				player.isLeftPressed = false;
			} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				player.isRightPressed = false;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {	
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	
	}

	private void loadHighScore() {
		try (BufferedReader br = new BufferedReader(new FileReader(HIGHSCORE_FILE))) {
			String line = br.readLine();
			if (line != null) {
				String[] parts = line.split(",", 2);
				try {
					highScore = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
				} catch (Exception ex) {
					highScore = 0;
				}
				if (parts.length > 1) highScoreName = parts[1];
			}
		} catch (Exception e) {
			highScore = 0;
			highScoreName = "---";
		}
	}

	private void saveHighScore() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(HIGHSCORE_FILE))) {
			bw.write(((int)highScore) + "," + highScoreName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Substituir o método checkHighScoreAndAskName para receber o score final
	public static void checkHighScoreAndAskName(int scoreFinal) {
		if (scoreFinal > highScore) {
			aguardandoNome = true;
			nomeDigitado = "";
			aguardandoScore = scoreFinal;
		}
	}
}
