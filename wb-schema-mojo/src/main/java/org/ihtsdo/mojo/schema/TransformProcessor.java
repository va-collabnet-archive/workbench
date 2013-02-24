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
//            watchUuids.add(UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66")); // fully specified name
            watchUuids.add(UUID.fromString("693e7426-8145-53ff-90a7-b43c6aa2e863")); // Presbylarynx (disorder)
//            watchUuids.add(UUID.fromString("814b4598-07f1-5548-a887-c5371dbaaf84")); // Cares for sick or handicapped family member
//            watchUuids.add(UUID.fromString("ae72b74d-92ad-f699-c019-10bbc1fbe0ec")); //

//            watchUuids.add(UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad")); // United States of America English language reference set
//            watchUuids.add(UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30")); // Great Britain English language reference set
              watchUuids.add(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")); // US English Dialect Subset
              watchUuids.add(UUID.fromString("e8191494-ce3c-5bd8-803e-31d31c831f8a")); // GB English Dialect Exceptions Subset
              watchUuids.add(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")); // GB English Dialect Subset

//            watchUuids.add(UUID.fromString("8c473da3-eb41-5415-9108-6b43adc0a42e")); // Hypothyroidism in pregnancy, antepartum -- KP
//            watchUuids.add(UUID.fromString("ac4744c9-ea2b-11e0-9572-0800200c9a66")); // Hypothyroidism in pregnancy, antepartum -- SCT
//            watchUuids.add(UUID.fromString("0313cfbf-a4db-3793-aa8c-595b4e383156")); // Hypothyroidism in pregnancy 
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
