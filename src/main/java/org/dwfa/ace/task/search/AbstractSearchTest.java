package org.dwfa.ace.task.search;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;


public abstract class AbstractSearchTest extends AbstractTask implements I_TestSearchResults {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    

    /**
     * Property name for the term component to test. 
     */
    private String componentPropName = ProcessAttachmentKeys.SEARCH_TEST_ITEM.getAttachmentKey();
    
    /**
     * Profile to use for determining view paths, status values. 
     */
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

	/**
	 * Attribute that indicates if the search criteria should be inverted
	 */
	protected boolean inverted = false;


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.componentPropName);
        out.writeObject(this.profilePropName);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.componentPropName = (String) in.readObject();
            this.profilePropName = (String) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }
    }

    public final void complete(I_EncodeBusinessProcess bp, I_Work worker) throws TaskFailedException {
        // nothing to do..
    }

    public final Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        
        try {
            I_AmTermComponent component = (I_AmTermComponent) process.readProperty(componentPropName);
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.readProperty(profilePropName);
            if (test(component, config)) {
                return Condition.TRUE;
            }
            return Condition.FALSE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.dwfa.ace.task.search.I_TestSearchResults#test(org.dwfa.ace.api.I_AmTermComponent)
     */
    public abstract boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException;

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS_REVERSE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getComponentPropName() {
        return componentPropName;
    }

    public void setComponentPropName(String componentPropName) {
        this.componentPropName = componentPropName;
    }

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	/**
	 * Applies the inversion attribute of this class to the specified result.
	 * 
	 * @param b
	 * @return if inverted==true then the inverse of the passed boolean value, 
	 * otherwise the unmodified passed boolean value
	 */
	protected boolean applyInversion(boolean b) {
		if (inverted) {
			return ! b;
		} else {
			return b;
		}
	}

}
