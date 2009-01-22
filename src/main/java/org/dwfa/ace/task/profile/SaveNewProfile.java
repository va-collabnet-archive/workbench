package org.dwfa.ace.task.profile;

import java.beans.IntrospectionException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.svn.AddSubversionEntry;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;

@BeanList(specs = { @Spec(directory = "tasks/ace/profile", type = BeanType.TASK_BEAN) })
public class SaveNewProfile extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame currentProfile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            I_ConfigAceDb currentDbProfile = currentProfile.getDbConfig();
            File creatorsProfileFile = currentDbProfile.getProfileFile();
            String repositoryUrlStr = null;
            String workingCopyStr = FileIO.getNormalizedRelativePath(creatorsProfileFile);
            SubversionData creatorSvd = new SubversionData(repositoryUrlStr, workingCopyStr);
            currentProfile.svnCompleteRepoInfo(creatorSvd);
            String sequenceToFind = "src/main/profiles/";
            int sequenceLocation = creatorSvd.getRepositoryUrlStr().indexOf(sequenceToFind);
            int sequenceEnd = sequenceLocation + sequenceToFind.length();
            
            I_ImplementTermFactory termFactory = (I_ImplementTermFactory) LocalVersionedTerminology.get();
            
            I_ConfigAceFrame profileToSave = (I_ConfigAceFrame) process.readProperty(profilePropName);
            String profileDirRepoUrl = creatorSvd.getRepositoryUrlStr().substring(0, sequenceEnd);
            String userDirRepoUrl = profileDirRepoUrl + profileToSave.getUsername();
            I_ConfigAceDb newDbProfile = termFactory.newAceDbConfig();
            profileToSave.setDbConfig(newDbProfile);
            String userDirStr = "profiles" + File.separator  + profileToSave.getUsername();
            File userDir = new File(userDirStr);
            File changeSetRoot = new File(userDir, "changesets");
            changeSetRoot.mkdirs();
            File profileFile = new File(userDir, profileToSave.getUsername() + ".ace");
            
            newDbProfile.getAceFrames().clear();
            newDbProfile.getAceFrames().add(profileToSave);
            newDbProfile.setCacheSize(currentDbProfile.getCacheSize());
            newDbProfile.setChangeSetRoot(changeSetRoot);
            newDbProfile.setChangeSetWriterFileName(profileToSave.getUsername() + "." + 
            		UUID.randomUUID().toString() + ".jcs");
            newDbProfile.setDbFolder(currentDbProfile.getDbFolder());
            newDbProfile.setProfileFile(profileFile);
            newDbProfile.setUsername(profileToSave.getUsername());
            
          //Custom button text
            Object[] options = {"Synchronize profile directory", // yes option
                                "Synchronize only user profile", // no option
                                "Cancel"}; // cancel option
            int n = JOptionPane.showOptionDialog(null,
                "Would you like some green eggs to go "
                + "with that ham?",
                "Select Subversion setup for profiles",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
            
            if (n == JOptionPane.YES_OPTION) {
            	
                AddSubversionEntry addUserProfileTask = new AddSubversionEntry();
                addUserProfileTask.setKeyName("profile");
                addUserProfileTask.setProfilePropName(profilePropName);
                addUserProfileTask.setPrompt("verify subversion settings for profile directory: ");
                addUserProfileTask.setRepoUrl(profileDirRepoUrl);
                addUserProfileTask.setWorkingCopy("profiles" + File.separator);
                addUserProfileTask.evaluate(process, worker);
            	
            } else if (n == JOptionPane.NO_OPTION) {
                // Create a new svn profile for the user
                
                AddSubversionEntry addUserProfileTask = new AddSubversionEntry();
                addUserProfileTask.setKeyName("profile");
                addUserProfileTask.setProfilePropName(profilePropName);
                addUserProfileTask.setPrompt("verify subversion settings for user profile: ");
                addUserProfileTask.setRepoUrl(userDirRepoUrl);
                addUserProfileTask.setWorkingCopy(userDirStr);
                addUserProfileTask.evaluate(process, worker);
                
            } else {
            	throw new TaskFailedException("user canceled operation");
            }
            
            // Crate a new svn profile for the database if it has a .svn folder
            File databaseSvn = new File(currentDbProfile.getDbFolder(), ".svn");
            if (databaseSvn.exists()) {
                SubversionData databaseSvd = new SubversionData(repositoryUrlStr, FileIO.getNormalizedRelativePath(currentDbProfile.getDbFolder()));
                currentProfile.svnCompleteRepoInfo(databaseSvd);
                AddSubversionEntry addDatabaseProfileTask = new AddSubversionEntry();
                addDatabaseProfileTask.setKeyName("database");
                addDatabaseProfileTask.setProfilePropName(profilePropName);
                addDatabaseProfileTask.setPrompt("verify subversion settings for database: ");
                addDatabaseProfileTask.setRepoUrl(databaseSvd.getRepositoryUrlStr());
                addDatabaseProfileTask.setWorkingCopy(databaseSvd.getWorkingCopyStr());
                addDatabaseProfileTask.evaluate(process, worker);
            }
            
            // write to disk
            FileOutputStream fos = new FileOutputStream(profileFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(newDbProfile);
            oos.close();
              
            // import, delete, then checkout...
            SubversionData userSvd = new SubversionData(userDirRepoUrl, userDirStr);
            currentProfile.svnImport(userSvd);
            //
            FileIO.recursiveDelete(new File(userDirStr));
            currentProfile.svnCheckout(userSvd);
            
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } 
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }
}
