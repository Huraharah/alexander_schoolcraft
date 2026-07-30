package snake_game;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Random;
import java.awt.Graphics;

/**Name: DrawPoly.java runs several methods to define the graphics for the Snake-like game that is compiled
 * through the main method in Csci1302_hw2
 * @author Alexander Schoolcraft
 * @version 02/10/2022 - 1.0 - initial game program completed
 * 			02/15/2022 - 1.1 - added a few system print messages to enhance player feedback while playing
 * 							 - minor updates to the game over screen to keep the final score centered regardless of the digits in the
 * 								final score
 */

@SuppressWarnings("serial")
public class DrawPoly extends JPanel{

	//global variable creation block
	Random rand = new Random();       //creates and instantiates a random number generator used for the initial start point
	                                  //as well as the location of the apples
	//the following boolean variables are used to control which screen is painted, the opening splash, the game over splash, or the
	//game itself, as well as identifying if the mouse is over one of the buttons on the splash screens
	boolean game_paused = true;       //starts the game paused for the opening splash
	boolean game_over = false;        //variable to trigger upon game over conditions
	boolean hover = false;             //used to detect if the mouse cursor is over the start button on the opening splash and end screen
	//the following variables are used in the splash screens to control their functions
	int current_x = 0;                 //initializes the current position of the read mouse x location
	int current_y = 0;                 //initializes the current position of the read mouse y location
	int high_score = 0;                //initializes a high score system for continued replay
	int high_score1 = 0;               //used in the new system out prints to control high score print lines
	//the following variables are the game control variables and are initialized to their starting values
	int current_direction = 0;        //initializes current direction, will be integer 0-3, 0 = right, increasing clockwise
	int tick_speed = 80;              //initializes the tick speed (the difficulty scaler for the game, the lower the number, the faster
	                                  //the game ticks
	int x_grid = rand.nextInt(75,406);//initializes x coordinate for the location to a random location in the play area
	int y_grid = rand.nextInt(75,356);//initializes the y coordinate for the location to a random location in the play area
	int current_time = 0;             //initializes the game clock to 0
	int current_tick = 0;             //initializes the game refresh to 0
	int current_score = 0;            //initializes the score at 0
	int apple_x = rand.nextInt(50,430);//initializes the x coordinate of the first apple
	int apple_y = rand.nextInt(50,380);//initializes the y coordinate of the first apple
	//the following variables are used for collision detection, and will be updated continually through the paintComponent method
	int head_x;                        //the center of the snakes head on x
	int head_y;                        //the center of the snakes head on y
	int apple_cent_x = apple_x + 11;   //center of the apple on x
	int apple_cent_y = apple_y + 11;   //center of the apple on y

	
	/**
	 * DrawPoly constructor method sets up the JPanel to build the game off of
	 * @param no parameters
	 * @return no return
	 */
	public DrawPoly(){
		PolygonPanel listener = new PolygonPanel();
		addKeyListener(listener);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		setFocusable(true);
		setBackground(Color.black);
		setPreferredSize(new Dimension(500,500));
	}
	
