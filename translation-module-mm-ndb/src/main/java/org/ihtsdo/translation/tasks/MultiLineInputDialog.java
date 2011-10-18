package org.ihtsdo.translation.tasks;

import javax.swing.JOptionPane;

import org.ihtsdo.translation.MultilinePopUpDialog;

class MultiLineInputDialog extends JOptionPane
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
