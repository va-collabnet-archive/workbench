package org.dwfa.svn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.DirEntry;
import org.tigris.subversion.javahl.Revision;

public class CheckoutPanel extends JPanel {
	
	private class CheckoutListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			SvnLog.info("checkout");
			try {
				SvnLog.info("recursion: false");
				for (DirEntry entry: Svn.getSvnClient().list(repository.getText(), Revision.HEAD, false)) {
					SvnLog.info("repository entry: " + entry.getPath());
				}
				
				SvnLog.info("recursion: true");
				for (DirEntry entry: Svn.getSvnClient().list(repository.getText(), Revision.HEAD, true)) {
					SvnLog.info("repository entry: " + entry.getPath());
				}
				
				
				Svn.getSvnClient().checkout(repository.getText(),
						workingCopy.getText(), Revision.HEAD, true);
				SvnLog.info("cleanup");
				Svn.getSvnClient().cleanup(workingCopy.getText());
				SvnLog.info("working copy Author: " + Svn.getSvnClient().info(workingCopy.getText()).getAuthor());
				SvnLog.info("working copy CopyRev: " + Svn.getSvnClient().info(workingCopy.getText()).getCopyRev());
				SvnLog.info("working copy CopyUrl: " + Svn.getSvnClient().info(workingCopy.getText()).getCopyUrl());
				SvnLog.info("working copy LastChangedRevision: " + Svn.getSvnClient().info(workingCopy.getText()).getLastChangedRevision());
				SvnLog.info("working copy Name: " + Svn.getSvnClient().info(workingCopy.getText()).getName());
				SvnLog.info("working copy NodeKind: " + Svn.getSvnClient().info(workingCopy.getText()).getNodeKind());
				SvnLog.info("working copy Repository: " + Svn.getSvnClient().info(workingCopy.getText()).getRepository());
				SvnLog.info("working copy Revision: " + Svn.getSvnClient().info(workingCopy.getText()).getRevision());
				SvnLog.info("working copy Schedule: " + Svn.getSvnClient().info(workingCopy.getText()).getSchedule());
				SvnLog.info("working copy Url: " + Svn.getSvnClient().info(workingCopy.getText()).getUrl());
				SvnLog.info("working copy Uuid: " + Svn.getSvnClient().info(workingCopy.getText()).getUuid());
				SvnLog.info("working copy LastChangedDate: " + Svn.getSvnClient().info(workingCopy.getText()).getLastChangedDate());
				SvnLog.info("working copy LastChangedRevision: " + Svn.getSvnClient().info(workingCopy.getText()).getLastChangedRevision());
				SvnLog.info("working copy LastDatePropsUpdate: " + Svn.getSvnClient().info(workingCopy.getText()).getLastDatePropsUpdate());
				SvnLog.info("working copy LastDateTextUpdate: " + Svn.getSvnClient().info(workingCopy.getText()).getLastDateTextUpdate());
			} catch (ClientException e) {
				SvnLog.alertAndLog(e);
			}
		}
	}
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField repository;
	private JTextField workingCopy;

	public CheckoutPanel() {
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		this.add(new JLabel("Repository: "), c);
		c.gridy++;
		this.add(new JLabel("Working copy: "), c);

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		repository = new JTextField("http://svn.apache.org/repos/asf/maven/plugins/trunk/maven-javadoc-plugin");
		this.add(repository, c);
		c.gridy++;
		workingCopy = new JTextField("target/maven-javadoc-plugin");
		this.add(workingCopy, c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		JButton checkout = new JButton("checkout");
		checkout.addActionListener(new CheckoutListener());
		this.add(checkout, c);
		c.gridy++;
		c.gridx = 0;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JPanel(), c);
		
		// TODO Auto-generated constructor stub
	}

}
