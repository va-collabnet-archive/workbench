package org.dwfa.mojo.relformat.mojo.sql;

public final class SQLLineBuilder {

    public String defaults() {
        return new StringBuilder().
                append("INSERT INTO Special ").
                append("(PID, REASON, DESCRIPTION, COUNT, STATUS, EFFECTIVE_TS, EFFECTIVE_DATE, EFFECTIVE_TIME)").
                append(" VALUES (").
                append("100, NULL, 'Testing', 3, 'A', ").
                append("TIMESTAMP('2008-11-06 15:55:33'), DATE('2008-11-11'), TIME('10:35:46'));").
                toString();
    }
}
