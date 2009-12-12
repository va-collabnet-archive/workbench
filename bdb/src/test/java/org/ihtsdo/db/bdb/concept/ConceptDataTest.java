/**
 * 
 */
package org.ihtsdo.db.bdb.concept;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author kec
 *
 */
public class ConceptDataTest {
	
	byte[] readOnlyData;
	byte[] readWriteData;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TupleOutput to = new TupleOutput();
		for (ConceptData.OFFSETS off: ConceptData.OFFSETS.values()) {
			to.writeInt(0);
		}
		TupleOutput offsetOutput = new TupleOutput(to.getBufferBytes());
		for (ConceptData.OFFSETS off: ConceptData.OFFSETS.values()) {
			offsetOutput.writeInt(to.size());
			to.writeInt(off.ordinal());
		}
		readOnlyData = to.getBufferBytes();
	}

	/**
	 * Test method for {@link org.ihtsdo.db.bdb.concept.ConceptData#getDescriptions()}.
	 */
	@Test
	public void testGetDescriptions() {
		TupleInput dataInput = new TupleInput(readOnlyData);
		int i = 0;
		for (ConceptData.OFFSETS off: ConceptData.OFFSETS.values()) {
			int offset = off.getOffset(readOnlyData);
			dataInput.reset();
			dataInput.skipFast(offset);
			int value = dataInput.readInt();
			Assert.assertEquals(i, value);
			i++;
		}
	}

}
