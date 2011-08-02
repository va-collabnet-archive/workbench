package org.dwfa.ace.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

public class AceProfileManager {
	
    private String profileDirName = "profiles";
    private String profileFileEnding= ".ace";
    private Hashtable<String,File> nameProf = new Hashtable<String,File>();
    private File profile;
    private File profileDir;
    
    private String userName;
    
    private boolean profileFolderFound = false;
    
    
    public void processProfiles() throws Exception {
    	setProfiles();
    }
    
    private void setProfiles() throws Exception {
    	try {
    	getProfiles(getProfileDir());
    	} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLog(Level.SEVERE, e.getMessage(),e);	
			throw e;
		}
    }
    
    private void getProfiles(File dir) {
        if (dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    getProfiles(f);
                } else if (f.getName().toLowerCase().endsWith(profileFileEnding)) {
                	nameProf.put(getUserNameFromProfileFN(f.getName()), f);
                   // AceLog.getAppLog().info("getProfiles adding "+f.getName() + " user name = "+getUserNameFromProfileFN(f.getName()));
                }
            }
        }
    }
    
    
    private String getUserNameFromProfileFN(String proFN){
    	int i = proFN.indexOf(profileFileEnding);
    	return proFN.substring(0,i);	
    }
    
	public String getProfileDirName() {
		return profileDirName;
	}
	public void setProfileDirName(String profileDirName) {
		this.profileDirName = profileDirName;
	}
	public String getProfileFileEnding() {
		return profileFileEnding;
	}
	public void setProfileFileEnding(String profileFileEnding) {
		this.profileFileEnding = profileFileEnding;
	}
	public Hashtable<String, File> getNameProf() {
		return nameProf;
	}
	public void setNameProf(Hashtable<String, File> nameProf) {
		this.nameProf = nameProf;
	}
	public File getProfile() {
		if(getUserName() != null && getUserName().length() > 0){
			//AceLog.getAppLog().info("getProfile nameProf size = "+nameProf.size() +" prompt.getUsername()) "+getUserName());
        	if(nameProf.get(getUserName()) != null){
        		profile = nameProf.get(getUserName());
        		//AceLog.getAppLog().info("getProfile profile = "+profile.getName());
        	}
		}
		return profile;
	}
	public void setProfile(File profile) {
		this.profile = profile;
	}

	public File getProfileDir() throws FileNotFoundException {

		profileDir = new File(profileDirName);
		String e_msg = null;
		if (!profileDir.exists()) {
			e_msg = "Profile Directory does not exist";
		} else {
			if (!profileDir.isDirectory()) {
				e_msg = "Profile Directory exists but is not a directory";
			} else {
				if (!profileDir.canRead()) {
					e_msg = "Profile Directory exists but can't be read";
				}
			}
		}
		if (e_msg != null) {
			throw new FileNotFoundException(e_msg + " Profile Directory = "
					+ profileDir.getAbsolutePath());
		}

		profileFolderFound = true;
		return profileDir;
	}
	public void setProfileDir(File profileDir) {
		this.profileDir = profileDir;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isProfileFolderFound() {
		return profileFolderFound;
	}

	public void setProfileFolderFound(boolean profileFolderFound) {
		this.profileFolderFound = profileFolderFound;
	}
    

}
