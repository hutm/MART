package misc;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class JSliderTest implements Runnable, ActionListener, ChangeListener {
	private JSlider slider;
	private Timer timer;
	private boolean timerIsDoingIt;

	public void run() {
		slider = new JSlider(0, 100, 0);
		slider.addChangeListener(this);
		timer = new Timer(200, this);

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(slider);
		f.pack();
		f.setLocationRelativeTo(null);
        f.setVisible(true);

        timer.start();
	}

	public void actionPerformed(ActionEvent evt) {
		timerIsDoingIt = true;
		slider.setValue(1 + slider.getValue());
		timerIsDoingIt = false;
	}

	public void stateChanged(ChangeEvent e) {
		if (!timerIsDoingIt && !slider.getValueIsAdjusting())
			System.out.println("manual adjustment detected");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new JSliderTest());
	}
}
