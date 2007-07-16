package org.dwfa.ace.task;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN)})
/**
 * Get the potential duplicate detail Html file directory, and potential dup uuid from properties and set detail uuid file name property
 * @author Susan Castillo
 *
 */
public class GetDetailHtmlDirUuidSetHtmlFileName extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    
    private String detailHtmlFileNameProp = ProcessAttachmentKeys.DETAIL_HTML_FILE.getAttachmentKey();
 	private String potUuidListPropName = ProcessAttachmentKeys.DUP_UUID_LIST.getAttachmentKey();
	private String htmlDirPropName = ProcessAttachmentKeys.DETAIL_HTML_DIR.getAttachmentKey();
	
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(detailHtmlFileNameProp);
        out.writeObject(potUuidListPropName);
        out.writeObject(htmlDirPropName);
 
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
          	detailHtmlFileNameProp = (String) in.readObject();
          	potUuidListPropName = (String) in.readObject();
          	htmlDirPropName = (String) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public GetDetailHtmlDirUuidSetHtmlFileName() throws MalformedURLException {
        super();
    }

    @SuppressWarnings("unchecked")
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {    	
			List<UUID> potDupUuidList = (List<UUID>) process.readProperty(potUuidListPropName);
			UUID firstUuid = potDupUuidList.get(0);
			worker.getLogger().info("potDupUuidList uuid is: " + potDupUuidList);
			
			String htmlDirStr = (String) process.readProperty(htmlDirPropName);
			worker.getLogger().info("htmldir is: " +htmlDirPropName);
			
			String detailUuidInfoFile = htmlDirStr + firstUuid +".html";
			worker.getLogger().info("detailUuidInfoFile is: " +detailUuidInfoFile);
			if (htmlDirStr.endsWith(File.separator) == false) {
				detailUuidInfoFile = htmlDirStr + File.separator + firstUuid +".html";
			}
			
			process.setProperty(this.detailHtmlFileNameProp, detailUuidInfoFile);
			worker.getLogger().info("detailHtmlFileNameProp is: " +detailHtmlFileNameProp);
			String testFileName = (String) process.readProperty(detailHtmlFileNameProp);
			worker.getLogger().info("testFileName is: " +testFileName);
			
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {  };
    }

	public String getDetailHtmlFileNameProp() {
		return detailHtmlFileNameProp;
	}

	public void setDetailHtmlFileNameProp(String detailHtmlFileNameProp) {
		this.detailHtmlFileNameProp = detailHtmlFileNameProp;
	}

	public String getHtmlDirPropName() {
		return htmlDirPropName;
	}

	public void setHtmlDirPropName(String htmlDirPropName) {
		this.htmlDirPropName = htmlDirPropName;
	}

	public String getPotUuidListPropName() {
		return potUuidListPropName;
	}

	public void setPotUuidListPropName(String potUuidListPropName) {
		this.potUuidListPropName = potUuidListPropName;
	}



}
