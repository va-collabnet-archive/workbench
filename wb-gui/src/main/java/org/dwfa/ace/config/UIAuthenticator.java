package org.dwfa.ace.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private String lastUser;
	
	public final static String ERR_NO_PROFILE_S = "No Profiles folder found";

	
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
			//AceLog.getAppLog().info("### Adding an existing frame");
		}/* else {
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
		} */
		}
		return parentFrame;
		
	}

	//public String authenticate(SvnHelper svnH){
		public String authenticate(SvnPrompter prompt, String testURL,String lastUserProfile){	
		String retVal = null;	
		baseURL = testURL;
		this.prompt = prompt;
		try {
		    if(lastUserProfile != null){
		        apm.setLastUserProfile(lastUserProfile);
		    }  
			apm.processProfiles();
			if(!apm.isProfileFolderFound()){
				retVal = ERR_NO_PROFILE_S;
				String msg = "No Profiles folder found. The application will try and check one out from the repository";
				JOptionPane.showMessageDialog(getParentFrame(),msg , ERR_NO_PROFILE_S, JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try{
			initPrompter();
			apm.setUserName(this.prompt.getUsername());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//AceLog.getAppLog().info("authenticate un "+this.prompt.getUsername()+" pw "+this.prompt.getPassword());
		
		setProfile(apm.getProfile());
		//AceLog.getAppLog().info("authenticate this profile = "+getProfile().getAbsoluteFile());
		
		return retVal;
	}
	
	private void initPrompter(){
		if(prompt.getUsername() == null || prompt.getUsername().length() == 0){
			//AceLog.getAppLog().info("No name found so prompting");
			AceLoginDialog2 ald = new AceLoginDialog2(getParentFrame(),apm.getNameProf(),apm.getLastUser());
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


	public SvnPrompter getPrompt() {
		return prompt;
	}

	public void setPrompt(SvnPrompter prompt) {
		this.prompt = prompt;
	}


	public File getProfile() {
		/*if(profile == null){
			profile = new File("");
		}*/
		return profile;
	}


	public void setProfile(File profile) {
		this.profile = profile;
	}


	public AceProfileManager getApm() {
		return apm;
	}


	public void setApm(AceProfileManager apm) {
		this.apm = apm;
	}


    public String getLastUser() {
        if(lastUser == null){
            return "";
        }
        return lastUser;
    }


    public void setLastUser(String lastUser) {
        this.lastUser = lastUser;
    }

	

}