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
package org.ihtsdo.rules.test.context;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.qa.store.QAStoreBIImpl;
import org.ihtsdo.qa.store.gui.QAStorePanel;

/**
 * The Class CaseManagerStandaLon.
 */
public class CaseManagerStandaLon {

	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("QA Frame");
		frame.setPreferredSize(new Dimension(1024,768));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Properties props = new Properties();
		String endpoint = null;
		try{
			props.load(new FileInputStream("rules/qa_ws.properties"));
			endpoint = props.getProperty("ws_endpoint");
		}catch (IOException e) {
			 AceLog.getAppLog().alertAndLogException(e);
		}
		frame.getContentPane().add(new QAStorePanel(new QAStoreBIImpl(endpoint)), BorderLayout.CENTER);

		//Display the window.
		frame.setTitle("IHTSDO QA Case Manager");
		frame.pack();
		frame.setVisible(true);
	}
}
