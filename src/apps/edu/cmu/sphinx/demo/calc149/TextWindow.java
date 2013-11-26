package edu.cmu.sphinx.demo.calc149;

import javax.swing.*;

public class TextWindow {
	/**
	 * @param args
	 */
	public static void main(String [] args){
		
		JFrame frame = new JFrame("FrameDemo");

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//3. Create components and put them in the frame.
		//...create emptyLabel...
		//frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

		//4. Size the frame.
		//frame.pack();
		frame.getContentPane().setLayout(null);
		
		JButton button = new JButton("Voice Mode");
		button.setSize(100, 30);
		button.setLocation(140, 10);
		
		JTextField textfield = new JTextField();
		textfield.setSize(200, 30);
		textfield.setLocation(10, 100);
		
		JLabel label1 = new JLabel("Please enter your formula here");
		JLabel label2 = new JLabel("For example: 1 + 1 or 3*5");
		
		label1.setSize(200, 20);
		label2.setSize(300, 20);
		label1.setLocation(10, 50);
		label2.setLocation(10, 70);
		
		JButton button2 = new JButton("=");
		button2.setBounds(215, 100, 60, 30);
		
		frame.getContentPane().add(button);
		frame.getContentPane().add(button2);
		frame.getContentPane().add(textfield);
		frame.getContentPane().add(label2);
		frame.getContentPane().add(label1);
		frame.setSize(300,300);
		//5. Show it.
		frame.setVisible(true);
	}
}
