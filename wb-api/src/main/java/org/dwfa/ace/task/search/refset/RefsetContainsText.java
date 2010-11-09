package org.dwfa.ace.task.search.refset;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class RefsetContainsText extends AbstractSearchTest {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    
    private String text = "text";

	private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(text);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        	text = in.readUTF();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
	public boolean test(I_AmTermComponent component,
			I_ConfigAceFrame frameConfig) throws TaskFailedException {
		if (I_ExtendByRefVersion.class.isAssignableFrom(component.getClass())) {
			I_ExtendByRefVersion extV = (I_ExtendByRefVersion) component;
			String extStr = extV.toString().toLowerCase();
			return extStr.contains(text);
		}
		return false;
	}


    public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}


}
