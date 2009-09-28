package org.dwfa.vodb.impl;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.apache.commons.collections.primitives.IntList;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;

public class CheckAndProcessSearchTest implements Runnable {

	IntList matches;

	I_GetConceptData conceptToTest;

	List<I_TestSearchResults> checkList;

	I_ConfigAceFrame config;

	Semaphore checkSemaphore;

	Semaphore addSemaphore = new Semaphore(1);

	public CheckAndProcessSearchTest(Semaphore checkSemaphore, IntList matches,
			I_GetConceptData conceptToTest,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config) {
		super();
		this.matches = matches;
		this.conceptToTest = conceptToTest;
		this.checkList = checkList;
		this.config = config;
		this.checkSemaphore = checkSemaphore;
	}

	public void run() {
		if (checkList == null || checkList.size() == 0) {
			try {
				addSemaphore.acquire();
				matches.add(conceptToTest.getConceptId());
				addSemaphore.release();
			} catch (InterruptedException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {
			try {
				boolean failed = false;
				for (I_TestSearchResults test : checkList) {
					if (test.test(conceptToTest, config) == false) {
						failed = true;
						break;
					}
				}

				if (failed == false) {
					addSemaphore.acquire();
					matches.add(conceptToTest.getConceptId());
					addSemaphore.release();
				}
			} catch (TaskFailedException e) {
				if (ACE.editMode) {
					AceLog.getAppLog().alertAndLogException(e);
				} else {
					AceLog.getAppLog().log(Level.SEVERE,
							e.getLocalizedMessage(), e);
				}
			} catch (InterruptedException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		checkSemaphore.release();
	}

}