/**
 * Sheathe.java - basic shmup for high school Computer Science project
 */
 
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

// done
public class Sheathe extends Applet implements Runnable, KeyListener {
	
	Thread animator;
	Image offImg;
	Graphics offGfx;
	Dimension d;
	
	int frameNum;
	int fps;

	public void init() {
		fps = 100; // <-- set FPS here
		frameNum = 0;
		this.addKeyListener(this);
		this.setFocusable(true);
		Everything.begin();
	}
	
	public void start() {
		animator = new Thread(this);
		animator.start();
	}
	
	public void stop() {
		animator = null;
	}
	
	// create offscreen Graphics object and call the next frame
	public void paint(Graphics g) {
		if (offGfx == null || !d.equals(getSize())) {
			d = getSize();
			offImg = createImage(d.width, d.height);
			offGfx = offImg.getGraphics();
		} else {
			offGfx.setColor(Color.white);
			offGfx.fillRect(0, 0, d.width, d.height);
		}
		offGfx.setColor(Color.black);
		
		Everything.nextFrame(offGfx);
		g.drawImage(offImg, 0, 0, null);
	}
	
	public void update(Graphics g) {
		paint(g);
	}
	
	// animate per frame
	public void run() {
		long time = System.currentTimeMillis();
		int delay = 1000 / fps;
		
		while (true) {
			
			time = System.currentTimeMillis() + delay;

			Everything.animateAll(frameNum);
			repaint();  
			try { 
				Thread.sleep(Math.max(0, time - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				break;
			}
			
			frameNum++;
		}
	}

	public void keyTyped(KeyEvent e) { }
	
	/* 37 = left
	 * 38 = up
	 * 39 = right
	 * 40 = down
	 * 17 = ctrl
	 */
	
	public void keyPressed(KeyEvent e) {
		Everything.handleKeyPress(e.getKeyCode());
	}
	
	public void keyReleased(KeyEvent e) {
		Everything.handleKeyRelease(e.getKeyCode());
	}
}

// giant mess of shit; not sure if it should be static..
class Everything {
	
	private static LinkedList coolStuff;
	private static LinkedList newStuff;
	private static Ship yourShip;
	private static Level lvl;
	private static int stage;
	private static int score;
	private static Random rand;
	private static boolean paused;
	private static int lives;
	private static HSGUI highscores;
	private static int lastKeyPress;
	private static int lastKeyRelease;
	
	public static void begin() {
		rand = new Random();
		coolStuff = new LinkedList();
		//yourShip = new Ship(150, 200);
		lvl = new StartUp();
		newStuff = new LinkedList();
		score = 0;
		paused = false;
		stage = 0;
		lives = 5;
		highscores = new HSGUI();
		initiateHighScores();
	}
	
	private static void initiateHighScores() {
		for (int i = 1; i <= 10; i++) {
			highscores.enterScore("TIM", i);
		}
	}
	
	public static void startUp() {
		coolStuff = new LinkedList();
		//yourShip = new Ship(150, 200);
		lvl = new StartUp(lvl.getStarLocs(), 1600);
		newStuff = new LinkedList();
		score = 0;
		paused = false;
		stage = 0;
		lives = 5;
	}
	
	// probably needs tweaking.. (lots and lots of it)
	public static void animateAll(int frame) {
		if (!paused) {
			lvl.animate(frame);
			if (stage == 1) runGame(frame);
		}
	}
	
	/*private static void enterHighScore(int frame) {
		lvl.animate();
	}
	
	private static void runStartUp(int frame) {
		lvl.animate();
	}*/
	
	private static void runGame(int frame) {
		
		/* if (frame % 50 == 0) {
			int a = rand.nextInt(3) +1;
			if (a == 1)
				coolStuff.add(new Enemy1(rand.nextInt(300), 0));
			else if (a == 2)
				coolStuff.add(new Enemy2(rand.nextInt(300), 0));
			else if (a == 3)
				coolStuff.add(new Boss1(rand.nextInt(300),0));
			} */
		
		yourShip.animate(frame);
		
		LinkedList goodStuff = new LinkedList();
		LinkedList badStuff = new LinkedList();
		
		goodStuff.add(yourShip);
		
		Iterator itr = coolStuff.iterator();
		while (itr.hasNext()) {
			Thing thing = ((Thing)itr.next());
			if (thing.exists())
				thing.animate(frame);
			else 
				itr.remove();
			String temp = thing.id();
			if (temp == "Main" || temp == "MainBullet")
				goodStuff.add(thing);
			else
				badStuff.add(thing);
		}
		
		Iterator itr1 = goodStuff.iterator();
		while (itr1.hasNext()) {
			Thing thing1 = ((Thing)itr1.next());
		 	if (thing1.exists()) {
		 	Iterator itr2 = badStuff.iterator();
		 	while (itr2.hasNext()) {
				Thing thing2 = ((Thing)itr2.next());
		 		if (thing2.exists() && 
		 			Location.collides(thing2.getLoc(), thing1.getLoc())) {
		 			thing1.collide(thing2);
		 			thing2.collide(thing1);
		 		}
			}
			}
		}
		
		coolStuff.addAll(newStuff);
		newStuff.clear();
		
		if (!yourShip.exists()) {
			lives--;
			if (lives <= 0) {
				highScore();
			}
			else {
				yourShip = new Ship(150, 400);
				lvl.start(lvl.getPos());
				coolStuff = new LinkedList();
			}
		}
		
	}
	
	public static void nextFrame(Graphics g) {
		lvl.draw(g);
		if (stage == 1) drawGame(g);
	}
	
	private static void drawGame(Graphics g) {
			try {
			yourShip.draw(g);
			LinkedList lol = new LinkedList();
			lol.addAll(coolStuff);
			Iterator itr = lol.iterator();
			while (itr.hasNext())
				((Thing)itr.next()).draw(g);
			} catch (Exception e) {}
			
			// status bar????
			g.setColor(Color.white);
			g.fillRect(0, 500, 300, 30);
			g.setColor(Color.blue);
			g.setFont(new Font("Verdana", 0, 10));
			g.drawString("Lives: " + lives + "                         Score: " + score, 20, 512);
	}
	
	public static void loadLevel(Level l) {
		if (yourShip == null) yourShip = new Ship(150, 400);
		lvl = l;
		stage = 1;
	}
	
	public static void highScore() {
		stage = 0;
		if (highscores.checkScore(score))
			lvl = new HighScoreEnter(lvl.getStarLocs(), 0, score, highscores);
		else gameOver();
	}
	
	public static void gameOver() {
		lvl = new GameOver(lvl.getStarLocs(), 0);
		stage = 0;
	}
	
	public static void credits() {
		stage = 1;
		lvl = new Credits(lvl.getStarLocs(), 0);
	}
	
	public static void add(Thing t) {
		newStuff.add(t);
	}
	
	public static Ship getShip() {
		return yourShip; }
	
	public static void changeScore(int sc) {
		score += sc; }
		
	public static Random getRand() {
		return rand; }
		
	public static void drawScores(Graphics g) {
		highscores.drawScore(g);
	}
	
	public static Level getLvl() {
		return lvl; }
	
	public static int getLastKeyPress() {
		int temp = lastKeyPress;
		lastKeyPress = 0;
		return temp; }
	public static int getLastKeyRelease() {
		int temp = lastKeyRelease;
		lastKeyRelease = 0;
		return temp; }
		
	
	/* 37 = left
	 * 38 = up
	 * 39 = right
	 * 40 = down
	 * 17 = ctrl
	 */
	
	public static void handleKeyPress(int keyCode) {
		lastKeyPress = keyCode;
		if (stage == 1) yourShip.checkKeyPress(keyCode);
		if (keyCode == 80)
			paused = !paused;
	}
	
	public static void handleKeyRelease(int keyCode) {
		lastKeyRelease = keyCode;
		if (stage == 1) yourShip.checkKeyRelease(keyCode);
	}
	
}

class Location {
	
	private double x, y, radius;
	
	public Location(double x, double y, double radius) {
		this.x = x; this.y = y; this.radius = radius; }
		
	public void changeLoc(double xplus, double yplus) {
		x += xplus; y += yplus; }
	
	public void setLoc(double newx, double newy) {
		x = newx; y = newy; }
		
	public int x() {
		return (int)x; }
		
	public int y() {
		return (int)y; }
		
	public static boolean collides(Location loc1, Location loc2) {
		return ((Math.pow(loc1.x-loc2.x, 2) + Math.pow(loc1.y-loc2.y, 2)) 
			<= Math.pow(loc1.radius + loc2.radius, 2));
	}
	
	public boolean inBounds() {
		boolean b = true;
		if (x < 0 || x > 300)
			b = false;
		if (y < 0 || y > 500)
			b = false;
		return b;
	}		
}
 
interface Level {
	
	public void animate(int frame);
	public void draw(Graphics g);
	public Location[] getStarLocs();
	public int getPos();
	public void start(int pos);
}

class Rainbow {
	
	int red; boolean redUp;
	int green; boolean greenUp;
	int blue; boolean blueUp;
	
	public Rainbow(int r, int g, int b) {
		red = r;
		green = g;
		blue = b;
		redUp = true;
		greenUp = false;
		blueUp = true;
	}
	
	public void chgColor() {
		if (redUp) {
			red++;
			if (red == 255) redUp = false;
		} else {
			red--;
			if (red == 0) redUp = true;
		}
		if (greenUp) {
			green++;
			if (green == 255) greenUp = false;
		} else {
			green--;
			if (green == 0) greenUp = true;
		}
		if (blueUp) {
			blue++;
			if (blue == 255) blueUp = false;
		} else {
			blue--;
			if (blue == 0) blueUp = true;
		}
	}
	
	public Color getColor() {
		return new Color(red, green, blue);
	}
	
	public void start(int pos) {}
}

class StartUp implements Level{

	private int position;
	private Location[] stars;
	private Rainbow clr;
	private boolean title;
	
	public StartUp() {
		Random rand = Everything.getRand();
		stars = new Location[50];
		for (int i = 0; i < 50; i++)
			stars[i] = new Location(rand.nextInt(300), rand.nextInt(500), 0);
		position = 0;
		clr = new Rainbow(128, 64, 254);
		title = true;
	}
	
	public StartUp(Location[] l, int pos) {
		Random rand = Everything.getRand();
		stars = l;
		position = pos;
		clr = new Rainbow(128, 64, 254);
		title = true;
	}
	
	public void animate(int frame) {
		
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
		
		if (Everything.getLastKeyPress() != 0)
			Everything.loadLevel(new Level1(stars, 0));
		
		position++;
		clr.chgColor();
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, 300, 500);
		
		g.setColor(Color.white);
		
		for (int i = 0; i < 50; i++)
			g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
		
		if (position % 2400 < 1600) {
			g.setFont(new Font("Verdana",2,40));
			g.drawString("SHEATHE", 45, 250 );
			g.setFont(new Font("Verdana", 1, 16));
			g.setColor(clr.getColor());
			g.drawString("PRESS ANY KEY TO BEGIN", 35, 400);
			if(position>1200){title = false;}
		} else {
			Everything.drawScores(g);
		}
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
	
	public void start(int pos) {}
} 

class GameOver implements Level{
	
	private int position;
	private Location[] stars;
	Rainbow clr;
	
	public GameOver() {
		Random rand = Everything.getRand();
		stars = new Location[50];
		for (int i = 0; i < 50; i++)
			stars[i] = new Location(rand.nextInt(300), rand.nextInt(500), 0);
		position = 0;
		clr = new Rainbow(254, 128, 63);
	}
	
	public GameOver(Location[] l, int pos) {
		Random rand = Everything.getRand();
		stars = l;
		position = pos;
		clr = new Rainbow(254, 128, 63);
	}
	
	public void animate(int frame) {
		
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
		
		position++;
		if (position == 600 || Everything.getLastKeyPress() == 17)
			Everything.startUp();
			
		clr.chgColor();
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, 300, 500);
		
		g.setColor(Color.white);
		
		for (int i = 0; i < 50; i++)
			g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
			
		g.setFont(new Font("Verdana", 1, 30));
		g.setColor(clr.getColor());
		if (position % 100 > 50) g.drawString("GAME OVER", 40, 240);
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
	
	public void start(int pos) {}
}

class HighScoreEnter implements Level {
	
	private int position;
	private Location[] stars;
	String name;
	int score;
	char nextChar;
	HSGUI highscores;
	boolean x;
	int key;
	
	public HighScoreEnter(Location[] l, int pos, int s, HSGUI h) {
		Random rand = Everything.getRand();
		stars = l;
		position = pos;
		score = s;
		name = "";
		nextChar = 'A';
		Everything.getLastKeyPress();
		highscores = h;
		x = false;
		key = 0;
	}
	
	public void animate(int frame) {
		
		int key = Everything.getLastKeyPress();
		
		switch (key) {
			case 38:
				nextChar++;
				if (nextChar > 'Z') nextChar = 'A';
				break;
			case 40:
				nextChar--;
				if (nextChar < 'A') nextChar = 'Z';
				break;
			case 17:
			case 32:
				name = name+nextChar;
				nextChar = 'A';
				break;
		}
		
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
		
		position++;
		
		if (name.length() >= 3) {
			highscores.enterScore(name, score);
			Everything.startUp();
		}
	}
	
	public void draw(Graphics g) {
		if (x == true) {
			g.setColor(Color.black);
			g.fillRect(0, 0, 300, 500);
		
			g.setColor(Color.white);
		
			for (int i = 0; i < 50; i++)
				g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
		}
		else {
			g.setColor(Color.black);
			g.fillRect(0, 0, 300, 500);
		
			g.setColor(Color.white);
		
			for (int i = 0; i < 50; i++)
				g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
			
			g.setFont(new Font("Courier New", 1, 20));
			g.setColor(Color.white);
			g.drawString("Enter Your Initials", 40, 200);
			g.drawString(name + nextChar, 130, 240);
		}
			
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
	
	public void start(int pos) {}
}

class Credits implements Level{
	
	private int position;
	private Location[] stars;
	private boolean start;
	private boolean end;
	
	public Credits(Location[] l, int pos) {
		stars = l;
		position = pos;
		start = true;
		end = false;

	}
	
	public void start(int pos) {
		position = pos;
		start = true;
	}
	
	public void animate(int frame) {
		
		position++;
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, 300, 500);
		
		g.setColor(Color.white);
		g.setFont(new Font("Courier New", 1, 20));
		if(position<500){g.drawString("Credits", 40, position);}
		g.setFont(new Font("Courier New", 1, 20));
		if(position>200 && position<700){g.drawString("Project Manager", 40,position-215);}
		g.setFont(new Font("Courier New", 2, 20));
		if(position>200 && position<700){g.drawString("A. B.", 40,position-200);}
		g.setFont(new Font("Courier New", 1, 20));
		if(position>300 && position<800){g.drawString("Project Designer", 40,position-315);}
		g.setFont(new Font("Courier New", 2, 20));
		if(position>300 && position<800){g.drawString("T. M.", 40,position-300);}
		g.setFont(new Font("Courier New", 1, 20));
		if(position>400 && position<900){g.drawString("Design Artist", 40, position-415);}
		g.setFont(new Font("Courier New", 2, 20));
		if(position>400 && position<900){g.drawString("A. W.", 40, position-400);}
		g.setFont(new Font("Courier New", 1, 20));
		if(position>500 && position<1000){g.drawString("Documentation and",40,position-515);
		g.drawString("High Scores", 40,position-500);
		g.setFont(new Font("Courier New", 2, 20));}
		if(position>500 && position<1000){g.drawString("S. Y.", 40,position-485);}
		g.setFont(new Font("Courier New", 1, 20));
		if(position>600 && position<1100){g.drawString("Beta Testers", 40,position-645);}
		g.setFont(new Font("Courier New", 2, 20));
		if(position>600 && position<1100){g.drawString("A. B.", 40,position-630);}
		if(position>600 && position<1100){g.drawString("T. M.", 40,position-615);}
		if(position>600 && position<1100){g.drawString("A. W.", 40,position-600);}
		if(position>600 && position<1100){g.drawString("S. Y.", 40,position-585);}
		g.setFont(new Font("Courier New", 1, 30));
		if(position>1000 && position<1500){g.drawString("LONG LIVE",40,position-1030);
		 g.drawString("SPACE PIG!", 40,position-1000);}
		
		if(position > 1550)end = true;
		
		if(end) Everything.highScore();	
		for (int i = 0; i < 50; i++)
			g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
}

class Level1 implements Level{
	
	private int position;
	private Location[] stars;
	private boolean start;
	private int countdown;
	private boolean createdBoss;
	
	public Level1(Location[] l, int pos) {
		stars = l;
		position = pos;
		start = true;
		countdown = 300;
		createdBoss = false;
	}
	
	public void start(int pos) {
		position = pos;
		start = true;
		countdown = 300;
		createdBoss = false;
	}
	
	public void animate(int frame) {
	    Random rand = Everything.getRand();
	    if (start)
	    	countdown--;
	    else position += 1;
	    if (countdown <= 0) start = false;
	    if (!start) {
		if (position > 0 && position <= 2500) {
			if (position % 100 == 0)
				Everything.add(new Enemy1(rand.nextInt(300), 0));
		}
		if (position > 2500 && position <= 5000) {
			if (position % 100 == 0) {
					Everything.add(new Enemy3(rand.nextInt(250) +50, 0));
			}
		}
		if (position > 5000 && position <= 10000) {
			if (position % 75 == 0)
				if (rand.nextInt(2) == 0)
					Everything.add(new Enemy1(rand.nextInt(300), 0));
				else
					Everything.add(new Enemy3(rand.nextInt(250) +50, 0));
		}
		if (position >= 10500 && !createdBoss) {
			Everything.add(new Boss1(rand.nextInt(300), 0));
			createdBoss = true;
		}
		}
		
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, 300, 500);
		
		g.setColor(Color.white);
		
		for (int i = 0; i < 50; i++)
			g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
			
		if (start) {
			g.setFont(new Font("Verdana", 2, 20));
			if (countdown % 100 > 50) g.drawString("READY", 120, 240);
		}
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
}

class Level2 implements Level {
	
	private int position;
	private Location[] stars;
	private boolean start;
	private int countdown;
	private boolean createdBoss;
	
	public Level2(Location[] l, int pos) {
		Random rand = Everything.getRand();
		stars = l;
		position = pos;
		start = true;
		countdown = 300;
		createdBoss = false;
	}
	
	public void start(int pos) {
		position = pos;
		start = true;
		countdown = 300;
		createdBoss = false;
	}
	
	public void animate(int frame) {
	    Random rand = Everything.getRand();
	    if (start)
	    	countdown--;
	    else position += 1;
	    if (countdown <= 0) start = false;
	    if (!start) {
		if (position > 0 && position <= 2500) {
			if (position % 75 == 0)
				Everything.add(new Rock(rand.nextInt(300), 0));
		}
		if (position > 2500 && position <= 5000) {
			if (position % 75 == 0) {
				if (rand.nextInt(2) == 0)
					Everything.add(new Enemy5(rand.nextInt(300), 0));
				else
					Everything.add(new Rock(rand.nextInt(250) +50, 0));
			}
		}
		if (position > 5000 && position <= 10000) {
			if (position % 10 == 0)
				Everything.add(new Rock(rand.nextInt(300), 0));
		}
		if (position > 10000 && position <= 15000) {
			if (position % 50 == 0) {
				Everything.add(new Enemy5(rand.nextInt(300), 0));
			}
		}
		if (position >= 15500 && !createdBoss) {
			Everything.add(new Boss2(rand.nextInt(300), 0));
			createdBoss = true;
		}
		}
		
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, 300, 500);
		
		g.setColor(Color.white);
		
		for (int i = 0; i < 50; i++)
			g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
			
		if (start) {
			g.setFont(new Font("Verdana", 2, 20));
			if (countdown % 100 > 50) g.drawString("READY", 120, 240);
		}
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
}

class Level3 implements Level {
	
	private int position;
	private Location[] stars;
	private boolean start;
	private int countdown;
	private boolean createdBoss;
	
	public Level3(Location[] l, int pos) {
		Random rand = Everything.getRand();
		stars = l;
		position = pos;
		start = true;
		countdown = 300;
		createdBoss = false;
	}
	
	public void start(int pos) {
		position = pos;
		start = true;
		countdown = 300;
		createdBoss = false;
	}
	
	public void animate(int frame) {
	    Random rand = Everything.getRand();
	    if (start)
	    	countdown--;
	    else position += 1;
	    if (countdown <= 0) start = false;
	    if (!start) {
		if (position > 0 && position <= 3000) {
			if (position % 150 == 0)
				Everything.add(new Enemy4(rand.nextInt(300), 0));
		}
		if (position > 3000 && position <= 8000) {
			if (position % 100 == 0) {
				if (rand.nextInt(2) == 0)
					Everything.add(new Enemy4(rand.nextInt(300), 0));
				else
					Everything.add(new Enemy2(rand.nextInt(250) +50, 0));
			}
		}
		if (position > 8000 && position <= 12000) {
			if (position % 75 == 0) {
				if (rand.nextInt(2) == 0)
					Everything.add(new Enemy4(rand.nextInt(300), 0));
				else
					Everything.add(new Enemy2(rand.nextInt(250) +50, 0));
			}
		}
		if (position > 12000 && position <= 15000) {
			if (position % 50 == 0) {
				Everything.add(new Enemy2(rand.nextInt(300), 0));
			}
		}
		if (position >= 15500 && !createdBoss) {
			Everything.add(new Boss3(rand.nextInt(300), 0));
			createdBoss = true;
		}
		}
		
		for (int i = 0; i < 50; i++)
			stars[i].setLoc(stars[i].x(), (stars[i].y() + 1) % 500);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, 300, 500);
		
		g.setColor(Color.white);
		
		for (int i = 0; i < 50; i++)
			g.fillOval(stars[i].x()-1, stars[i].y()-1, 2, 2);
			
		if (start) {
			g.setFont(new Font("Verdana", 2, 20));
			if (countdown % 100 > 50) g.drawString("READY", 120, 240);
		}
	}
	
	public Location[] getStarLocs() {
		return stars; }
		
	public int getPos() { return position; }
}

// ehhhhhh... it works i think
abstract class Thing {
	
	protected Location loc;
	protected boolean exists;
	
	public abstract void animate(int frame);
	public abstract void draw(Graphics g);
	public Location getLoc() { return loc; }
	public boolean exists() { return exists; } 
	public void setExists(boolean b) { exists = b; }
	public abstract String id();
	public abstract void collide(Thing t);
}

// simple basic ship.... needs revision
class Ship extends Thing {
	
	private boolean right, left, up, down, shoot;
	private int shields;
	private double speed;
	private int weapon;
	private int lastShot;
	private boolean shieldShow;
	private int rateOfFire;
	private Gfx shipGfx;
		
	public Ship(int x, int y) {
		loc = new Location(x, y, 4);
		exists = true;
		speed = 1.5;
		shieldShow = false;
		rateOfFire = 40;
		shipGfx = new Gfx(1);
		shields = 0;
	}
	
	public void animate(int frame) {
		if (exists && !shipGfx.exploding()) {
		//checkKeyPress(Everything.getLastKeyPress());
		//checkKeyRelease(Everything.getLastKeyRelease());
		if (right) {
			if (shipGfx.getTwist() < 60) shipGfx.changeTwist(speed);
			if (loc.x() < 290)
				loc.changeLoc(speed, 0);
		}
		if (left) {
			if (shipGfx.getTwist() > -60) shipGfx.changeTwist(-speed);
			if (loc.x() > 10)
				loc.changeLoc(-speed, 0);
		}
		if (up) {
			if (loc.y() > 10)
				loc.changeLoc(0, -speed);
		}
		if (down) {
			if (loc.y() < 490)
				loc.changeLoc(0, speed);
		} 
		if (!right && !left) {
			if (shipGfx.getTwist() != 0) {
			if (shipGfx.getTwist() > 0) shipGfx.changeTwist(-1);
			else shipGfx.changeTwist(+1); }
		}
			
		
		if (shoot && (frame - lastShot > rateOfFire)) {
			lastShot = frame;
			Everything.add(new SimpleShot(loc.x(), loc.y() - 10));
		}
		}
	}
	
	public void checkKeyPress(int keyCode) {
		switch (keyCode) {
			case 37:
				left(); break;
			case 38:
				up(); break;
			case 39:
				right(); break;
			case 40:
				down(); break;
			case 17:
			case 32:
				shoot(); break;
		}
	}
	
	public void checkKeyRelease(int keyCode) {
		switch (keyCode) {
			case 37:
				noLeft(); break;
			case 38:
				noUp(); break;
			case 39:
				noRight();	break;
			case 40:
				noDown(); break;
			case 17:
			case 32:
				noShoot(); break;
		}
	}
	
	public void draw(Graphics g) {
		if (shipGfx.exploded()) exists = false;
		if (exists) {
			/*Polygon back = new Polygon();
			back.addPoint(loc.x()-20, loc.y()+10);
			back.addPoint(loc.x()+20, loc.y()+10);
			back.addPoint(loc.x(), loc.y());
			Polygon self = new Polygon();
			self.addPoint(loc.x()-10,loc.y()+10);//210 190
			self.addPoint(loc.x()+0,loc.y()-10);
			self.addPoint(loc.x()+10,loc.y()+10);
			self.addPoint(loc.x()+0,loc.y()+5);
			self.addPoint(loc.x()-10,loc.y()+10);
			g.fillPolygon(self);*/
			//g.fillOval(loc.x()-12, loc.y()-12, 24, 24);
			
			shipGfx.draw(g, loc);
			
			if (shields > 0) {
				if (shieldShow) {
				g.setColor(new Color(0, 128, 128));
				g.fillOval(loc.x()-12, loc.y()-12, 24, 24);
				}
				shieldShow = !shieldShow;
			}
			
		}
	}
	
	public void collide(Thing t) {
		String type = t.id();
		if (type == "Enemy" || type == "EnemyBullet") {
			shields--; shipGfx.flash();
			if (shields == -1) {
				shipGfx.explode();
			}
		}
		if (type == "PowerUp") {
			if (((PowerUp)t).getPowerUpType() == 1)
				if (speed < 3) speed+=.5; 
			if (((PowerUp)t).getPowerUpType() == 2)
				if (shields < 3) shields++;
			if (((PowerUp)t).getPowerUpType() == 3)
				if (rateOfFire > 10) rateOfFire -= 10;
		}
	}
	
	public void right() { right = true; }
	public void left() { left = true; }
	public void up() { up = true; }
	public void down() { down = true; }
	public void noRight() { right = false; }
	public void noLeft() { left = false; }
	public void noUp() { up = false; }
	public void noDown() { down = false; }
	
	public void shoot() { shoot = true; }
	public void noShoot() { shoot = false; }
	
	public String id() { return "Main"; }
}


/* following are types of Things
 * anything besides the main ship should fit into one of these categories
 * they help work out collisions and shit..
 */
 
abstract class MainBullet extends Thing {
	public String id() { return "MainBullet"; }
} 

abstract class PowerUp extends Thing {
	public abstract int getPowerUpType();
	public String id() { return "PowerUp"; }
}

abstract class Enemy extends Thing {
	Gfx enemyGfx;
	boolean exploding;
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (enemyGfx.exploded()) exists = false; 
		if (exists && !exploding)
			enemyAni(frame);
	}
	
	public void draw(Graphics g) {
		if (exists) {
			enemyGfx.draw(g, loc);
		}
	}
	
	public abstract void enemyAni(int frame);
	
	public String id() { if (!exploding) return "Enemy"; else return ""; }
	
	public void collide(Thing t) {
		if (t.id() == "MainBullet" && !exploding) {	
			enemyGfx.explode();
			exploding = true;
			drop(40);
			Everything.changeScore(1);
		}
	}
	
	public void drop(int p) {
		Random rand = Everything.getRand();
		int a = rand.nextInt(p);
		if (a == 1)
			Everything.add(new Speed(loc.x(), loc.y()));
		else if (a == 2)
			Everything.add(new Shield(loc.x(), loc.y()));
		else if (a == 3)
			Everything.add(new ROFL(loc.x(), loc.y()));
	}
}

abstract class EnemyBullet extends Thing {
	public String id() { return "EnemyBullet"; }
}

/* end Thing types */

/* extensions of Things... */
 
class SimpleShot extends MainBullet {
	
