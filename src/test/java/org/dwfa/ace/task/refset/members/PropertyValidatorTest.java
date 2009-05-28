package org.dwfa.ace.task.refset.members;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public final class PropertyValidatorTest {

    @Test
    public void shouldFailIfTheValueSuppliedIsNull() {
        try {
            new PropertyValidatorImpl().validate(null, "test value");
        } catch (PropertyNotFoundException e) {
            assertThat(e.getMessage(), equalTo("The test value has not been set."));
        }
    }

    @Test
    public void shouldPassIfTheValueIsNotNull() {
        new PropertyValidatorImpl().validate("blue", "another value");
    }
}
