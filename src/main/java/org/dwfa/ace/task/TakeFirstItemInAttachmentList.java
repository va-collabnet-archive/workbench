package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN) })
public class TakeFirstItemInAttachmentList extends AbstractTask {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

    private String listName = AttachmentKeys.DEFAULT_CONCEPT_LIST.getAttachmentKey();

    private String conceptKey = AttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();


	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
        out.writeObject(listName);
        out.writeObject(conceptKey);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
            listName = (String) in.readObject();
            conceptKey = (String) in.readObject();
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
            ArrayList<Collection<UUID>> temporaryList =
                (ArrayList<Collection<UUID>>) process.readProperty(listName);

            I_TermFactory termFactory = (I_TermFactory) worker
			.readAttachement(AttachmentKeys.I_TERM_FACTORY.name());

			I_GetConceptData concept = termFactory.getConcept((Collection<UUID>) temporaryList.remove(0));

            process.setProperty(this.conceptKey, concept);
			return Condition.CONTINUE;
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
		} catch (IOException e) {
            throw new TaskFailedException(e);
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getConceptKey() {
        return conceptKey;
    }

    public void setConceptKey(String conceptKey) {
        this.conceptKey = conceptKey;
    }

}
