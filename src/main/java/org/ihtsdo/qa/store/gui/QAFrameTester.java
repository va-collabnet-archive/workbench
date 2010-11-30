package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.ihtsdo.qa.store.QAStoreStubImpl;

public class QAFrameTester {
	/*
	* Create the GUI and show it.  For thread safety,
	* this method should be invoked from the
	* event-dispatching thread.
	*/
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("QA Frame");
		frame.setPreferredSize(new Dimension(1024,768));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(new QAStorePanel(new QAStoreStubImpl()), BorderLayout.CENTER);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
