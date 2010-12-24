package org.ihtsdo.concept.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_BindConceptComponents;
import org.ihtsdo.db.bdb.Bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;

public class ConceptComponentBinder<V extends Revision<V, C>, 
									C extends ConceptComponent<V, C>> 
	extends TupleBinding<Collection<C>> 
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

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<C> entryToObject(TupleInput input) {
		assert enclosingConcept != null: "enclosing concept cannot be null.";
		int listSize = input.readInt();
		assert listSize >= 0: "Processing nid: " + enclosingConcept.getNid() + 
			" listSize: " + listSize +
			"\ndata: " + new DatabaseEntry(input.getBufferBytes()).toString();
		assert listSize < 1000000: "Processing nid: " + enclosingConcept.getNid()  + 
			" listSize: " + listSize +
			"\ndata: " + new DatabaseEntry(input.getBufferBytes()).toString();
		if (readOnlyConceptComponentList != null) {
			readOnlyConceptComponentList.ensureCapacity(listSize + 
					readOnlyConceptComponentList.size());
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
			C conceptComponent = (C) Concept.componentsCRHM.get(nid);
			if (nidToConceptComponentMap != null && 
			        nidToConceptComponentMap.containsKey(nid)) {
				if (conceptComponent == null) {
					conceptComponent = nidToConceptComponentMap.get(nid);
					C oldComponent = (C) Concept.componentsCRHM.putIfAbsent(conceptComponent.nid, conceptComponent);
					if (oldComponent != null) {
						conceptComponent = oldComponent;
						if (nidToConceptComponentMap != null) {
							nidToConceptComponentMap.put(nid, oldComponent);
						}
					}
				}
 				try {
					conceptComponent.merge(factory.create(enclosingConcept, input));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
 				try {
					if (conceptComponent == null) {
						conceptComponent = factory.create(enclosingConcept, input);
						if (conceptComponent.getTime() != Long.MIN_VALUE) {
	                        C oldComponent = (C) Concept.componentsCRHM.putIfAbsent(conceptComponent.nid, conceptComponent);
	                        if (oldComponent != null) {
	                            conceptComponent = oldComponent;
	                            if (nidToConceptComponentMap != null) {
	                                nidToConceptComponentMap.put(nid, oldComponent);
	                            }
	                        }
						} else {
                            AceLog.getAppLog().warning("\n########## Suppressing concept component:\n     " + conceptComponent +
						        "\n##########" );
						}
					} else {
					    conceptComponent.merge(factory.create(enclosingConcept, input));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				if (conceptComponent.getTime() != Long.MIN_VALUE) {
					newConceptComponentList.add(conceptComponent);
				}
			}
		}
		newConceptComponentList.trimToSize();
		return newConceptComponentList;
	}

	@Override
	public void objectToEntry(Collection<C> conceptComponentList, TupleOutput output) {
		List<C> componentListToWrite = new ArrayList<C>(conceptComponentList.size());
		for (C conceptComponent: conceptComponentList) {
			componentsEncountered.incrementAndGet();
			if (conceptComponent.primordialSapNid > maxReadOnlyStatusAtPositionId &&
					conceptComponent.getTime() != Long.MIN_VALUE) {
				componentListToWrite.add(conceptComponent);
			} else {
				if (conceptComponent.revisions != null) {
					for (V part: conceptComponent.revisions) {
						assert part.getStatusAtPositionNid() != Integer.MAX_VALUE;
						if (part.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionId &&
								part.getTime() != Long.MIN_VALUE) {
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