	/** 
	 * The paintComponent method conforms with the JComponent method of the same name, and creates the actual
	 *  graphics for the game as well as perform the logic to run the game
	 */
	public void paintComponent (Graphics page) {
		super.paintComponent (page);
		
		//creates the opening splash, with a title, instructions, and start button
		if (game_over == false && game_paused == true) {
			Font titleFont = new Font("SansSerif", Font.BOLD, 40);
			page.setFont(titleFont);
			page.setColor(Color.green);
			page.drawString("SNAKE" , 180, 75);
			Font instructions = new Font("SansSerif", Font.PLAIN, 20);
			page.setFont(instructions);
			page.drawString("WASD or Arrow Keys to turn", 120, 100);
			page.drawString("Eat apples to score", 168, 125);
			page.drawString("Avoid contact with the walls!", 125, 150);
			//two "IF" statements to indicate when the start button is hovered over
			if (hover == false) {
				page.setColor(Color.green);
				page.drawRect(150, 200, 200, 100);
				Font buttonFont = new Font("Serif", Font.BOLD, 45);
				page.setFont(buttonFont);
				page.drawString("START",175, 265);
			}
			if (hover == true) {
				page.setColor(Color.red);
				page.drawRect(150, 200, 200, 100);
				Font buttonFont = new Font("Serif", Font.BOLD+Font.ITALIC , 45);
				page.setFont(buttonFont);
				page.drawString("START",180, 265);
			}
		}
		
		//runs the actual game itself, moving the snake along the screen to collect the apples while avoiding its own body and the walls
		if (game_over == false && game_paused == false) {
			
			//uses the Pythagorean Theorem to determine the distance from the center of the head to the center of the apple to confirm
			//if the apple is eaten
			if (Math.abs(Math.sqrt(Math.pow(Math.abs(head_x - apple_cent_x), 2) + Math.pow(Math.abs(head_y - apple_cent_y), 2))) <= 10) {
				current_score ++;
				//increases the speed of the game every other apple, until the tick_speed is 1
				if (tick_speed > 1 && current_score % 2 == 0) {
					tick_speed --;
				}
				apple_x = rand.nextInt(50,430);//sets the new x coordinate of the next apple
				apple_y = rand.nextInt(50,380);//sets the new y coordinate of the next apple
				apple_cent_x = apple_x + 11;   //updates the center x location of the apple to correlate
				apple_cent_y = apple_y + 11;   //updates the center y location of the apple to correlate
				System.out.println("OM NOM NOM");
				if(current_score > high_score1) {
					System.out.println("NEW HIGH SCORE!");
					high_score1 = current_score;
				}
			}
			//long block of code that builds the snake head and animates it as well as turns it depending on current direction
			page.setColor(Color.green);
			if (current_direction == 0) { //moving right
				if ((current_tick / 4) % 4 == 0) {
					int[] x_coordinate = {x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,x_grid+11,x_grid+10,x_grid+9,
										  x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,x_grid+2,x_grid+1,x_grid+1,
										  x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,x_grid+4,x_grid+5,x_grid+6,
										  x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,x_grid+14,x_grid+15,
										  x_grid+16,x_grid+17,x_grid+11};
					int[] y_coordinate = {y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,y_grid+8,y_grid+9,y_grid+10,
										  y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,y_grid+17,y_grid+18,y_grid+19,
										  y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+19,y_grid+19,
										  y_grid+18,y_grid+17,y_grid+11};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+11,x_grid+16,x_grid+18,x_grid+19,x_grid+17,x_grid+19,x_grid+18,x_grid+16,x_grid+11};
					int[] y_tounge = {y_grid+10,y_grid+10,y_grid+8, y_grid+9, y_grid+11,y_grid+13,y_grid+14,y_grid+12,y_grid+11};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				
				if ((current_tick / 4) % 4 == 1) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+11};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+11,x_grid+16,x_grid+18,x_grid+19,x_grid+17,x_grid+19,x_grid+18,x_grid+16,x_grid+11};
					int[] y_tounge = {y_grid+10,y_grid+10,y_grid+8, y_grid+9, y_grid+11,y_grid+13,y_grid+14,y_grid+12,y_grid+11};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				if ((current_tick / 4) % 4 == 2) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,
										  y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
				}
				if ((current_tick / 4) % 4 == 3) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+11};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+11,x_grid+16,x_grid+18,x_grid+19,x_grid+17,x_grid+19,x_grid+18,x_grid+16,x_grid+11};
					int[] y_tounge = {y_grid+10,y_grid+10,y_grid+8, y_grid+9, y_grid+11,y_grid+13,y_grid+14,y_grid+12,y_grid+11};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				page.setColor(Color.white);
				int[] x_sclara = {x_grid+10,x_grid+13,x_grid+10,x_grid+7};
				int[] y_sclara = {y_grid+9,y_grid+6,y_grid+3,y_grid+6};
				page.fillPolygon(x_sclara, y_sclara, x_sclara.length);
				page.setColor(Color.black);
				int[] x_pupal = {x_grid+10,x_grid+11,x_grid+10,x_grid+8};
				int[] y_pupal = {y_grid+7,y_grid+6,y_grid+5,y_grid+6};
				page.fillPolygon(x_pupal, y_pupal, x_pupal.length);
			}
			if (current_direction == 1) { //moving down
				if ((current_tick / 4) % 4 == 0) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,x_grid+4,x_grid+11,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,y_grid+17,y_grid+11,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+12,x_grid+12,x_grid+14,x_grid+13,x_grid+11,x_grid+9,x_grid+8,x_grid+10,x_grid+10};
					int[] y_tounge = {y_grid+11,y_grid+16,y_grid+18, y_grid+19, y_grid+17,y_grid+19,y_grid+18,y_grid+16,y_grid+11};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				
				if ((current_tick / 4) % 4 == 1) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+11,x_grid+14,x_grid+15,x_grid+16,x_grid+17,
										  x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+11,y_grid+19,y_grid+19,y_grid+18,y_grid+17,
										  y_grid+16,y_grid+15,y_grid+14,y_grid+13,y_grid+12,y_grid+11,y_grid+10,y_grid+9,y_grid+8};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+12,x_grid+12,x_grid+14,x_grid+13,x_grid+11,x_grid+9,x_grid+8,x_grid+10,x_grid+10};
					int[] y_tounge = {y_grid+11,y_grid+16,y_grid+18, y_grid+19, y_grid+17,y_grid+19,y_grid+18,y_grid+16,y_grid+11};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
					}
				if ((current_tick / 4) % 4 == 2) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,
										  y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
				}
				if ((current_tick / 4) % 4 == 3) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+11,x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,
										  x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+11,y_grid+19,y_grid+19,y_grid+18,y_grid+17,
										  y_grid+16,y_grid+15,y_grid+14,y_grid+13,y_grid+12,y_grid+11,y_grid+10,y_grid+9,y_grid+8};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+12,x_grid+12,x_grid+14,x_grid+13,x_grid+11,x_grid+9,x_grid+8,x_grid+10,x_grid+10};
					int[] y_tounge = {y_grid+11,y_grid+16,y_grid+18, y_grid+19, y_grid+17,y_grid+19,y_grid+18,y_grid+16,y_grid+11};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
					}
				page.setColor(Color.white);
				int[] x_sclara = {x_grid+12,x_grid+15,x_grid+18,x_grid+15};
				int[] y_sclara = {y_grid+9,y_grid+6,y_grid+9,y_grid+12};
				page.fillPolygon(x_sclara, y_sclara, x_sclara.length);
				page.setColor(Color.black);
				int[] x_pupal = {x_grid+14,x_grid+15,x_grid+16,x_grid+15};
				int[] y_pupal = {y_grid+9,y_grid+8,y_grid+9,y_grid+10};
				page.fillPolygon(x_pupal, y_pupal, x_pupal.length);
			}
			if (current_direction == 2) { //moving left
				if ((current_tick / 4) % 4 == 0) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+11,x_grid+4,
										  x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,x_grid+14,
										  x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+11,y_grid+17,y_grid+18,
										  y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+19,
										  y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,y_grid+10,y_grid+9,
										  y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+10,x_grid+5,x_grid+3,x_grid+2,x_grid+4,x_grid+2,x_grid+3,x_grid+5,x_grid+10};
					int[] y_tounge = {y_grid+12,y_grid+12,y_grid+14, y_grid+13, y_grid+11,y_grid+9,y_grid+8,y_grid+10,y_grid+10};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
					}
				
				if ((current_tick / 4) % 4 == 1) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+11,x_grid+2,x_grid+2,x_grid+3,x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,
										  x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,x_grid+14,x_grid+15,x_grid+16,x_grid+17,
										  x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+11,y_grid+14,y_grid+15,y_grid+16,y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,
										  y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,
										  y_grid+16,y_grid+15,y_grid+14,y_grid+11,y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+10,x_grid+5,x_grid+3,x_grid+2,x_grid+4,x_grid+2,x_grid+3,x_grid+5,x_grid+10};
					int[] y_tounge = {y_grid+12,y_grid+12,y_grid+14, y_grid+13, y_grid+11,y_grid+9,y_grid+8,y_grid+10,y_grid+10};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
					}
				if ((current_tick / 4) % 4 == 2) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,
										  y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
				}
				if ((current_tick / 4) % 4 == 3) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,
										  y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+10,x_grid+5,x_grid+3,x_grid+2,x_grid+4,x_grid+2,x_grid+3,x_grid+5,x_grid+10};
					int[] y_tounge = {y_grid+12,y_grid+12,y_grid+14, y_grid+13, y_grid+11,y_grid+9,y_grid+8,y_grid+10,y_grid+10};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				page.setColor(Color.white);
				int[] x_sclara = {x_grid+9,x_grid+12,x_grid+15,x_grid+12};
				int[] y_sclara = {y_grid+15,y_grid+12,y_grid+15,y_grid+18};
				page.fillPolygon(x_sclara, y_sclara, x_sclara.length);
				page.setColor(Color.black);
				int[] x_pupal = {x_grid+11,x_grid+12,x_grid+13,x_grid+12};
				int[] y_pupal = {y_grid+15,y_grid+14,y_grid+15,y_grid+16};
				page.fillPolygon(x_pupal, y_pupal, x_pupal.length);
				
			}
			if (current_direction == 3) { //moving up
				if ((current_tick / 4) % 4 == 0) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+11,x_grid+4,x_grid+3,x_grid+2,x_grid+2,
										  x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,x_grid+4,
										  x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,x_grid+14,
										  x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+11,y_grid+4,y_grid+5,y_grid+6,y_grid+7,y_grid+8,
										  y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,y_grid+17,
										  y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,y_grid+10,
										  y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+9,x_grid+9,x_grid+7,x_grid+8,x_grid+10,x_grid+12,x_grid+13,x_grid+11,x_grid+11};
					int[] y_tounge = {y_grid+10,y_grid+5,y_grid+3, y_grid+2, y_grid+4,y_grid+2,y_grid+3,y_grid+5,y_grid+10};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				
				if ((current_tick / 4) % 4 == 1) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+11,x_grid+7,
										  x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,
										  x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,
										  x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,x_grid+14,x_grid+15,x_grid+16,x_grid+17,
										  x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+11,y_grid+2,y_grid+2,
										  y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,
										  y_grid+13,y_grid+14,y_grid+15,y_grid+16,y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,
										  y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,
										  y_grid+16,y_grid+15,y_grid+14,y_grid+11,y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+9,x_grid+9,x_grid+7,x_grid+8,x_grid+10,x_grid+12,x_grid+13,x_grid+11,x_grid+11};
					int[] y_tounge = {y_grid+10,y_grid+5,y_grid+3, y_grid+2, y_grid+4,y_grid+2,y_grid+3,y_grid+5,y_grid+10};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
					}
				if ((current_tick / 4) % 4 == 2) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+13,x_grid+12,
										  x_grid+11,x_grid+10,x_grid+9,x_grid+8,x_grid+7,x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,
										  x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,
										  x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,
										  x_grid+14,x_grid+15,x_grid+16,x_grid+17,x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,
										  x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+1,y_grid+1,y_grid+1,
										  y_grid+1,y_grid+1,y_grid+1,y_grid+2,y_grid+2,y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,
										  y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,y_grid+13,y_grid+14,y_grid+15,y_grid+16,
										  y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,
										  y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,y_grid+16,y_grid+15,y_grid+14,y_grid+11,
										  y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
				}
				if ((current_tick / 4) % 4 == 3) {
					int[] x_coordinate = {x_grid+19,x_grid+19,x_grid+18,x_grid+17,x_grid+16,x_grid+15,x_grid+14,x_grid+11,x_grid+7,
										  x_grid+6,x_grid+5,x_grid+4,x_grid+3,x_grid+2,x_grid+2,x_grid+1,x_grid+1,x_grid+1,x_grid+1,
										  x_grid+1,x_grid+1,x_grid+2,x_grid+2,x_grid+3,x_grid+4,x_grid+5,x_grid+6,x_grid+7,x_grid+8,
										  x_grid+9,x_grid+10,x_grid+11,x_grid+12,x_grid+13,x_grid+14,x_grid+15,x_grid+16,x_grid+17,
										  x_grid+18,x_grid+19,x_grid+19,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20,x_grid+20};
					int[] y_coordinate = {y_grid+7,y_grid+6,y_grid+5,y_grid+4,y_grid+3,y_grid+2,y_grid+2,y_grid+11,y_grid+2,y_grid+2,
										  y_grid+3,y_grid+4,y_grid+5,y_grid+6,y_grid+7,y_grid+8,y_grid+9,y_grid+10,y_grid+11,y_grid+12,
										  y_grid+13,y_grid+14,y_grid+15,y_grid+16,y_grid+17,y_grid+18,y_grid+19,y_grid+19,y_grid+20,
										  y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+20,y_grid+19,y_grid+19,y_grid+18,y_grid+17,
										  y_grid+16,y_grid+15,y_grid+14,y_grid+11,y_grid+10,y_grid+9,y_grid+8,y_grid+7,y_grid+6};
					page.fillPolygon(x_coordinate, y_coordinate, x_coordinate.length);
					page.setColor(Color.red);
					int[] x_tounge = {x_grid+9,x_grid+9,x_grid+7,x_grid+8,x_grid+10,x_grid+12,x_grid+13,x_grid+11,x_grid+11};
					int[] y_tounge = {y_grid+10,y_grid+5,y_grid+3, y_grid+2, y_grid+4,y_grid+2,y_grid+3,y_grid+5,y_grid+10};
					page.fillPolygon(x_tounge, y_tounge, x_tounge.length);
				}
				page.setColor(Color.white);
				int[] x_sclara = {x_grid+6,x_grid+9,x_grid+6,x_grid+3};
				int[] y_sclara = {y_grid+9,y_grid+12,y_grid+15,y_grid+12};
				page.fillPolygon(x_sclara, y_sclara, x_sclara.length);
				page.setColor(Color.black);
				int[] x_pupal = {x_grid+6,x_grid+7,x_grid+6,x_grid+5};
				int[] y_pupal = {y_grid+11,y_grid+12,y_grid+13,y_grid+12};
				page.fillPolygon(x_pupal, y_pupal, x_pupal.length);
			}
			//builds the apples in random locations off of the apple collision detection block from earlier
			page.setColor(Color.yellow);
			int[] x_apple = {apple_x+5,apple_x+8,apple_x+10,apple_x+11,apple_x+13,apple_x+16,apple_x+18,apple_x+18,apple_x+19,apple_x+19,
							 apple_x+18,apple_x+18,apple_x+16,apple_x+15,apple_x+14,apple_x+12,apple_x+11,apple_x+10,apple_x+9, apple_x+7,
							 apple_x+6, apple_x+5, apple_x+3, apple_x+3,apple_x+2, apple_x+2, apple_x+3,apple_x+3};
			int[] y_apple = {apple_y+4,apple_y+4,apple_y+6, apple_y+6, apple_y+4, apple_y+4, apple_y+6, apple_y+7, apple_y+8, apple_y+14,
							 apple_y+15,apple_y+16,apple_y+18,apple_y+18,apple_y+19,apple_y+19,apple_y+18,apple_y+18,apple_y+19,
							 apple_y+19,apple_y+18,apple_y+18,apple_y+16,apple_y+15,apple_y+14,apple_y+8,apple_y+7,apple_y+6};
			page.fillPolygon(x_apple,y_apple,x_apple.length);
			page.setColor(new Color (156,106,72));
			int[] x_stem = {apple_x+10,apple_x+11,apple_x+12,apple_x+11};
			int[] y_stem = {apple_y+5,apple_y+2,apple_y+1,apple_y+5};
			page.fillPolygon(x_stem,y_stem,x_stem.length);
			
			//sets the center variables of the player avatar (head_x and head_y), used in collision detections
			head_x = x_grid + 11;
			head_y = y_grid + 11;
			
			//sets the outside border
			page.setColor(Color.red);
			page.drawRect(50,50,400,350);
			
			//game clock, controlled by processor speed (iterates current_time each time the "repaint" method processes)
			//and current tick_speed
			current_time++;
			if (current_time % tick_speed == 0) {
				current_tick++;
				
				//moves the avatar each game tick, based off of the direction that the player has set (5 pixels a tick)
				if (current_direction == 0) {
					x_grid += 5;
				}
				if (current_direction == 1) {
					y_grid += 5;
				}
				if (current_direction == 2) {
					x_grid -= 5;
				}
				if (current_direction == 3) {
					y_grid -= 5;
				}
			}
			
			//displays the current score on the screen
			page.setColor(Color.green);
			Font scoreFont = new Font( "SansSerif", Font.PLAIN, 38 ); 
			page.setFont(scoreFont); 
			page.drawString("Score: "+ current_score, 175, 460);
			
			//calls the repaint method to re-process this paintComponent override method on a loop according to the game clock
			repaint();
			
			//detects for collisions with the walls, and ends the game if it does have a collision
			if (head_x <= 55 || head_x >= 445 || head_y <= 55 || head_y >= 395) {
				game_over = true;
				game_paused = true;
				System.out.println("SPLAT!");
				if (current_score > high_score) {
					System.out.println("NEW HIGH SCORE!!! " + current_score);
				}
				else if (high_score > current_score) {
					System.out.println("High Score: " + high_score + " Your Score: " + current_score);
				}
				else if (current_score == high_score) {
					System.out.println("You tied the high score! " + current_score);
				}
				System.out.println("You played for " + current_tick + " ticks, and got to a speed of " + tick_speed + " cycles per tick.");
				System.out.println("(Total cycles: " + current_time + ". Average Tick speed: " + current_time/current_tick + ")");
			}
		}
		//end-game splash screen
		if (game_over == true) {
			Font endFont = new Font( "SansSerif", Font.PLAIN, 38 );
			page.setFont(endFont);
			page.setColor(Color.red);
			page.drawString("GAME OVER" , 140, 98);
			if (current_score < 10)
				page.drawString("Final Score: "+ current_score, 135, 150);
			else if (current_score >= 10 && current_score < 100)
				page.drawString("Final Score: "+ current_score, 125, 150);
			else
				page.drawString("Final Score: "+ current_score, 115, 150);
			//detects if the current score is higher than the old high score and updates it accordingly
			if (current_score >= high_score) {
				page.setColor(Color.green);
				page.drawString("NEW HIGH SCORE!", 75, 210);
				high_score = current_score;
			}
			else {
				page.setColor(Color.yellow);
				if (high_score < 10)
					page.drawString("High Score: "+ high_score, 135, 210);
				else if (high_score >= 10 && high_score < 100)
					page.drawString("High Score: "+ high_score, 125, 210);
				else
					page.drawString("High Score: "+ high_score, 115, 210);
			}
			
			//builds a button to loop the game back to the start splash
			if (hover == false) {
				page.setColor(Color.green);
				page.drawRect(150, 250, 200, 100);
				Font buttonFont = new Font("Serif", Font.BOLD, 40);
				page.setFont(buttonFont);
				page.drawString("Try Again?",155, 315);
			}
			if (hover == true) {
				page.setColor(Color.red);
				page.drawRect(150, 250, 200, 100);
				Font buttonFont = new Font("Serif", Font.BOLD+Font.ITALIC , 40);
				page.setFont(buttonFont);
				page.drawString("Try Again?",160, 315);
			}
		}
	}
	
	/**The PolygonPanel class implements the KeyListener methods to "read" user input from the keyboard to
	 * change the direction of the snake
	 * @author Alexander Schoolcraft
	 */
	private class PolygonPanel implements KeyListener, MouseListener, MouseMotionListener {
		
		/** keyPressed uses the KeyListener to listen for either arrow keys or WASD keys to adjust the
		 * direction of the snake, while not letting the player reverse direction on themselves
		 */
		public void keyPressed(KeyEvent event) {
			//sets the direction to right if it is not currently left
			if (event.getKeyCode() == KeyEvent.VK_RIGHT || event.getKeyCode() == KeyEvent.VK_D) {
				if (current_direction != 2) {
					current_direction = 0;
				}
			}
			//sets the direction to down if it is not currently up
			if (event.getKeyCode() == KeyEvent.VK_DOWN || event.getKeyCode() == KeyEvent.VK_S) {
				if (current_direction != 3) {
					current_direction = 1;
				}
			}
			//sets the direction to left if it is not currently right
			if (event.getKeyCode() == KeyEvent.VK_LEFT || event.getKeyCode() == KeyEvent.VK_A) {
				if (current_direction != 0) {
					current_direction = 2;
				}
			}
			//sets the direction to up if it is not currently down
			if (event.getKeyCode() == KeyEvent.VK_UP || event.getKeyCode() == KeyEvent.VK_W) {
				if (current_direction != 1) {
					current_direction = 3;
				}
			}
		}
		/** mouseMoved uses the MouseMotionListener to pinpoint the location of the cursor on the screen to determine if the start
		 *  button on the opening splash is hovered over
		 */
		public void mouseMoved(MouseEvent event) {
			//listens for movement of the mouse, and sets the x and y variables based off of its location
			if (game_paused == true) {
				current_x = event.getPoint().x;
				current_y = event.getPoint().y;
			}
			//calls the paintComponent method each time the mouse is moved
			repaint();
			
			if (game_over == false) {
			//checks if the mouse is over the start button on the opening splash, and sets the detection variable "hover" accordingly
				if((current_x > 150) && (current_x < 350) && (current_y > 200) && (current_y < 300)) {
					hover = true;
				}
				else {
					hover = false;
				}
			}
			
			//used to loop the game back to the start splash after a game over, with the same method as the start splash screen
			if (game_over == true) {
				if((current_x > 150) && (current_x < 350) && (current_y > 250) && (current_y < 350)) {
					hover = true;
				}
				else {
					hover = false;
				}
			}
		}
		/** mouseClicked uses the MouseListener to detect when the mouse is clicked if the start button on the opening splash is clicked
		 *  on
		 */
		public void mouseClicked(MouseEvent event) {
			//starts the game if the start button is clicked
			if(hover == true && game_paused == true && game_over == false) {
				if(event.getButton() == MouseEvent.BUTTON1) {
					game_paused = false;
				}
				repaint();
			}
			
			//loops back to opening splash after game over and resets the game function control variables to initial values
			if(hover == true && game_paused == true && game_over == true) {
				if(event.getButton() == MouseEvent.BUTTON1) {
					game_over = false;
					x_grid = rand.nextInt(75,406);
					y_grid = rand.nextInt(75,356);
					current_score = 0;
					current_tick = 0;
					current_time = 0;
					tick_speed = 80;
					current_direction = 0;
					apple_x = rand.nextInt(50,430);
					apple_y = rand.nextInt(50,380);
					apple_cent_x = apple_x + 11; 
					apple_cent_y = apple_y + 11;
				}
				repaint();
			}
		}
		public void mousePressed(MouseEvent event) {}    //unused event
		public void mouseReleased(MouseEvent event) {}  //unused event
		public void mouseEntered(MouseEvent event) {}   //unused event
		public void mouseExited(MouseEvent event) {}    //unused event
		public void mouseDragged(MouseEvent event) {}   //unused event
		public void keyTyped(KeyEvent event) {}         //unused event
		public void keyReleased(KeyEvent event) {}      //unused event
	}
}