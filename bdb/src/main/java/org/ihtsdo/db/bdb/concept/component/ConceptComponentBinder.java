package org.ihtsdo.db.bdb.concept.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_BindConceptComponents;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;

public class ConceptComponentBinder<V extends Revision<V, C>, 
									C extends ConceptComponent<V, C>> 
	extends TupleBinding<ArrayList<C>> 
	implements I_BindConceptComponents {


    private static final int maxReadOnlyStatusAtPositionId = Bdb.getSapDb().getReadOnlyMax();
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
		assert enclosingConcept != null: "enclosing concept cannot be null.";
		int listSize = input.readInt();
		assert listSize >= 0: "Processing nid: " + enclosingConcept.getNid() + " listSize: " + listSize +
			"\ndata: " + new DatabaseEntry(input.getBufferBytes()).toString();
		assert listSize < 1000000: "Processing nid: " + enclosingConcept.getNid()  + " listSize: " + listSize +
			"\ndata: " + new DatabaseEntry(input.getBufferBytes()).toString();
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
			// All components must write the nid first...
			input.mark(16);
			int nid = input.readInt();
			// we have to put it back so the component can read it again...
			input.reset();
			C conceptComponent;
			if (nidToConceptComponentMap != null && nidToConceptComponentMap.containsKey(nid)) {
				conceptComponent = nidToConceptComponentMap.get(nid);
				conceptComponent.readComponentFromBdb(input);
			} else {
				conceptComponent = factory.create(enclosingConcept, input);
				newConceptComponentList.add(conceptComponent);
			}
		}
		newConceptComponentList.trimToSize();
		return newConceptComponentList;
	}

	@Override
	public void objectToEntry(ArrayList<C> conceptComponentList, TupleOutput output) {
		List<C> componentListToWrite = new ArrayList<C>(conceptComponentList.size());
		for (C conceptComponent: conceptComponentList) {
			componentsEncountered.incrementAndGet();
			if (conceptComponent.primordialSapNid > maxReadOnlyStatusAtPositionId) {
				componentListToWrite.add(conceptComponent);
			} else {
				if (conceptComponent.revisions != null) {
					for (V part: conceptComponent.revisions) {
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