	public SimpleShot(int x, int y) {
		loc = new Location(x, y, 5);
		exists = true;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists) {
			loc.changeLoc(0, -6);
		}
	}
	
	public void draw(Graphics g) {
		if (exists) {
			g.setColor(Color.blue);
			g.fillOval(loc.x()-5, loc.y()-10, 10, 20);
			g.setColor(new Color(128, 128, 255));
			g.fillOval(loc.x()-3, loc.y()-7, 6, 14);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Enemy")
			exists = false;
	}
	
}

class Enemy1 extends Enemy {
	
	int lastShot;
	
	public Enemy1(int x, int y) {
		loc = new Location(x, y, 15);
		exists = true;
		lastShot = 0;
		enemyGfx = new Gfx(2);
		exploding = false;
	}
	
	public void enemyAni(int frame) {
		enemyGfx.changeTwist(2);
		loc.changeLoc(0, 2);
		if (frame - lastShot > 300) {
			//enemyGfx.setRot(-enemyGfx.getRot());
			lastShot = frame;
			Everything.add(new EnemyFire(loc.x(), loc.y()));
		}
	}
	
}

class Enemy2 extends Enemy {
	
	int lastShot;
	short direction; //1 = down, 2 = left, 3 = down, 4 = right
	int timeDirection;
	int health;
	
