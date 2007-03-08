package org.dwfa.vodb.bind;

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
			String lang = ti.readString();
			if (lang == null) {
				desc.setLang(lastLang);				
			} else {
				desc.setLang(lang);
				lastLang = lang;
			}
			String text = ti.readString();
			if (text == null) {
				desc.setText(lastText);				
			} else {
				desc.setText(text);
				lastText = text;
			}
			desc.setTypeId(ti.readInt());
			versioned.addVersion(desc);
		}
		return versioned;
	}

	public void objectToEntry(Object obj, TupleOutput to) {
		ThinDescVersioned versioned = (ThinDescVersioned) obj;
		to.writeInt(versioned.getDescId());
		to.writeInt(versioned.getConceptId());
		to.writeInt(versioned.versionCount());
		String lastLang = null;
		String lastText = null;
		for (ThinDescPart desc: versioned.getVersions()) {
			to.writeInt(desc.getPathId());
			to.writeInt(desc.getVersion());
			to.writeInt(desc.getStatusId());
			to.writeBoolean(desc.getInitialCaseSignificant());
			String lang = desc.getLang();
			if (lang == lastLang) {
				to.writeString((String) null);
			} else {
				to.writeString(lang);
				lastLang = lang;
			}
			String text = desc.getText();
			if (text == lastText) {
				to.writeString((String) null);
			} else {
				to.writeString(text);
				lastText = text;
			}
			to.writeInt(desc.getTypeId());
		}
	}

}

