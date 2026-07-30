package snake_game;

import javax.swing.*;

/**Name: Csci1302_hw2.java is the main method class to run a game of Snake as defined in the DrawPoly class
 * @author Alexander Schoolcraft
 * @version 02/10/2022 - 1.0 - initial game completed 
 */
public class Csci1302_hw2 {

	public static void main (String[] args) {
		JFrame frame=new JFrame("Snake");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DrawPoly snakes = new DrawPoly();
		frame.getContentPane().add(snakes);
		frame.pack();
		frame.setVisible(true);
	}
}