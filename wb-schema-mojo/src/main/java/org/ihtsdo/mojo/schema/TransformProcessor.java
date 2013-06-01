/*
 * 
 */
package org.ihtsdo.mojo.schema;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.dto.concept.TkConcept;

/**
 * The Class TransformProcessor.<br>
 * This class handles the iteration through the database, invokes the transformer for each concept and writes the result to a data output stream.
 */
public class TransformProcessor implements ProcessUnfetchedConceptDataBI{

	/** The i. */
	AtomicInteger i = new AtomicInteger();

	/** The all concepts. */
	NidBitSetBI allConcepts;

	/** The transformer. */
	AbstractTransformer transformer;

	/** The e concept dos. */
	DataOutputStream eConceptDOS;

	/** The concept count. */
	private transient AtomicInteger conceptCount = new AtomicInteger();

	/** The write semaphore. */
	private final Semaphore writeSemaphore = new Semaphore(1);

	/** The watch uuids. */
	private Set<UUID> watchUuids = new HashSet<>();

	/**
	 * Instantiates a new transform processor.
	 *
	 * @param transformer the transformer
	 * @param eConceptDOS the e concept dos
	 */
	public TransformProcessor(AbstractTransformer transformer, DataOutputStream eConceptDOS) {
		try {
                    allConcepts = Ts.get().getAllConceptNids();
                    this.transformer = transformer;
                    this.eConceptDOS = eConceptDOS;
                    // <!-- Path origin reference set -->
                    // watchUuids.add(UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b"));
                    // <!-- Path reference set -->
                    // watchUuids.add(UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f"));
                                
                } catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
	 */
	@Override
	public boolean continueWork() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
	 */
	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return allConcepts;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
	 */
	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
		I_GetConceptData c = (I_GetConceptData) fetcher.fetch();
		TkConcept eC;
		try {
			boolean watch = watchUuids.contains(c.getPrimUuid());
			long ini = Calendar.getInstance().getTimeInMillis();

			if (watch) {
                             System.out.println("Watch concept found: " + c);
                             System.out.println("\n\nLong form: " + c.toLongString());
			}
			eC = new EConcept(c);
			if (watch) {
                             System.out.println("\n\nExternal form: " + eC.toString());
			}
			long elapsed = Calendar.getInstance().getTimeInMillis() - ini;

			if (elapsed > 1000) {
				System.out.println("Abnormally long eConcept conversion: " + c.toString() + " - " + c.getPrimUuid() + " (" + elapsed/1000 + " seconds)");
			}

			boolean shouldWrite = transformer.transform(eC);
                        if (watch) {
                            System.out.println("\nwrite: " + shouldWrite);
                            System.out.println("\n\nTransformed: " + eC.toString());
			
                        }
			if (shouldWrite) {
				write(eC);
				int count = conceptCount.getAndIncrement();
				if (count == 1000 || count % 10000 == 0) {
					System.out.println(".");
					System.out.print(count + "-");
				}
				if (count % 1000 == 0) {
					System.out.print(".");
				}
			}
		} catch (Exception e) {
			System.out.println("\r\nThere was an error converting to eConcept: " + c.getPrimUuid() + " " + c.toString());
			e.printStackTrace();
		}

	}

	/**
	 * Write list of EConcepts to the DataOutputStream.
	 *
	 * @param eConcepts the e concepts
	 * @throws IOException signals that an I/O exception has occurred.
	 */
	public void writeList(List<TkConcept> eConcepts) throws IOException {
		if (eConcepts != null) {
			for (TkConcept eConcept : eConcepts) {
				write(eConcept);
			}
		}
	}

	/**
	 * Write a single Econcept to the DataOutputStream.
	 *
	 * @param eConcept the e concept
	 * @throws IOException signals that an I/O exception has occurred.
	 */
	public void write(TkConcept eConcept) throws IOException {
		writeSemaphore.acquireUninterruptibly();
		try {
			eConcept.writeExternal(eConceptDOS);
		} finally {
			writeSemaphore.release();
		}
	}

}
