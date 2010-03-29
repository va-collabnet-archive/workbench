package org.dwfa.ace.cs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the RemoveDuplicateParts method in BinaryChangeSetWriter
 *
 * NB RemoveDuplicateParts relies on the I_AmPart's equals method.
 */
public class TestBinaryChangeSetWriterRemoveDuplicateParts {
    BinaryChangeSetWriter binaryChangeSetWriter;

    @Before
    public void setUp() throws Exception {
        binaryChangeSetWriter = new BinaryChangeSetWriter(null, null);
    }

    /**
     * Test that no parts are removed when all parts are different.
     *
     * @throws Exception
     */
    @Test
    public void testRemoveNoParts() throws Exception {
        Method removeDuplicatePartsMethod = BinaryChangeSetWriter.class.getDeclaredMethod("removeDuplicateParts", List.class);
        removeDuplicatePartsMethod.setAccessible(true);
        List<I_ConceptAttributeVersioned> conceptAttributes = new ArrayList<I_ConceptAttributeVersioned>();

        I_ConceptAttributeVersioned versioned = new ThinConVersioned(1, 0);
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 1, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 1, true));
        conceptAttributes.add(versioned);

        removeDuplicatePartsMethod.invoke(binaryChangeSetWriter, conceptAttributes);

        Assert.assertEquals(5, versioned.getVersions().size());
    }

    /**
     * Test removing all the duplicates
     *
     * @throws Exception
     */
    @Test
    public void testRemoveDuplicateParts() throws Exception {
        Method removeDuplicatePartsMethod = BinaryChangeSetWriter.class.getDeclaredMethod("removeDuplicateParts", List.class);
        removeDuplicatePartsMethod.setAccessible(true);
        List<I_ConceptAttributeVersioned> conceptAttributes = new ArrayList<I_ConceptAttributeVersioned>();

        I_ConceptAttributeVersioned versioned = new ThinConVersioned(1, 0);
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 1, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 1, false));
        versioned.addVersion(getConceptAttributePart(1, 1, 1, true));
        versioned.addVersion(getConceptAttributePart(1, 1, 1, true));
        conceptAttributes.add(versioned);

        removeDuplicatePartsMethod.invoke(binaryChangeSetWriter, conceptAttributes);

        Assert.assertEquals(5, versioned.getVersions().size());
    }

    /**
     * Test removing the duplicate parts and the empty versioned objects.
     *
     * @throws Exception
     */
    @Test
    public void testRemoveDuplicateVersions() throws Exception {
        Method removeDuplicatePartsMethod = BinaryChangeSetWriter.class.getDeclaredMethod("removeDuplicateParts", List.class);
        removeDuplicatePartsMethod.setAccessible(true);
        List<I_ConceptAttributeVersioned> conceptAttributes = new ArrayList<I_ConceptAttributeVersioned>();

        I_ConceptAttributeVersioned versioned = new ThinConVersioned(1, 0);
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        conceptAttributes.add(versioned);

        I_ConceptAttributeVersioned versioned2 = new ThinConVersioned(1, 0);
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(0, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        versioned.addVersion(getConceptAttributePart(1, 0, 0, false));
        conceptAttributes.add(versioned2);

        removeDuplicatePartsMethod.invoke(binaryChangeSetWriter, conceptAttributes);

        Assert.assertEquals(2, versioned.getVersions().size());
        Assert.assertEquals(0, versioned2.getVersions().size());
        Assert.assertEquals(1, conceptAttributes.size());
    }

    /**
     * Part creator
     *
     * @param path int
     * @param version int
     * @param status int
     * @param defined int
     *
     * @return I_ConceptAttributePart
     */
    private I_ConceptAttributePart getConceptAttributePart(int path, int version, int status, boolean defined) {
        I_ConceptAttributePart part = new ThinConPart();
        part.setPathId(path);
        part.setVersion(version);
        part.setStatusId(status);
        part.setDefined(defined);

        return part;
    }
}