	public Enemy2(int x, int y) {
		if (x <= 60) x = 61;
		loc = new Location(x, y, 15);
		exists = true;
		lastShot = 0;
		direction = 1;
		timeDirection = 0;
		enemyGfx = new Gfx(3);
		exploding = false;
		health = 2;
	}
	
	public void enemyAni(int frame) {
		if (direction == 1) {
			loc.changeLoc(0, 2);
			if (enemyGfx.getRot() > 0) enemyGfx.changeRot(-2);
			if (enemyGfx.getRot() < 0) enemyGfx.changeRot(2);
		}
		if (direction == 2) {
			loc.changeLoc(-1,0);
			if (enemyGfx.getRot() < 90) enemyGfx.changeRot(2);
		}
		if (direction == 3) {
			loc.changeLoc(0, 2);
			if (enemyGfx.getRot() > 0) enemyGfx.changeRot(-2);
			if (enemyGfx.getRot() < 0) enemyGfx.changeRot(2);
		}
		if (direction == 4) {
			loc.changeLoc(1,0);
			if (enemyGfx.getRot() > -90) enemyGfx.changeRot(-2);
		}
		timeDirection++;
		if (timeDirection >= 60) {
			timeDirection = 0;
			direction ++;
			if (direction == 5)
				direction = 1;
		}
				
		if (frame - lastShot > 125) {
			lastShot = frame;
			Everything.add(new EnemyFire(loc.x(), loc.y()));
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "MainBullet") {
			health--; enemyGfx.flash();
			if (health == 0 && !exploding)	{
				enemyGfx.explode();
				exploding = true;
				drop(10);
				Everything.changeScore(5);
			}
		}
	}
	
}

class Enemy3 extends Enemy {
	
