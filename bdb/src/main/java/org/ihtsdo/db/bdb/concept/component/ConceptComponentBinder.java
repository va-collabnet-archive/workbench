package org.ihtsdo.db.bdb.concept.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_BindConceptComponents;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ConceptComponentBinder<V extends Version<V, C>, 
									C extends ConceptComponent<V, C>> 
	extends TupleBinding<ArrayList<C>> 
	implements I_BindConceptComponents {


    private static int maxReadOnlyStatusAtPositionId = Bdb.getStatusAtPositionDb().getReadOnlyMax();
	private Concept enclosingConcept;
	private ArrayList<C> readOnlyConceptComponentList;
	private ComponentFactory<V, C> factory;
	private AtomicInteger componentsEncountered;
	private AtomicInteger componentsWritten;

	public ConceptComponentBinder(ComponentFactory<V, C> factory, 
								  AtomicInteger componentsEncountered, 
								  AtomicInteger componentsWritten) {
		super();
		this.factory = factory;
		this.componentsEncountered = componentsEncountered;
		this.componentsWritten = componentsWritten;
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
			UUID primordialUuid = new UUID(input.readLong(), input.readLong());
			int partCount = input.readShort();
			C conceptComponent;
			if (nidToConceptComponentMap != null && nidToConceptComponentMap.containsKey(nid)) {
				conceptComponent = nidToConceptComponentMap.get(nid);
				int totalSize = conceptComponent.additionalVersions.size() + partCount;
				conceptComponent.additionalVersions.ensureCapacity(totalSize);
			} else {
				conceptComponent = factory.create(nid, partCount, enclosingConcept, input, primordialUuid);
				newConceptComponentList.add(conceptComponent);
			}
			conceptComponent.readComponentFromBdb(input, partCount);
		}
		newConceptComponentList.trimToSize();
		return newConceptComponentList;
	}

	@Override
	public void objectToEntry(ArrayList<C> conceptComponentList, TupleOutput output) {
		List<C> componentListToWrite = new ArrayList<C>(conceptComponentList.size());
		for (C conceptComponent: conceptComponentList) {
			componentsEncountered.incrementAndGet();
			if (conceptComponent.primordialStatusAtPositionNid > maxReadOnlyStatusAtPositionId) {
				componentListToWrite.add(conceptComponent);
			} else {
				if (conceptComponent.additionalVersions != null) {
					for (V part: conceptComponent.additionalVersions) {
						assert part.getStatusAtPositionNid() != Integer.MAX_VALUE;
						if (part.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId) {
							componentListToWrite.add(conceptComponent);
							break;
						}
					}
				}
			}
		}
		output.writeInt(componentListToWrite.size()); // List size
		for (C conceptComponent: componentListToWrite) {
			componentsWritten.incrementAndGet();
			conceptComponent.writeComponentToBdb(output, maxReadOnlyStatusAtPositionId);
		}
	}

	@Override
	public Concept getEnclosingConcept() {
		return enclosingConcept;
	}

	@Override
	public void setupBinder(Concept enclosingConcept) {
		this.enclosingConcept = enclosingConcept;
		this.readOnlyConceptComponentList = null;
	}

	public void setTermComponentList(ArrayList<C> componentList) {
		this.readOnlyConceptComponentList = componentList;
	}
}
