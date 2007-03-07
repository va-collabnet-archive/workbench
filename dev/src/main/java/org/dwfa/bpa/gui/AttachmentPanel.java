/*
 * Created on Jun 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.MarshalledObject;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.util.FrameWithOpenFramesListener;


/**
 * @author kec
 *
 */
public class AttachmentPanel extends JPanel implements ActionListener {
	/**
     * 
     */
    private static final long serialVersionUID = -4198350907660414054L;
    private String key;
    private I_EncodeBusinessProcess process;
    private JButton openButton = new JButton("open");
	/**
	 * @param key
	 * @param process
	 */
	public AttachmentPanel(String key, I_EncodeBusinessProcess process) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
		this.key = key;
        this.process = process;
        this.setBorder(BorderFactory.createTitledBorder("Attachment: " + this.key));
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        this.openButton.addActionListener(this);
        this.add(openButton, c);
        c.gridx++;
        this.add(new JLabel(this.process.readAttachement(key).getClass().getName()), c);
        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        this.add(new JPanel(), c);

    }
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		try {
			this.open(this.process.readAttachement(this.key));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @param object
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void open(Object object) throws IOException, ClassNotFoundException {
		if (MarshalledObject.class.isAssignableFrom(object.getClass())) {
			MarshalledObject marshalledObj = (MarshalledObject) object;
            this.open(marshalledObj.get());//recursive call
        } else if (I_EncodeBusinessProcess.class.isAssignableFrom(object.getClass())) {
            I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) object;
            try {
                ProcessPanel panel = new ProcessPanel(process, null);
                new FrameWithOpenFramesListener("Attached Process: " + process.getName(), "Attachment", new JScrollPane(panel));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JEditorPane textPane = new JEditorPane();
            textPane.setText(object.toString());
            textPane.setEditable(false);
            try {
				new FrameWithOpenFramesListener("Attached Object: " + process.getName(), "Attachment", new JScrollPane(textPane));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
		
	}

}