	int lastShot;
	
	public Enemy3(int x, int y) {
		loc = new Location(x, y, 15);
		exists = true;
		lastShot = 0;
		enemyGfx = new Gfx(6);
		enemyGfx.setSize(1.5);
		exploding = false;
	}
	
	public void enemyAni(int frame) {				
		loc.changeLoc(0, 1.5);
		if (frame - lastShot > 225) {
			lastShot = frame;
			Everything.add(new StraightEnemyFire(loc.x()+6, loc.y()));
			Everything.add(new StraightEnemyFire(loc.x()-9, loc.y()));
		}
	}
}

class Enemy4 extends Enemy {
	
	int lastShot;
	int health;
	boolean up;
	int lol;
	boolean done;
	
	public Enemy4(int x, int y) {
		loc = new Location(x, y, 15);
		exists = true;
		lastShot = 0;
		enemyGfx = new Gfx(7);
		exploding = false;
		up = false;
		done = false;
		lol = 0;
		health = 2;
	}
	
	public void enemyAni(int frame) {
		enemyGfx.changeTwist(3);
		
		if (lol == 6) done = true;
		
		if (!up || done) {
			loc.changeLoc(0, 1);
			if (loc.y() > 400) up = true;
		} else {
			loc.changeLoc(0, -1);
			if (loc.y() < 100) { up = false; lol++;}
		}
			
		if (frame - lastShot > 300) {
			lastShot = frame;
			Everything.add(new FollowShot(loc.x(), loc.y()));
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "MainBullet") {
			health--; enemyGfx.flash();
			if (health == 0 && !exploding)	{
				enemyGfx.explode();
				exploding = true;
				drop(10);
				Everything.changeScore(5);
			}
		}
	}
}

class Enemy5 extends Enemy {
	
	int lastShot;
	int curve;
	int xCoord;
	int health;
	
	public Enemy5(int x, int y) {
		if (x < 50) x = 50;
		if (x > 250) x = 250;
		loc = new Location(x, y, 15);
		exists = true;
		lastShot = 0;
		enemyGfx = new Gfx(8);
		exploding = false;
		enemyGfx.setSize(2);
		curve = 0;
		xCoord = x;
		health = 1;
	}
	
	public void enemyAni(int frame) {
		curve++;
		//if (enemyGfx.getRot() < 45)
			//enemyGfx.changeRot(1);
		loc.changeLoc(0, 1);
		if (curve%250 < 125 || curve%250 > 375)
			if (enemyGfx.getTwist() > -60) enemyGfx.changeTwist(-1);
		if (curve%250 > 125 && curve%250 < 375)
			if (enemyGfx.getTwist() < 60) enemyGfx.changeTwist(1);
		loc.setLoc(xCoord + 50*Math.sin(curve * (2*Math.PI/500)), loc.y());
		if (frame - lastShot > 200) {
			lastShot = frame;
			Everything.add(new Shuriken(loc.x(), loc.y()));
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "MainBullet") {
			health--; enemyGfx.flash();
			if (health == 0 && !exploding)	{
				enemyGfx.explode();
				exploding = true;
				drop(20);
				Everything.changeScore(5);
			}
		}
	}
}

class Rock extends Enemy {
	
	int health;
	public Rock(int x, int y) {
		loc = new Location(x, y, 15);
		exists = true;
		enemyGfx = new Gfx(11);
		exploding = false;
		enemyGfx.setSize(1.5);
		health = 2;
	}
	
	public void enemyAni(int frame) {
		enemyGfx.changeRot(6);
		loc.changeLoc(0, .5);
	}
	public void collide(Thing t) {
		Random rand = Everything.getRand();
		if (t.id() == "MainBullet" && !exploding) {	
			health --; enemyGfx.flash();
		}
		if (health == 0 && !exploding) {
			enemyGfx.explode();
			exploding = true;
			Everything.changeScore(2);
		}
		
	}
	
}

class Boss1 extends Enemy {
	
	int lastShot;
	int lastShotStraight;
	int health;
	boolean right;
	double red;
	int numBigShot;
	
	public Boss1(int x, int y) {
		loc = new Location(x, y, 50);
		exists = true;
		lastShot = 0;
		lastShotStraight = 0;
		health = 50;
		right = true;
		enemyGfx = new Gfx(5);
		red = 0;
		numBigShot = 300;
		exploding = false;
	}
	
	public void animate(int frame) {
		if (enemyGfx.exploded()) {
			exists = false;
			Everything.add(new Speed(loc.x(), loc.y()));
			Everything.add(new Shield(loc.x()+5, loc.y()));
			Everything.add(new ROFL(loc.x()+10, loc.y()));
			Everything.loadLevel(new Level2(Everything.getLvl().getStarLocs(), 0));
		} 
		if (exists && !exploding)
			enemyAni(frame);
	}
	
	public void enemyAni(int frame) {
		if (loc.y() < 50)
			loc.changeLoc(0, 2);
		else if (right) { 
			loc.changeLoc(1, 0);
			if (enemyGfx.getTwist() < 30) enemyGfx.changeTwist(.2);
			if (loc.x() >= 298)
				right = !right;
		} else {
			loc.changeLoc(-1, 0);
			if (enemyGfx.getTwist() > -30) enemyGfx.changeTwist(-.2);
			if (loc.x() <= 2)
				right = !right;
		}	
				
		if (frame - lastShot > 125) {
			lastShot = frame;
			Everything.add(new EnemyFire(loc.x(), loc.y()));
		}
		if (frame - lastShotStraight > 100) {
			lastShotStraight = frame;
			Everything.add(new StraightEnemyFire(loc.x() - 31, loc.y() + 45));
			Everything.add(new StraightEnemyFire(loc.x() + 22, loc.y() + 45));
		}
		red+=.5;
		if (red >= 255) {
			red = -50;
			Everything.add(new BigShot(loc.x(), loc.y()+40, right));
		}
			
	}
	
	public void draw(Graphics g) {
		if (exists) {
			g.setColor(new Color((red > 0) ? (int)red : 0, 0, 128));
			if (!exploding) g.fillRect(loc.x()-5, loc.y(), 10, 30);
			enemyGfx.draw(g, loc);
		}
	}
	
	public void collide(Thing t) {
		Random rand = Everything.getRand();
		if (t.id() == "MainBullet") {
			health --; enemyGfx.flash();
			if (health == 0)	{
				enemyGfx.explode(.001);
				exploding = true;
				Everything.changeScore(200);
			}
		}
	}
	
}

class Boss2 extends Enemy {
	
	int lastShotRot;
	int lastShotShur;
	int lastShotNorm;
	int health;
	boolean right;
	double red;
	int numBigShot;
	int sequence;
	
	public Boss2(int x, int y) {
		loc = new Location(50, 0, 50);
		exists = true;
		lastShotShur = 0;
		lastShotRot = 0;
		health = 100;
		right = true;
		enemyGfx = new Gfx(4);
		numBigShot = 300;
		exploding = false;
		sequence = 0;
		lastShotNorm = 0;
	}
	
