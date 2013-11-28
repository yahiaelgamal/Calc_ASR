package edu.cmu.sphinx.demo.calc149;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

public class TextWindow implements MouseListener{
	
	public TextWindow(Calc calcc){
		
        final JFrame frame = new JFrame("Text Mode");  
        final Calc calc = calcc;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
  
        JPanel panel = (JPanel)frame.getContentPane();
        
        panel.setLayout(null);
		
		JButton button = new JButton("Voice Mode");
		button.setSize(100, 30);
		button.setLocation(180, 10);
		button.addActionListener(
					new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		            	
		                    new VoiceWindow(calc);
		                    frame.dispose();
		                    frame.dispose();
		              }
		          }
		      );
		
		final JTextField textfield = new JTextField();
		textfield.setSize(200, 30);
		textfield.setLocation(10, 100);
		
		JLabel label1 = new JLabel("Please enter your formula here");
		JLabel label2 = new JLabel("For example: 1 + 1 or cos 22");
		JLabel resultLabel = new JLabel("Result:");
		final JLabel label3 = new JLabel("");
		
		label1.setSize(200, 20);
		label2.setSize(300, 20);
		label1.setLocation(10, 50);
		label2.setLocation(10, 70);
		resultLabel.setSize(300, 20);
		resultLabel.setLocation(10, 130);
		label3.setSize(300, 20);
		label3.setLocation(10, 160);
		
		JButton button2 = new JButton("=");
		button2.setBounds(215, 100, 60, 30);
		button2.addActionListener(
					new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		            	if(textfield.getText().length() > 0){
		                    calc.doTextStuff(textfield.getText());
		                    label3.setText(calc.result);
		              }
		            }
				}
		      );
		
		panel.add(button);
		panel.add(button2);
		panel.add(textfield);
		panel.add(label2);
		panel.add(label1);
		panel.add(resultLabel);
		panel.add(label3);
		frame.setSize(300,300);
		//5. Show it.
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
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
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
