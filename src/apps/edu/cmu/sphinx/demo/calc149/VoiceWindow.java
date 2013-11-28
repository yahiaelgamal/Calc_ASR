package edu.cmu.sphinx.demo.calc149;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class VoiceWindow {
	public VoiceWindow(Calc calcc){
		final Calc calc = calcc;
		final JFrame frame = new JFrame("Voice Mode");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		  
        JPanel panel = (JPanel)frame.getContentPane();
        
        panel.setLayout(null);
		
		JButton button = new JButton("Text Mode");
		button.setSize(100, 30);
		button.setLocation(180, 10);
		button.addActionListener(
					new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                    new TextWindow(calc);
		                    frame.dispose();
		                    frame.dispose();
		              }
		          }
		      );
		
		JLabel label1 = new JLabel("Press the button and start speaking!");
		JLabel label2 = new JLabel("Say for example: one plus one or sine pie");
		JLabel resultLabel = new JLabel("You said");
		final JLabel you = new JLabel("");
		JLabel r = new JLabel("result:");
		final JLabel label3 = new JLabel("");
		
		label1.setSize(300, 20);
		label2.setSize(300, 20);
		label1.setLocation(10, 50);
		label2.setLocation(10, 70);
		resultLabel.setSize(300, 20);
		you.setLocation(10, 130);
		r.setSize(300, 20);
		r.setLocation(10, 160);
		label3.setSize(300, 20);
		label3.setLocation(10, 185);
		you.setSize(300, 20);
		resultLabel.setLocation(10, 100);
		
		JButton button2 = new JButton("Start");
		button2.setBounds(200, 100, 80, 30);
		button2.addActionListener(
					new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		            	calc.listenOnce();
		            	you.setText(calc.recognizedString);
		            	label3.setText(calc.result);
		            }
				}
		      );
		
		panel.add(button);
		panel.add(button2);
		panel.add(label2);
		panel.add(label1);
		panel.add(you);
		panel.add(resultLabel);
		panel.add(label3);
		panel.add(r);
		panel.add(button);
		frame.setSize(300,300);
		//5. Show it.
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