	public void animate(int frame) {
		if (enemyGfx.exploded()) {
			exists = false;
			Everything.add(new Speed(loc.x(), loc.y()));
			Everything.add(new Shield(loc.x(), loc.y()+5));
			Everything.add(new ROFL(loc.x(), loc.y()+10));
			Everything.loadLevel(new Level3(Everything.getLvl().getStarLocs(), 0));
		} 
		if (exists && !exploding)
			enemyAni(frame);
	}
	
	public void enemyAni(int frame) {
		if (loc.y() < 50)
			loc.changeLoc(0, 2);
		else if (sequence >= 0 && sequence <= 5) {
			if (sequence % 2 == 0)
				loc.changeLoc(1,0);
			else 
				loc.changeLoc(-1,0);
			if (right) {
				if (enemyGfx.getRot() < 25)
					enemyGfx.changeRot(.2);
				if (enemyGfx.getRot() >= 25)
					right = false;
			}
			if (!right) {
				if (enemyGfx.getRot() > -25)
					enemyGfx.changeRot(-.2);
				if (enemyGfx.getRot() <= -25)
					right = true;
			}
			if (frame - lastShotRot > 35) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(),enemyGfx.getRot()));
			}
			if (frame - lastShotShur > 60) {
				lastShotShur = frame;
				Everything.add(new Shuriken(loc.x(), loc.y()));
			}
			if (loc.x() >= 250)
				sequence ++;
			if (loc.x() <= 50)
				sequence ++;
		}
		if (sequence == 6) {
			if (enemyGfx.getRot() <0)
				enemyGfx.changeRot(.2);
			else if (enemyGfx.getRot() > 0)
				enemyGfx.changeRot(-.2);
			if (Math.abs(enemyGfx.getRot()) < .4) {
				enemyGfx.setRot(0);
				sequence = 8;
			}
		}
		if (sequence == 8) {
			loc.changeLoc(0,1);
			enemyGfx.changeTwist(1.5);
			if (enemyGfx.getTwist() == 0)
				sequence = 9;
			if (frame - lastShotNorm > 45) {
				lastShotNorm = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
		}
		if (sequence == 9) {
			if (enemyGfx.getRot() > -60)
				enemyGfx.changeRot(-.2);
			if (frame - lastShotRot > 20) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(), enemyGfx.getRot()));
			}
			if (enemyGfx.getRot() <= -60)
			sequence = 10;
		}
		if (sequence == 10) {
			if (enemyGfx.getRot() < 0)
				enemyGfx.changeRot(.2);
			if (frame - lastShotRot > 20) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(), enemyGfx.getRot()));
			}
			if (enemyGfx.getRot() >= 0)
				sequence = 11;
		}
		if (sequence == 11) {
			loc.changeLoc(0,-1);
			enemyGfx.changeTwist(-1.5);
			if (enemyGfx.getTwist() == 0)
				sequence = 12;
			if (frame - lastShotNorm > 45) {
				lastShotNorm = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
		}
		if (sequence >= 12 && sequence <= 18) {
			if (sequence % 2 == 0)
				loc.changeLoc(1,0);
			else 
				loc.changeLoc(-1,0);
			if (right) {
				if (enemyGfx.getRot() < 25)
					enemyGfx.changeRot(.2);
				if (enemyGfx.getRot() >= 25)
					right = false;
			}
			if (!right) {
				if (enemyGfx.getRot() > -25)
					enemyGfx.changeRot(-.2);
				if (enemyGfx.getRot() <= -25)
					right = true;
			}
			if (frame - lastShotRot > 35) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(),enemyGfx.getRot()));
			}
			if (frame - lastShotShur > 60) {
				lastShotShur = frame;
				Everything.add(new Shuriken(loc.x(), loc.y()));
			}
			if (loc.x() >= 250)
				sequence ++;
			if (loc.x() <= 50)
				sequence ++;
		}
		if (sequence == 19) {
			if (enemyGfx.getRot() <=0)
				enemyGfx.changeRot(.2);
			else if (enemyGfx.getRot() >= 0)
				enemyGfx.changeRot(-.2);
			if (Math.abs(enemyGfx.getRot()) < .4) {
				enemyGfx.setRot(0);
				sequence ++;
			}
		}
		if (sequence == 20) {
			loc.changeLoc(0,1);
			enemyGfx.changeTwist(1.5);
			if (enemyGfx.getTwist() == 0)
				sequence = 21;
			if (frame - lastShotNorm > 45) {
				lastShotNorm = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
		}
		if (sequence == 21) {
			if (enemyGfx.getRot() < 60)
				enemyGfx.changeRot(.2);
			if (frame - lastShotRot > 20) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(), enemyGfx.getRot()));
			}
			if (enemyGfx.getRot() >= 60)
			sequence = 22;
		}
		if (sequence == 22) {
			if (enemyGfx.getRot() > 0)
				enemyGfx.changeRot(-.2);
			if (frame - lastShotRot > 20) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(), enemyGfx.getRot()));
			}
			if (enemyGfx.getRot() <= 0)
				sequence = 23;
		}
		if (sequence == 23) {
			loc.changeLoc(0,-1);
			enemyGfx.changeTwist(-1.5);
			if (enemyGfx.getTwist() == 0)
				sequence = 24;
			if (frame - lastShotNorm > 45) {
				lastShotNorm = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
		}
		if (sequence == 24) {
			loc.changeLoc(-1,0);
			if (right) {
				if (enemyGfx.getRot() < 25)
					enemyGfx.changeRot(.2);
				if (enemyGfx.getRot() >= 25)
					right = false;
			}
			if (!right) {
				if (enemyGfx.getRot() > -25)
					enemyGfx.changeRot(-.2);
				if (enemyGfx.getRot() <= -25)
					right = true;
			}
			if (frame - lastShotRot > 35) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(),enemyGfx.getRot()));
			}
			if (frame - lastShotShur > 60) {
				lastShotShur = frame;
				Everything.add(new Shuriken(loc.x(), loc.y()));
			}
			if (loc.x() <= 50)
				sequence = 0;	
		}			
	}
	
	public void draw(Graphics g) {
		if (exists) {
			g.setColor(new Color((red > 0) ? (int)red : 0, 0, 128));
			if (!exploding) g.fillRect(loc.x()-5, loc.y(), 10, 30);
			enemyGfx.draw(g, loc);
		}
	}
	
	public void collide(Thing t) {
		Random rand = Everything.getRand();
		if (t.id() == "MainBullet") {
			health --; enemyGfx.flash();
			if (health == 0)	{
				enemyGfx.explode(.001);
				exploding = true;
				Everything.changeScore(500);
			}
		}
	}
	
}

class Boss3 extends Enemy {
	
	int lastShot;
	int lastShotRot;
	int lastShotShur;
	int lastBigShot;
	int lastFollowShot;
	int health;
	double red;
	int numBigShot;
	int sequence;
	boolean dual;
	
	public Boss3(int x, int y) {
		loc = new Location(250, 0, 40);
		exists = true;
		lastShot = 0;
		lastShotRot = 0;
		health = 150;
		enemyGfx = new Gfx(10);
		enemyGfx.setSize(1.5);
		red = 0;
		numBigShot = 300;
		exploding = false;
		sequence = 0;
		lastShotShur = 0;
		dual = false;
	}
	
	public void animate(int frame) {
		if (enemyGfx.exploded()) {
			exists = false;
		} 
		if (exists && !exploding)
			enemyAni(frame);
	}
	
