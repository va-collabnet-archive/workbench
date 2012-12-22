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
package org.ihtsdo.document;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * The Class TestHl.
 */
public class TestHl {

	/**
	 * The main method.
	 *
	 * @param argv the arguments
	 */
	public static void main(String[] argv) {  
		JEditorPane jep = new JEditorPane("text/html", "The rain in <a href='http://foo.com/'>"  
				+"Spain</a> falls mainly on the <a href='http://bar.com/'>plain</a>.");  
		jep.setEditable(false);  
		jep.setOpaque(false);  
		jep.addHyperlinkListener(new HyperlinkListener() {  
			public void hyperlinkUpdate(HyperlinkEvent hle) {  
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {  
					System.out.println(hle.getURL());  
				}  
			}  
		});  

		JPanel p = new JPanel();  
		p.add( new JLabel("Foo.") );  
		p.add( jep );  
		p.add( new JLabel("Bar.") );  

		JFrame f = new JFrame("HyperlinkListener");  
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		f.getContentPane().add(p, BorderLayout.CENTER);  
		f.setSize(400, 150);  
		f.setVisible(true);  
	}  

}
