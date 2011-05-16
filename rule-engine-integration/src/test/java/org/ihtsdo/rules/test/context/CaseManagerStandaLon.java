package org.ihtsdo.rules.test.context;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

import org.ihtsdo.qa.store.QAStoreBIImpl;
import org.ihtsdo.qa.store.gui.QAStorePanel;

public class CaseManagerStandaLon {

	
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
			 e.printStackTrace();
		}
		frame.getContentPane().add(new QAStorePanel(new QAStoreBIImpl(endpoint)), BorderLayout.CENTER);

		//Display the window.
		frame.setTitle("IHTSDO QA Case Manager");
		frame.pack();
		frame.setVisible(true);
	}
}