	public void enemyAni(int frame) {
		if (loc.y() < 50)
			loc.changeLoc(0, 2);
		else if (sequence == 0) {
			if (enemyGfx.getRot() < 45)
				enemyGfx.changeRot(1);
			if (enemyGfx.getRot() == 45)
				sequence = 1;		
		}
		if (sequence == 1) {
			loc.changeLoc(-1,1);
			if (loc.x() == 150)
				sequence = 2;
		}
		if (sequence == 2) {
			if (enemyGfx.getRot() > 0)
				enemyGfx.changeRot(-1);
			if (enemyGfx.getRot() == 0)
				sequence = 3;
		}
		if (sequence == 3) {
			if (enemyGfx.getRot() < 30)
				enemyGfx.changeRot(.2);
			if (frame - lastShotRot > 25) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(),enemyGfx.getRot()));
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
			if (enemyGfx.getRot() >= 30)
				sequence = 4;
			
		}
		if (sequence == 4) {
			if (enemyGfx.getRot() > -30)
				enemyGfx.changeRot(-.2);
			if (frame - lastShotRot > 25) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(),enemyGfx.getRot()));
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
			if (enemyGfx.getRot() <= -30)
				sequence = 5;
		}
		if (sequence == 5) {
			if (enemyGfx.getRot() < 0)
				enemyGfx.changeRot(.2);
			if (frame - lastShotRot > 25) {
				lastShotRot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y(),enemyGfx.getRot()));
				Everything.add(new EnemyFire(loc.x(), loc.y()));
			}
			if (enemyGfx.getRot() >= 0) {
				enemyGfx.setRot(0);
				sequence = 6;
			}
		}
		if (sequence == 6) {
			loc.changeLoc(0,-.2);
			if (frame - lastShotShur > 40) {
				lastShotShur = frame;
				Everything.add(new Shuriken(loc.x(), loc.y()));
			}
			if (frame - lastShot > 50) {
				lastShot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
				Everything.add(new StraightEnemyFire(loc.x(), loc.y()));
			}
			if (loc.y() <= 60)
				sequence = 7;
		}
		if (sequence >= 7 && sequence <= 13) {
			if (sequence % 2 != 0)
				loc.changeLoc(-1,0);
			else
				loc.changeLoc(1,0);
			if (frame - lastShotShur > 50) {
				lastShotShur = frame;
				Everything.add(new Shuriken(loc.x(), loc.y()));
			}
			if (frame - lastShot > 78) {
				lastShot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
				Everything.add(new StraightEnemyFire(loc.x(), loc.y()));
				dual = true;
			}
			if (frame - lastShot > 20 && dual == true) {
				lastShot = frame;
				Everything.add(new EnemyFire(loc.x(), loc.y()));
				Everything.add(new StraightEnemyFire(loc.x(), loc.y()));
				dual = false;
			}
			if (sequence == 9 && loc.x() == 200)
				Everything.add(new BigShot(loc.x(), loc.y() + 30, false));
			if (sequence == 12 && loc.x() == 100)
				Everything.add(new BigShot(loc.x(), loc.y()+30, true));
			if (loc.x() <= 50 || loc.x() >= 250)
				sequence ++;
		}
		if (sequence == 14) {
			if (enemyGfx.getRot() > -45)
				enemyGfx.changeRot(-1);
			if (enemyGfx.getRot() == -45)
				sequence = 15;	
		}
		if (sequence == 15) {
			loc.changeLoc(1,1);
			if (loc.x() == 150)
				sequence = 16;
		}
		if (sequence == 16) {
			if (enemyGfx.getRot() < 0)
				enemyGfx.changeRot(1);
			if (enemyGfx.getRot() == 0)
				sequence = 3;
		}
		
		if (frame - lastFollowShot > 50) {
			lastFollowShot = frame;
			Everything.add(new FollowShot(loc.x(), loc.y()));
		}
				
			
			
	}
	
	public void draw(Graphics g) {
		if (exists) {
			g.setColor(new Color((red > 0) ? (int)red : 0, 0, 128));
			if (!exploding) g.fillRect(loc.x()-5, loc.y(), 10, 30);
			enemyGfx.draw(g, loc);
		}
	}
	
	public void collide(Thing t) {
		Random rand = Everything.getRand();
		if (t.id() == "MainBullet") {
			health --; enemyGfx.flash();
			if (health == 0)	{
				enemyGfx.explode(.001);
				Everything.credits();
				exploding = true;
				Everything.changeScore(1000);
			}
		}
	}
	
}

class BigShot extends EnemyBullet {
	
	boolean created;
	boolean right;
	int countdown;
	
	public BigShot(int x, int y, boolean right) {
		loc = new Location(x, y, 20);
		exists = true;
		created = false;
		this.right = right;
		countdown = 100;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists) {
			countdown--;
			if (countdown == 0) exists = false;
			if (right) { 
				loc.changeLoc(1, 0);
				if (loc.x() >= 298)
					right = !right;
			} else {
				loc.changeLoc(-1, 0);
				if (loc.x() <= 2)
					right = !right;
			}	
			if (!created) {
				Everything.add(new BigShot(loc.x(), loc.y() + 5, right));
				created = true;
			}
		}
	}
	
	public void draw(Graphics g) {
		if (exists) {
			g.setColor(Color.green);
			g.fillRect(loc.x()-5, loc.y()-5, 10, 10);
			g.setColor(new Color(0, 128, 0));
			g.fillRect(loc.x()-3, loc.y()-5, 6, 10);
		}
	}
	
	public void collide(Thing t) {
	}
}

class StraightEnemyFire extends EnemyBullet {
	
	public StraightEnemyFire(int x, int y) {
		loc = new Location(x, y, 4);
		exists = true;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists) {
			loc.changeLoc(0, 2);
		}
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.green);
		if (exists)
			g.fillOval(loc.x()-4, loc.y()-4, 8, 8);
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
}

class EnemyFire extends EnemyBullet {
	
	private double chgX;
	private double chgY;
	
	public EnemyFire(int x, int y) {
		loc = new Location(x, y, 4);
		exists = true;
		double shipx = Everything.getShip().getLoc().x();
		double shipy = Everything.getShip().getLoc().y();
		double distance = Math.sqrt(Math.pow(shipx - x, 2) + Math.pow(shipy - y, 2));
		chgX = (shipx - x)/distance * 2;
		chgY = (shipy - y)/distance * 2;
	}
	
