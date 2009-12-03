package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Serializable version of SnoCon to support file I/O.
 * Serialization is not put in SnoCon to minimize the SnoCon memory.
 * 
 * @author Marc E. Campbell
 *
 */

public class SnoConSer extends SnoCon implements Serializable {
    private static final long serialVersionUID = 1L;

    public SnoConSer(int id, boolean isDefined) {
        this.id = id;
        this.isDefined = isDefined;
    }

    // customization to handle non-serializable superclass.
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(id);
        oos.writeBoolean(isDefined);
    }

    // customization to handle non-serializable superclass.
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        id = ois.readInt();
        isDefined = ois.readBoolean();
    }
}
