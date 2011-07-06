package org.dwfa.ace.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.OpenFrames;
/**
 * A shell around SVNPrompter to allow for changes 
 * @author adfl
 *
 */
public class UIAuthenticator {
	
	public File profile;

	//Store UN etc in the SVN Prompter
	public SvnPrompter prompt = new SvnPrompter();
	
	public String baseURL;
	
	private JFrame parentFrame = null;
	private AceProfileManager apm = new AceProfileManager();
	
	public void setParentFrame(JFrame parentFrame) {
		this.parentFrame = parentFrame;
	}


	private JFrame getParentFrame(){
		if(parentFrame == null){
		parentFrame = new JFrame();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		if (OpenFrames.getNumOfFrames() > 0) {
			parentFrame = OpenFrames.getFrames().iterator().next();
			AceLog.getAppLog().info("### Adding an existing frame");
		} else {
			try {
						parentFrame.setContentPane(new JLabel(
								"Bobbin the bob is starting..."));
						parentFrame.pack();
						parentFrame.setVisible(true);
						parentFrame.setLocation((d.width / 2)
								- (parentFrame.getWidth() / 2),
								(d.height / 2)
										- (parentFrame.getHeight() / 2));
						OpenFrames.addFrame(parentFrame);
						AceLog.getAppLog().info("### Using a new frame");
						//newFrame = true;
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			} 
		}
		}
		return parentFrame;
		
	}

	//public String authenticate(SvnHelper svnH){
		public String authenticate(SvnPrompter prompt, String testURL){	
		baseURL = testURL;
		this.prompt = prompt;
		try {
			apm.processProfiles();
			initPrompter();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		AceLog.getAppLog().info("authenticate un "+this.prompt.getUsername()+" pw "+this.prompt.getPassword());
		apm.setUserName(this.prompt.getUsername());
		setProfile(apm.getProfile());
		AceLog.getAppLog().info("authenticate this profile = "+getProfile().getAbsoluteFile());
		
		return "";
	}
	
	private void initPrompter(){
		if(prompt.getUsername() == null || prompt.getUsername().length() == 0){
			AceLog.getAppLog().info("No name found so prompting");
			AceLoginDialog2 ald = new AceLoginDialog2(getParentFrame());
			//ald.setProfile(getProfile());
			ald.setSvnUrl(baseURL);
			ald.setPrompt(prompt);
			ald.setVisible(true);
			//prompt.setParentContainer(getParentFrame());	
			//prompt.prompt("please set password", userName);
			/*try {
				AceLog.getAppLog().info("aaaa profile =  "+ald.getProfile().getName());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		else{
			AceLog.getAppLog().info("Name found "+prompt.getUsername());
		}
		
		
	}
	
	public String testCredentials(){
		String result = "";
		
		
		return result;
	}

	public SvnPrompter getPrompt() {
		return prompt;
	}

	public void setPrompt(SvnPrompter prompt) {
		this.prompt = prompt;
	}


	public File getProfile() {
		if(profile == null){
			profile = new File("");
		}
		return profile;
	}


	public void setProfile(File profile) {
		this.profile = profile;
	}

	

}
