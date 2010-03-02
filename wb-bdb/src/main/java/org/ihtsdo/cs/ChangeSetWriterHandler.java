package org.ihtsdo.cs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;

public class ChangeSetWriterHandler implements Runnable {

	private static CopyOnWriteArraySet<I_WriteChangeSet> writers = new CopyOnWriteArraySet<I_WriteChangeSet>();
	public static AtomicInteger changeSetWriters = new AtomicInteger();

	private List<Integer> cNidsToWrite;
	private long commitTime;
	private IntSet sapNidsFromCommit;

	public ChangeSetWriterHandler(Collection<Integer> cNidsToWrite,
			long commitTime, IntSet sapNidsFromCommit) {
		super();
		assert commitTime != Long.MAX_VALUE;
		assert commitTime != Long.MIN_VALUE;
		this.cNidsToWrite = new ArrayList<Integer>(cNidsToWrite);
		this.commitTime = commitTime;
		this.sapNidsFromCommit = sapNidsFromCommit;
		changeSetWriters.incrementAndGet();
	}

	@Override
	public void run() {
		try {
			for (I_WriteChangeSet writer : writers) {
				writer.open(sapNidsFromCommit);
			}
			for (Integer cNid : cNidsToWrite) {
				Concept c = Concept.get(cNid);
				for (I_WriteChangeSet writer: writers) {
					writer.writeChanges(c, commitTime);
				}
			}
			for (I_WriteChangeSet writer: writers) {
				writer.commit();
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public static void addWriter(I_WriteChangeSet writer) {
		writers.add(writer);
	}

	public static void removeWriter(I_WriteChangeSet writer) {
		writers.remove(writer);
	}

}
