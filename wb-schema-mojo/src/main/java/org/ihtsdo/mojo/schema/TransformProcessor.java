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
						watchUuids.add(UUID.fromString("a8dd0021-4994-36b2-a0f5-567b7e007847"));
						watchUuids.add(UUID.fromString("0b8e4206-ab03-399d-906c-dedc941a4e5e"));
						watchUuids.add(UUID.fromString("b1b1e773-3eb6-3bcc-a6c7-52ac5d0a53be"));
						watchUuids.add(UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));
						watchUuids.add(UUID.fromString("c259d808-8011-3772-bece-b4fbde18d375"));
						watchUuids.add(UUID.fromString("d15fde65-ed52-3a73-926b-8981e9743ee9"));
						watchUuids.add(UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));
						watchUuids.add(UUID.fromString("1a090a21-28c4-3a87-9d04-766f04600494"));
						watchUuids.add(UUID.fromString("3bc6ad17-253e-53a9-bea6-049abfa467c8"));
						watchUuids.add(UUID.fromString("5ef10e09-8f16-398e-99b5-55cff5bd820a"));
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
		TkConcept eC;
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
