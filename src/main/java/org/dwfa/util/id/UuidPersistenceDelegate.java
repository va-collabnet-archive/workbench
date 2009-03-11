package org.dwfa.util.id;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.util.UUID;

public class UuidPersistenceDelegate extends DefaultPersistenceDelegate {

    protected Expression instantiate(Object oldInstance, Encoder out) {
        UUID uuid = (UUID) oldInstance;
        return new Expression(oldInstance,
        				uuid.getClass(),
                              "fromString",
                              new Object[]{ uuid.toString() });
    }

}
