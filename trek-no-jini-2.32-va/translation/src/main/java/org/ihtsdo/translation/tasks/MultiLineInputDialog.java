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
package org.ihtsdo.translation.tasks;

import javax.swing.JOptionPane;

import org.ihtsdo.translation.MultilinePopUpDialog;

/**
 * The Class MultiLineInputDialog.
 */
class MultiLineInputDialog extends JOptionPane
{
  
  /** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

/**
 * Show input dialog.
 *
 * @param message the message
 * @return the string
 */
public String showInputDialog(final String message)
  {
    String data = null;
//    class GetData extends JDialog implements ActionListener
//    {
//      JTextArea ta = new JTextArea(5,10);
//      JButton btnOK = new JButton("   OK   ");
//      JButton btnCancel = new JButton("Cancel");
//      String str = null;
//      public GetData()
//      {
//        setModal(true);
//        getContentPane().setLayout(new BorderLayout());
//        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
//        setLocation(400,300);
//        getContentPane().add(new JLabel(message),BorderLayout.NORTH);
//        getContentPane().add(ta,BorderLayout.CENTER);
//        JPanel jp = new JPanel();
//        btnOK.addActionListener(this);
//        btnCancel.addActionListener(this);
//        jp.add(btnOK);
//        jp.add(btnCancel);
//        btnCancel.setVisible(false);
//        getContentPane().add(jp,BorderLayout.SOUTH);
//        pack();
//        setVisible(true);
//      }
//      public void actionPerformed(ActionEvent ae)
//      {
//        if(ae.getSource() == btnOK) str = ta.getText();
//        dispose();
//      }
//      public String getData(){return str;}
//    }
//    data = new GetData().getData();
    data = new MultilinePopUpDialog(message).showDialog();
    return data;
  }
}
