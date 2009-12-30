package org.ihtsdo.db.bdb.concept.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.I_BindConceptComponents;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptComponentBinder<C extends ConceptComponent<P, C>, 
									P extends Version<P, C>> 
	extends TupleBinding<ArrayList<C>> 
	implements I_BindConceptComponents {


    private static int maxReadOnlyStatusAtPositionId = Bdb.getStatusAtPositionDb().getReadOnlyMax();
	private int conceptNid;
	private boolean editable;
	private ArrayList<C> readOnlyConceptComponentList;
	private ComponentFactory<C, P> factory;

	public ConceptComponentBinder(ComponentFactory<C, P> factory) {
		super();
		this.factory = factory;
	}

	@Override
	public ArrayList<C> entryToObject(TupleInput input) {
		int listSize = input.readInt();
		if (readOnlyConceptComponentList != null) {
			readOnlyConceptComponentList.ensureCapacity(listSize + readOnlyConceptComponentList.size());
		}
		ArrayList<C> newConceptComponentList;
		HashMap<Integer, C> nidToConceptComponentMap = null;
		if (readOnlyConceptComponentList != null) {
			newConceptComponentList = readOnlyConceptComponentList;
			nidToConceptComponentMap = new HashMap<Integer, C>(listSize);
			for (C component: readOnlyConceptComponentList) {
				nidToConceptComponentMap.put(component.nid, component);
			}
		} else {
			newConceptComponentList = new ArrayList<C>(listSize);
		}
		for (int index = 0; index < listSize; index++) {
			int nid = input.readInt();
			int partCount = input.readShort();
			C conceptComponent;
			if (nidToConceptComponentMap != null && nidToConceptComponentMap.containsKey(nid)) {
				conceptComponent = nidToConceptComponentMap.get(nid);
				int totalSize = conceptComponent.mutableComponentParts.size() + partCount;
				conceptComponent.mutableComponentParts.ensureCapacity(totalSize);
			} else {
				conceptComponent = factory.create(nid, partCount, editable);
				newConceptComponentList.add(conceptComponent);
			}
			conceptComponent.readComponentFromBdb(input, conceptNid, partCount);
		}
		newConceptComponentList.trimToSize();
		return newConceptComponentList;
	}

	@Override
	public void objectToEntry(ArrayList<C> conceptComponentList, TupleOutput output) {
		List<C> componentListToWrite = new ArrayList<C>(conceptComponentList.size());
		for (C conceptComponent: conceptComponentList) {
			for (P part: conceptComponent.mutableComponentParts) {
				if (part.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId) {
					componentListToWrite.add(conceptComponent);
					break;
				}
			}
		}
		output.writeInt(componentListToWrite.size()); // List size
		for (C conceptComponent: componentListToWrite) {
			conceptComponent.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
		}
	}

	@Override
	public int getConceptNid() {
		return conceptNid;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public void setupBinder(int conceptNid, boolean editable) {
		this.conceptNid = conceptNid;
		this.editable = editable;
		this.readOnlyConceptComponentList = null;
	}

	public void setTermComponentList(ArrayList<C> componentList) {
		this.readOnlyConceptComponentList = componentList;
	}
}
