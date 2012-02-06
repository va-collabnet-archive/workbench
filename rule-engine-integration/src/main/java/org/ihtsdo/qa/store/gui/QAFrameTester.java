/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.store.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.ihtsdo.qa.store.QAStoreBIImpl;

/**
 * The Class QAFrameTester.
 */
public class QAFrameTester {
	/*
	* Create the GUI and show it.  For thread safety,
	* this method should be invoked from the
	* event-dispatching thread.
	*/
	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("QA Frame");
		frame.setPreferredSize(new Dimension(1024,768));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(new QAStorePanel(new QAStoreBIImpl()), BorderLayout.CENTER);
		//frame.getContentPane().add(new QAStorePanel(new QAStoreStubImpl()), BorderLayout.CENTER);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
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
