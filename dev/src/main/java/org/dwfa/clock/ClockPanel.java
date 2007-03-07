package org.dwfa.clock;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

public class ClockPanel extends JLabel {

	private class UpdateListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			repaint();
			try {
				setText(dateFormat.format(timeKeeper.getTime()));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yy h:mm:ss a z");
	private I_KeepTime timeKeeper = new SystemTime();
	
	public ClockPanel() {
		super(dateFormat.format(new Date()));
		this.setOpaque(true);
		this.setBackground(Color.blue);
		this.setForeground(Color.white);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		new Timer(1000, new UpdateListener()).start();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		ClockPanel clock = new ClockPanel();
		clock.setTimeKeeper(new AcceleratedTime(100));
		frame.setContentPane(clock);
		frame.pack();
		frame.setVisible(true);
	}

	public I_KeepTime getTimeKeeper() {
		return timeKeeper;
	}

	public void setTimeKeeper(I_KeepTime timeKeeper) {
		this.timeKeeper = timeKeeper;
	}
}
