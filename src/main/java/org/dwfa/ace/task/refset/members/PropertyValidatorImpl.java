package org.dwfa.ace.task.refset.members;

/**
 * Validates propery values. If they are null throws a <code>PropertyNotFoundException</code>.
 */
public final class PropertyValidatorImpl implements PropertyValidator {

    public void validate(final Object value, final String name) throws PropertyNotFoundException {
        if (value == null) {
            throw new PropertyNotFoundException("The " + name + " has not been set.");
        }
    }
}
