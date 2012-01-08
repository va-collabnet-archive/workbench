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
	private Set<UUID> watchUuids = new HashSet<UUID>();

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
			//			watchUuids.add(UUID.fromString("80a52c3a-dbda-5f76-bcbd-991f8a7ce270"));
			//			watchUuids.add(UUID.fromString("83b14b89-e643-537f-b066-443e76416e80"));
			//			watchUuids.add(UUID.fromString("46ef00d-6629-5270-bfe0-75338d125831"));
			//			watchUuids.add(UUID.fromString("952c4f12-d04a-53ca-b39f-a14a8c3f14f3"));
			//			watchUuids.add(UUID.fromString("9ea4f40e-f2e0-56a5-a127-1eab255df6cc"));
			//			watchUuids.add(UUID.fromString("9ef4e796-d5b9-538e-b1b4-43b4d9d7815a"));
			//			watchUuids.add(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd"));
			//			watchUuids.add(UUID.fromString("a4a58113-95df-5556-8520-7504bb088dc8"));
			//			watchUuids.add(UUID.fromString("a7e7fddd-4a29-5f24-aeca-636c15cadb92"));
			//			watchUuids.add(UUID.fromString("afe88f59-26fc-5a2f-8ef1-62bfa073cd0b"));
			//			watchUuids.add(UUID.fromString("cec102b0-8562-5167-9d85-c96cae54de2"));
			//			watchUuids.add(UUID.fromString("846ef00d-6629-5270-bfe0-75338d125831"));
			//			watchUuids.add(UUID.fromString("1b475397-3c49-3222-b27f-94016283e99d"));
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
		EConcept eC;
		try {
			long ini = Calendar.getInstance().getTimeInMillis();

			if (watchUuids.contains(c.getPrimUuid())) {
				System.out.println("Watch concept found: " + c);
			}
			eC = new EConcept(c);
			long elapsed = Calendar.getInstance().getTimeInMillis() - ini;

			if (elapsed > 1000) {
				System.out.println("Abnormally long eConcept conversion: " + c.toString() + " - " + c.getPrimUuid() + " (" + elapsed/1000 + " seconds)");
			}

			boolean shouldWrite = transformer.transform(eC);

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
			System.out.println("There was an error converting to eConcept: " + c.toString());
			e.printStackTrace();
		}

	}

	/**
	 * Write list of EConcepts to the DataOutputStream.
	 *
	 * @param eConcepts the e concepts
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeList(List<EConcept> eConcepts) throws IOException {
		if (eConcepts != null) {
			for (EConcept eConcept : eConcepts) {
				write(eConcept);
			}
		}
	}

	/**
	 * Write a single Econcept to the DataOutputStream.
	 *
	 * @param eConcept the e concept
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void write(EConcept eConcept) throws IOException {
		writeSemaphore.acquireUninterruptibly();
		try {
			eConcept.writeExternal(eConceptDOS);
		} finally {
			writeSemaphore.release();
		}
	}

}
