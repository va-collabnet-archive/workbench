package org.dwfa.tapi.impl;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.IOException;

import org.dwfa.tapi.TerminologyException;

public class LocalFixedDescPersistenceDelegate extends
		DefaultPersistenceDelegate {

	protected Expression instantiate(Object oldInstance, Encoder out) {
        LocalFixedDesc d = (LocalFixedDesc) oldInstance;
		XMLEncoder encoder = (XMLEncoder) out;
        try {
			return new Expression(oldInstance,
			                      d.getClass(),
			                      "get",
			                      new Object[]{d.getUids(), 
				encoder.getOwner() } );
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		}
    }
}
