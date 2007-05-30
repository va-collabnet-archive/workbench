package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.LinkedList;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


@BeanList(specs = { @Spec(directory = "tasks/ace", type = BeanType.TASK_BEAN) })
public class NewAttachmentListFromFile extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String fileName = "C:/working/au-ct/change-sets/target/classes/change-sets/Concepts_modified.txt";

    private String listName = ProcessAttachmentKeys.DEFAULT_CONCEPT_LIST.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(fileName);
        out.writeObject(listName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            fileName = (String) in.readObject();
            listName = (String) in.readObject();
        } else {
            throw new IOException(
                    "Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
                         throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
                                throws TaskFailedException {
        try {

            ArrayList<Collection<UUID>> temporaryList
                    = new ArrayList<Collection<UUID>>();
            System.out.println("File: " + fileName);

            BufferedReader in = new BufferedReader(new FileReader(fileName));

            String currentLine = in.readLine();
            while (currentLine != null) {
                LinkedList<UUID> list = new LinkedList<UUID>();
                list.add(UUID.fromString(currentLine));
                temporaryList.add(list);
                currentLine = in.readLine();
            }

            process.setProperty(this.listName, temporaryList);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }
}
