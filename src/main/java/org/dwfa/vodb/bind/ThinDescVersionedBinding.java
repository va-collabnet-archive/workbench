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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinDescVersionedBinding extends TupleBinding {

    public ThinDescVersioned entryToObject(TupleInput ti) {
        int descId = ti.readInt();
        int conceptId = ti.readInt();
        int size = ti.readInt();
        ThinDescVersioned versioned = new ThinDescVersioned(descId, conceptId, size);
        String lastLang = null;
        String lastText = null;
        for (int x = 0; x < size; x++) {
            ThinDescPart desc = new ThinDescPart();
            desc.setPathId(ti.readInt());
            desc.setVersion(ti.readInt());
            desc.setStatusId(ti.readInt());
            desc.setInitialCaseSignificant(ti.readBoolean());
            int langLength = ti.readInt();
            if (langLength == -1) {
                desc.setLang(lastLang);
            } else {
                String lang = ti.readBytes(langLength);
                desc.setLang(lang);
                lastLang = lang;
            }
            lastText = readDescText(lastText, ti);
            desc.setText(lastText);
            desc.setTypeId(ti.readInt());
            versioned.addVersion(desc);
        }
        return versioned;
    }

    public static String readDescText(String lastText, TupleInput ti) {
        int textLength = ti.readInt();
        if (textLength == -1) {
            return lastText;
        } else {
            byte[] textBytes = new byte[textLength];
            ti.readFast(textBytes);
            String text;
            try {
                text = new String(textBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return text;
        }
    }

    public static byte[] writeDescText(byte[] lastText, I_DescriptionPart desc, TupleOutput to) {
        byte[] text;
        try {
            text = desc.getText().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (Arrays.equals(text, lastText)) {
            to.writeInt(-1);
            return lastText;
        } else {
            to.writeInt(text.length);
            to.writeFast(text);
            return text;
        }
    }

    public void objectToEntry(Object obj, TupleOutput to) {
        I_DescriptionVersioned versioned = (I_DescriptionVersioned) obj;
        to.writeInt(versioned.getDescId());
        to.writeInt(versioned.getConceptId());
        to.writeInt(versioned.versionCount());
        String lastLang = null;
        byte[] lastText = new byte[0];
        for (I_DescriptionPart desc : versioned.getVersions()) {
            to.writeInt(desc.getPathId());
            to.writeInt(desc.getVersion());
            to.writeInt(desc.getStatusId());
            to.writeBoolean(desc.getInitialCaseSignificant());
            String lang = desc.getLang();
            if (lang == lastLang) {
                to.writeInt(-1);
            } else {
                to.writeInt(lang.length());
                to.writeBytes(lang);
                lastLang = lang;
            }
            lastText = writeDescText(lastText, desc, to);
            to.writeInt(desc.getTypeId());
        }
    }
}
