/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        id = ois.readInt();
        isDefined = ois.readBoolean();
    }
}
