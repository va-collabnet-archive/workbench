package org.dwfa.ace.task.refset.members;

public interface PropertyValidator {

    void validate(Object value, String name) throws PropertyNotFoundException;
}