	public EnemyFire(int x, int y, double rot) {
		loc = new Location(x, y, 4);
		exists = true;
		rot = (rot - 90)/180 * Math.PI;
		chgX = -(2 * Math.cos(rot));
		chgY = -(2 * Math.sin(rot));
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists) {
			loc.changeLoc(chgX, chgY);
		}
	}
	
	public void draw(Graphics g) {
		if (exists) {
			g.setColor(Color.green);
			g.fillOval(loc.x()-4, loc.y()-4, 8, 8);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
}

class Shuriken extends EnemyBullet {
	
	private double chgX;
	private double chgY;
	private Gfx shotGfx;
	
	public Shuriken(int x, int y) {
		loc = new Location(x, y, 6);
		exists = true;
		shotGfx = new Gfx(9);
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists) {
			if (loc.x() < Everything.getShip().getLoc().x())
				loc.changeLoc(.5, 0);
			if (loc.x() > Everything.getShip().getLoc().x())
				loc.changeLoc(-.5, 0);
			loc.changeLoc(0, 2);
			shotGfx.changeRot(6);
		}
	}
	
	public void draw(Graphics g) {
		if (exists) {
			shotGfx.draw(g, loc);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
}

class FollowShot extends EnemyBullet {
	
	Gfx shotGfx;
	int count;
	
	public FollowShot(int x, int y) {
		loc = new Location(x, y, 8);
		exists = true;
		shotGfx = new Gfx(12);
		shotGfx.setSize(2);
		count = 0;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists) {
			count++;
			double x = Everything.getShip().getLoc().x();
			double y = Everything.getShip().getLoc().y();
			double distance = Math.sqrt((Math.pow(loc.x() - x, 2) + Math.pow(loc.y() - y, 2)));
			double chgX = (x - loc.x()) / distance;
			double chgY = (y - loc.y()) / distance;
			loc.changeLoc(chgX, chgY);
			if (count == 450) exists = false;
		}
	}
	
	public void draw(Graphics g) {
		if (exists) {
			shotGfx.draw(g, loc);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
}

class Speed extends PowerUp {
	
	public Speed(int x, int y) {
		loc = new Location(x, y, 6);
		exists = true;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists)
			loc.changeLoc(0, 1);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.yellow);
		if (exists) {
			g.fillOval(loc.x()-6, loc.y()-6, 12, 12);
			g.setColor(Color.black);
			g.setFont(new Font("Arial", 0, 12));
			g.drawString("S",loc.x()-4,loc.y()+5);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
	
	public int getPowerUpType() {
		return 1; }
}

class Shield extends PowerUp {
	
	public Shield(int x, int y) {
		loc = new Location(x, y, 6);
		exists = true;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists)
			loc.changeLoc(0, 1);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.yellow);
		if (exists) {
			g.setColor(new Color(35, 199, 199));
			Polygon p = new Polygon();
			p.addPoint(loc.x() + 6, loc.y());
			p.addPoint(loc.x(), loc.y() - 6);
			p.addPoint(loc.x() - 6, loc.y());
			p.addPoint(loc.x(), loc.y() + 6);
			p.addPoint(loc.x() + 6, loc.y());
			g.fillPolygon(p);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
	
	public int getPowerUpType() {
		return 2; }
}

class ROFL extends PowerUp {
	
	public ROFL(int x, int y) {
		loc = new Location(x, y, 6);
		exists = true;
	}
	
	public void animate(int frame) {
		if (!loc.inBounds()) exists = false;
		if (exists)
			loc.changeLoc(0, 1);
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.yellow);
		if (exists) {
			g.setColor(new Color(199, 39, 100));
			g.fillRect(loc.x()-6, loc.y()-6, 12, 12);
		}
	}
	
	public void collide(Thing t) {
		if (t.id() == "Main")
			exists = false;
	}
	
	public int getPowerUpType() {
		return 3; }
}

/* end of extentions of Things */	

/* graphics class
 * holds all the information for images used
 * plus methods for animating them in cool ways
 */
class Gfx {
	
	class Poly {
		double[] x;
		double[] y;
		int sides;
		int ptNum;
		Color c;
		public Poly(int s, Color c) {
			sides = s;
			x = new double[s];
			y = new double[s];
			ptNum = 0;
			this.c = c;
		}
		public void addPt(double a, double b) {
			x[ptNum] = a;
			y[ptNum] = b;
			ptNum++;
		}
		public void draw(Graphics g, Location l, double rad, double shift, double stretch) {
			draw(g, l, rad, shift, stretch, c);
		}
		public void draw(Graphics g, Location l, double rad, double shift, double stretch, Color newc) {
			g.setColor(newc);
			Polygon p = new Polygon();
			for (int i = 0; i < sides; i++) {
				double newx = x[i]*Math.cos(rad);
				p.addPoint((int)(l.x() + stretch*(((newx*Math.cos(shift) - y[i]*Math.sin(shift))))), 
					(int)(l.y() + stretch*(y[i]*Math.cos(shift) + x[i]*Math.sin(shift))));
			}
			g.fillPolygon(p);
		}
	}
	
	Poly[] p;
	double rotation;
	double twist;
	boolean explode;
	double size;
	boolean exploded;
	double sizeDec;
	int countFlash;
	boolean flash;
	
	public Gfx(int n) {
		rotation = 0;
		twist = 0;
		explode = false;
		exploded = false;
		size = 1;
		sizeDec = .01;
		countFlash = 0;
		flash = false;
		if (n == 1) {
			p = new Poly[2];
			p[0] = new Poly(3, Color.blue);
			p[0].addPt(-20, +10);
			p[0].addPt(+20, +10);
			p[0].addPt(0, 0);
			p[1] = new Poly(4, Color.pink);
			p[1].addPt(-10, +10);//210 190
			p[1].addPt(+0, -10);
			p[1].addPt(+10, +10);
			p[1].addPt(+0, +5);
		}
		else if (n == 2) {
			p = new Poly[5];
			p[0] = new Poly(4, Color.red);
			p[0].addPt(-12, -18);
			p[0].addPt(-12+3, -18);
			p[0].addPt(-12+3, -18+15);
			p[0].addPt(-12, -18+15);
			//g.fillRect(loc.x()-12, loc.y()-18, 3, 15);
			p[1] = new Poly(4, Color.red);
			p[1].addPt(8, -18);
			p[1].addPt(8+3, -18);
			p[1].addPt(8+3, -18+15);
			p[1].addPt(8, -18+15);
			//g.fillRect(loc.x()+8, loc.y()-18, 3, 15);
			//Polygon p = new Polygon();
			p[2] = new Poly(8, Color.red);
			for (int i = 0; i < 8; i++) {
				p[2].addPt((15*Math.cos(2*Math.PI * i / 8.0 + Math.PI/8.0)), 
					(15*Math.sin(2*Math.PI * i / 8.0 + Math.PI/8.0)));
			}
			//Polygon b = new Polygon();
			p[3] = new Poly(8, Color.white);
			for (int i = 0; i < 8; i++) {
				p[3].addPt((12*Math.cos(2*Math.PI * i / 8.0 + Math.PI/8.0)), 
					(12*Math.sin(2*Math.PI * i / 8.0 + Math.PI/8.0)));
			}
			//Polygon c = new Polygon();
			p[4] = new Poly(6, Color.blue);
			p[4].addPt((Math.cos(45/180.0 * Math.PI/2.0)*10), 0);
			for (int i = 0; i < 4; i++) {
				p[4].addPt((10*Math.cos(2*Math.PI * i / 8.0 + Math.PI/8.0)), 
					(10*Math.sin(2*Math.PI * i / 8.0 + Math.PI/8.0)));
			}
			p[4].addPt((-Math.cos(45/180.0 * Math.PI/2.0)*10), 0);
		
		}
		else if (n == 3) {
			/*Polygon m = new Polygon();
			Polygon n = new Polygon();
			for (int i = 0; i < 4; i++) {
				m.addPoint((int)(loc.x() + 15*Math.cos(2*Math.PI * i / 4.0)), 
					(int)(loc.y() + 15*Math.sin(2*Math.PI * i / 4.0)));
				n.addPoint((int)(loc.x() + 11*Math.cos(2*Math.PI * i / 4.0)), 
					(int)(loc.y() + 11*Math.sin(2*Math.PI * i / 4.0)));
				}*/
			p = new Poly[5];
			p[2] = new Poly(4, Color.blue);
			p[3] = new Poly(4, Color.black);
			for (int i = 0; i < 4; i++) {
				p[2].addPt((15*Math.cos(2*Math.PI * i / 4.0)), 
					(15*Math.sin(2*Math.PI * i / 4.0)));
				p[3].addPt((11*Math.cos(2*Math.PI * i / 4.0)), 
					(11*Math.sin(2*Math.PI * i / 4.0)));
			}
			/*g.setColor(Color.white);
			g.fillRect(loc.x()-13,loc.y()+3,3,8);
			g.fillRect(loc.x()+10,loc.y()+3,3,8);
			g.setColor(Color.blue);
			g.fillPolygon(m);
			g.setColor(Color.black);
			g.fillPolygon(n);
			g.setColor(Color.white);
			g.fillRect(loc.x()-4,loc.y()-4,8,8);*/
			p[0] = new Poly(4, Color.white);
			p[0].addPt(-13, 3);
			p[0].addPt(-13+3, 3);
			p[0].addPt(-13+3, 3+8);
			p[0].addPt(-13, 3+8);
			p[1] = new Poly(4, Color.white);
			p[1].addPt(10, 3);
			p[1].addPt(10+3, 3);
			p[1].addPt(10+3, 3+8);
			p[1].addPt(10, 3+8);
			p[4] = new Poly(4, Color.white);
			p[4].addPt(-4, -4);
			p[4].addPt(-4+8, -4);
			p[4].addPt(-4+8, -4+8);
			p[4].addPt(-4, -4+8);
		}
		else if (n == 4) {
			p = new Poly[3];
			p[0] = new Poly(12, Color.red);	
			p[0].addPt(15, -37);
			p[0].addPt(+65,-40);
			p[0].addPt(+35,-7);
			p[0].addPt(+25,+35);
			p[0].addPt(+15,0);
			p[0].addPt(-15,0);
			p[0].addPt(-25,+35);
			p[0].addPt(-35,-7);
			p[0].addPt(-65,-40);
			p[0].addPt(-15,-37);		
			p[0].addPt(-15,-37);
			p[0].addPt(0,-24);
			p[1] = new Poly(6, Color.blue);
			p[1].addPt(0,-20);
			p[1].addPt(-15,-35);
			p[1].addPt(-15,0);
			p[1].addPt(0,+45);
			p[1].addPt(+15,0);
			p[1].addPt(+15,-35);
			p[2] = new Poly(3, Color.black);
			p[2].addPt(-7,+10);
			p[2].addPt(0,+35);
			p[2].addPt(+7, +10);
			size = .75;
		}
		else if (n == 5) {
			p = new Poly[8];
			p[0] = new Poly(4, Color.white);
			p[0].addPt(-29, 25);
			p[0].addPt(-29+5, 25);
			p[0].addPt(-29+5, 25+22);
			p[0].addPt(-29, 25+22);
			p[1] = new Poly(4, Color.white);
			p[1].addPt(24, 25);
			p[1].addPt(24+5, 25);
			p[1].addPt(24+5, 25+22);
			p[1].addPt(24, 25+22);
			/*p[2] = new Poly(4, Color.white);
			p[2].addPt(-5, 0);
			p[2].addPt(-5+10, 0);
			p[2].addPt(-5+10, 0+30);
			p[2].addPt(-5, 0+30);*/
			p[2] = new Poly(0, Color.black);
			p[3] = new Poly(6, new Color(131,14,209));
			p[3].addPt(40,  - 5);
			p[3].addPt(0,-25);
			p[3].addPt( - 40, - 5);
			p[3].addPt( - 40, + 30);
			p[3].addPt(0, + 20);
			p[3].addPt(+ 40,+ 30);
			p[4] = new Poly(3, Color.blue);
			p[4].addPt(+40,  - 5);
			p[4].addPt(+75,  - 5);
			p[4].addPt(+40,  + 20);
			p[5] = new Poly(3, Color.blue);
			p[5].addPt(-40, - 5);
			p[5].addPt(-75,  - 5);
			p[5].addPt(-40, + 20);
			Polygon death = new Polygon();
			p[6] = new Poly(8, Color.white);
			for (int i = 0; i < 8; i++) {
				p[6].addPt(( + 15*Math.cos(2*Math.PI * i / 8.0 + Math.PI/8.0)), 
					( + 15*Math.sin(2*Math.PI * i / 8.0 + Math.PI/8.0)));
			}
			/*g.setColor(Color.black);
			g.fillOval(xcenter - 8, ycenter - 8, 15,15);*/
			p[7] = new Poly(4, Color.green);
			p[7].addPt(-3, -4);
			p[7].addPt(-3+5, -4);
			p[7].addPt(-3+5, -4+14);
			p[7].addPt(-3, -4+14);
		}
		else if (n == 6) {
			p = new Poly[4];
			p[0] = new Poly(4, new Color(14,180,62));
			p[0].addPt(5,3);
			p[0].addPt(7,3);
			p[0].addPt(7,8);
			p[0].addPt(5,8);
			p[1] = new Poly(4, new Color(14,180,62));
			p[1].addPt(-8,3);
			p[1].addPt(-6,3);
			p[1].addPt(-6,8);
			p[1].addPt(-8,8);
			p[2] =  new Poly(16, new Color(112, 14, 99));
			p[2].addPt(0,0);
			p[2].addPt(4,-4);
			p[2].addPt(6,0);
			p[2].addPt(10,0);
			p[2].addPt(3,-10);
			p[2].addPt(14,-2);
			p[2].addPt(14,4);
			p[2].addPt(6,3);
			p[2].addPt(0,10);
			p[2].addPt(-6,3);
			p[2].addPt(-14,4);
			p[2].addPt(-14,-2);
			p[2].addPt(-3,-10);
			p[2].addPt(-10,0);
			p[2].addPt(-6,0);
			p[2].addPt(-4,-4);
			p[3] = new Poly(3, Color.white);
			p[3].addPt(0,1);
			p[3].addPt(3,5);
			p[3].addPt(-3,5);
		}
		
		else if (n==7){
			p = new Poly[3];
			p[0] = new Poly(3, Color.red);
			Location loc1 = new Location(400,300,0);
			p[0].addPt(-30,-15);
			p[0].addPt(0,+25);
			p[0].addPt(+30,-15);
			p[1] = new Poly(8,new Color(226,163,14));
			p[1].addPt(-15,-20);
			p[1].addPt(-15,-13);
			p[1].addPt(-5,0);
			p[1].addPt(-5,+30);
			p[1].addPt(+5,+30);
			p[1].addPt(+5,0);
			p[1].addPt(+15,-13);
			p[1].addPt(+15,-20);
			p[2] = new Poly(6, Color.white);
			p[2].addPt(-7,-15);
			p[2].addPt(-7,-10);
			p[2].addPt(-2,-5);
			p[2].addPt(+2,-5);
			p[2].addPt(+7,-10);
			p[2].addPt(+7,-15);

		}
		else if (n==8) { 
			p = new Poly[2];
			p[0] = new Poly(16 , new Color(60,190,120));
			p[0].addPt(0 ,-12 );
			p[0].addPt(2 ,-4 );
			p[0].addPt(4 ,-8 );
			p[0].addPt(7 ,-4 );
			p[0].addPt(12 ,-7 );
			p[0].addPt(6 ,0 );
			p[0].addPt(5 ,4 );
			p[0].addPt(3 ,2 );
			p[0].addPt(0 ,9 );
			p[0].addPt(-3, 2 );
			p[0].addPt(-5 ,4 );
			p[0].addPt(-6 ,0 );
			p[0].addPt(-12 ,-7 );
			p[0].addPt(-7 ,-4 );
			p[0].addPt(-4 ,-8 );
			p[0].addPt(-2 ,-4 );
			p[1] = new Poly(3 , new Color(160,38,81));
			p[1].addPt(0,2);
			p[1].addPt(6 ,-2);
			p[1].addPt(-6 ,-2 );
		}
		else if (n == 9) {
			p = new Poly[1];
			p[0] = new Poly(8, Color.white);
			p[0].addPt(-6, 2);
			p[0].addPt(-2, -2);
			p[0].addPt(-2, -6);
			p[0].addPt(2, -2);
			p[0].addPt(6, -2);
			p[0].addPt(2, 2);
			p[0].addPt(2, 6);
			p[0].addPt(-2, 2);
		}
		else if (n ==10) {
			p = new Poly[5];
			p[0] = new Poly(3, new Color(90,180,255));
			p[0].addPt(-20,-30);
			p[0].addPt(20,-30);
			p[0].addPt(0, -7);
			p[1] = new Poly(3, new Color(180,90,255));
			p[1].addPt(-35,-10);
			p[1].addPt(35,-10);
			p[1].addPt(0,25);
			p[2] = new Poly(3, Color.white);
			p[2].addPt(-27,-6);
			p[2].addPt(27,-6);
			p[2].addPt(0,19);
			p[3] = new Poly(16, new Color(112,41,141));
			p[3].addPt(0,40);
			p[3].addPt(7,25);
			p[3].addPt(7,15);
			p[3].addPt(18,-10);
			p[3].addPt(14,-10);
			p[3].addPt(8,5);
			p[3].addPt(8,-35);
			p[3].addPt(4,-31);
			p[3].addPt(0,-35);
			p[3].addPt(-4,-31);
			p[3].addPt(-8,-35); 
			p[3].addPt(-8,5);
			p[3].addPt(-14,-10);
			p[3].addPt(-18,-10);
			p[3].addPt(-7,15);
			p[3].addPt(-7,25);
			p[4] = new Poly(5, Color.white);
			p[4].addPt(0,33);
			p[4].addPt(4,27);
			p[4].addPt(4,22);
			p[4].addPt(-4,22);
			p[4].addPt(-4,27);
		}
		else if (n == 11) {
			p = new Poly[3];
			p[0] = new Poly(8, new Color(113,91,51));
			p[0].addPt(0, 10);
			p[0].addPt(-7, 12);
			p[0].addPt(-12, 5);
			p[0].addPt(-5, -12);
			p[0].addPt(5, -8);
			p[0].addPt(7, -10);
			p[0].addPt(12, 0);
			p[0].addPt(3, 12);
			p[1] = new Poly(4, new Color(60, 45, 25));
			p[1].addPt(-7, 0);
			p[1].addPt(-5, -2);
			p[1].addPt(0, -5);
			p[1].addPt(-3, 2);
			p[2] = new Poly(4, new Color(60, 45, 25));
			p[2].addPt(2, 4);
			p[2].addPt(6, 1);
			p[2].addPt(8, -4);
			p[2].addPt(10, 2);
		}
		else if (n == 12) {
			p = new Poly[2];
			p[0] = new Poly(4, new Color(200, 100, 165));
			p[0].addPt(-4, 0);
			p[0].addPt(0, -4);
			p[0].addPt(4, 0);
			p[0].addPt(0, 4);
			p[1] = new Poly(4, new Color(133, 124, 209));
			p[1].addPt(-2, 0);
			p[1].addPt(0, -2);
			p[1].addPt(2, 0);
			p[1].addPt(0, 2);
		}
			
	}
	
	public void draw(Graphics g, Location l) {
		if (!explode && !flash) {
			boolean turnedOver = (twist > 90 && twist <= 270);
			double rad = twist/180 * Math.PI;
			double shiftrad = rotation/180 * Math.PI;
			
			if (!turnedOver)
				for (int i = 0; i < p.length; i++)
					p[i].draw(g, l, rad, shiftrad, size);
			else
				for (int i = p.length-1; i > -1; i--)
					p[i].draw(g, l, rad, shiftrad, size);
		} else if (explode) {
			Random rand = Everything.getRand();
			rotation+=2;
			twist+=2;
			size-=sizeDec;
			if (size <= 0) exploded = true;
			boolean turnedOver = (twist > 90 && twist <= 270);
			double rad = twist/180 * Math.PI;
			double shiftrad = rotation/180 * Math.PI;
			for (int i = 0; i < p.length; i++) {
				switch (i % 4) {
					case 0:	p[i].draw(g, l, rad, shiftrad, size); break;
					case 1: p[i].draw(g, l, -rad, shiftrad, size); break;
					case 2: p[i].draw(g, l, rad, -shiftrad, size); break;
					case 3: p[i].draw(g, l, -rad, -shiftrad, size); break;
				}
			}
		} else if (flash) {
			countFlash--;
			boolean turnedOver = (twist > 90 && twist <= 270);
			double rad = twist/180 * Math.PI;
			double shiftrad = rotation/180 * Math.PI;
			
			if (!turnedOver)
				for (int i = 0; i < p.length; i++)
					p[i].draw(g, l, rad, shiftrad, size, Color.white);
			else
				for (int i = p.length-1; i > -1; i--)
					p[i].draw(g, l, rad, shiftrad, size, Color.white);
			
			if (countFlash <= 0) flash = false;
		}
			
			
	}

	public void flash() { flash = true; countFlash = 20; }
	public void explode() { explode = true; }
	public void explode(double s) { explode = true; sizeDec = s;}
	public boolean exploding() { return explode; }
	public boolean exploded() { return exploded; }
	public double getRot() { return rotation; }
	public void changeRot(double r) { rotation += r; rotation = rotation % 360; }
	public void setRot(double r) { rotation = r; }
	public double getTwist() { return twist; }
	public void changeTwist(double r) { twist += r; twist = twist % 360; }
	public void setTwist(double r) { twist = r; }
	public double getSize() { return size; }
	public void changeSize(double s) { size *= s; }
	public void setSize(double s) { size = s; }
	
}

/**
 * The GUI for the high scores
 */
class HSGUI{
    //Temporary holders
    private int score;
    private String name;
    private int lowestScore;
    
    //Storage
    private File extStorage;
    private BufferedReader fileIn;
    private BufferedWriter fileOut;
    private String[] people;
    private int[] scores;
    private boolean noFile;
    
    //Constructor
    public HSGUI(){
        noFile=true;
        people = new String[10];
        scores = new int[10];
        initArrays();
        //try{openFile();}
        //catch(IOException e){e.printStackTrace();}
    }
    
    //For checking the score
    public boolean checkScore(int score){
        return score>=lowestScore;
    }
    
    public void enterScore(String name,int score){
        if(name==null || name.equals("")) name="No Name";

        //Find the place to squeeze the entry into and manipulate the arrays
        int insToIdx=0;
        while(people[insToIdx]!=null&&insToIdx<=8&&score<=scores[insToIdx]) insToIdx++;
        //If we have a last-place entry, then just replace it.
        if(insToIdx==9){
            people[insToIdx]=name;
            scores[insToIdx]=score;
        }
        /*Otherwise, time to manipulate the arrays!  Here, we will toss the
         *last-place sucker into oblivion... MUWAHAHAHAHA!!!*/
        else{
            for(int onIdx=8;onIdx>=insToIdx;onIdx--){
                people[onIdx+1]=people[onIdx];
                scores[onIdx+1]=scores[onIdx];
            }
            people[insToIdx]=name;
            scores[insToIdx]=score;            
        }
        
        if(!noFile)
            try{
                //Write the file
                fileOut=new BufferedWriter(new FileWriter(extStorage));
                int idx=0;
                while(!people[idx].equals("")){
                    fileOut.write(people[idx]);
                    fileOut.newLine();
                    fileOut.write(String.valueOf(scores[idx]));
                    fileOut.newLine();
                    idx++;
                }
            }
            catch(IOException e){}
            catch(ArrayIndexOutOfBoundsException e){}
            finally{
                try{
                    fileOut.close();
                }
                catch(IOException e){}}
    } 
    
    public void drawScore(Graphics g){
        //Header
        g.setFont(new Font("Comic Sans MS",Font.BOLD,16));
        g.drawString("Position",20,64);
        g.drawString("Name",120,64);
        g.drawString("Score",200,64);
        
        //Scores
        g.setFont(new Font("Arial",Font.BOLD,12));
        for(int idx=0;idx<10;idx++){
            g.drawString(String.valueOf(idx+1),48,89+32*idx);
            g.drawString(people[idx],128,89+32*idx);
            g.drawString(String.valueOf(scores[idx]),215,89+32*idx);
        }
    }
    
    private void initArrays(){
        people=new String[10];
        scores=new int[10];
        for(int idx=0;idx<scores.length;idx++){
            scores[idx]=0;
            people[idx]="";
        }
        lowestScore=scores[9];
    } 
}
