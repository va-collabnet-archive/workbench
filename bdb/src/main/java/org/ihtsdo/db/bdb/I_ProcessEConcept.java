package org.ihtsdo.db.bdb;

import org.ihtsdo.etypes.EConcept;

interface I_ProcessEConcept extends Runnable {

	public void setEConcept(EConcept eConcept) throws Throwable;

}