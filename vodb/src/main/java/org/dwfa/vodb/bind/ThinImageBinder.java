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
package org.dwfa.vodb.bind;

import java.util.ArrayList;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.vodb.types.ThinImagePart;
import org.dwfa.vodb.types.ThinImageVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinImageBinder extends TupleBinding {

    public ThinImageVersioned entryToObject(TupleInput ti) {
        int nativeId = ti.readInt();
        int imageLength = ti.readInt();
        byte[] image = new byte[imageLength];
        ti.readFast(image, 0, imageLength);
        String format = ti.readString();
        int conceptId = ti.readInt();
        int size = ti.readInt();
        ThinImageVersioned versioned = new ThinImageVersioned(nativeId, image, new ArrayList<I_ImagePart>(size),
            format, conceptId);
        for (int x = 0; x < size; x++) {
            ThinImagePart part = new ThinImagePart();
            part.setPathId(ti.readInt());
            part.setVersion(ti.readInt());
            part.setStatusId(ti.readInt());
            part.setTextDescription(ti.readString());
            part.setTypeId(ti.readInt());
            versioned.addVersion(part);
        }
        return versioned;
    }

    public void objectToEntry(Object obj, TupleOutput to) {
        I_ImageVersioned versioned = (I_ImageVersioned) obj;
        to.writeInt(versioned.getImageId());
        byte[] image = versioned.getImage();
        to.writeInt(image.length);
        to.writeFast(image, 0, image.length);
        to.writeString(versioned.getFormat());
        to.writeInt(versioned.getConceptId());
        to.writeInt(versioned.getVersions().size());
        for (I_ImagePart part : versioned.getVersions()) {
            to.writeInt(part.getPathId());
            to.writeInt(part.getVersion());
            to.writeInt(part.getStatusId());
            to.writeString(part.getTextDescription());
            to.writeInt(part.getTypeId());
        }
    }
}
