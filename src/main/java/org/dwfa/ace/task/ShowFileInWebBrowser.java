package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.PlatformWebBrowser;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Displays an attachment in a web browser
 * @author Susan Castillo
 *
 */

@BeanList(specs = { @Spec(directory = "tasks/ace/dups", type = BeanType.TASK_BEAN) })
public class ShowFileInWebBrowser extends AbstractTask {

	
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;
	
	private String reasonForDupHtmlStrPropName = ProcessAttachmentKeys.HTML_STR.getAttachmentKey();
    
    private String uuidStrPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();
    

        private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(reasonForDupHtmlStrPropName);
		out.writeObject(uuidStrPropName);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			reasonForDupHtmlStrPropName = (String) in.readObject();
			uuidStrPropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			
			String uuidString = (String) process.readProperty(uuidStrPropName);
			worker.getLogger().info("STR uuid is: " + uuidString);
			File htmlFile = new File("temp", uuidString + ".html");
			worker.getLogger().info("htmlfile is: " + htmlFile);
			URL fileUrl = htmlFile.toURL();
			
			String htmlData = (String) process.readProperty(reasonForDupHtmlStrPropName);
			worker.getLogger().info("STR html is: " + htmlData);
			
			worker.getLogger().info("URL: " + fileUrl.toString());
	      	PlatformWebBrowser.openURL(fileUrl);
			return Condition.CONTINUE;
			
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (MalformedURLException e) {
			throw new TaskFailedException(e);
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getReasonForDupHtmlStrPropName() {
		return reasonForDupHtmlStrPropName;
	}

	public void setReasonForDupHtmlStrPropName(String reasonForDupHtmlStr) {
		this.reasonForDupHtmlStrPropName = reasonForDupHtmlStr;
	}

	public String getUuidStrPropName() {
		return uuidStrPropName;
	}

	public void setUuidStrPropName(String uuidStrPropName) {
		this.uuidStrPropName = uuidStrPropName;
	}

}
