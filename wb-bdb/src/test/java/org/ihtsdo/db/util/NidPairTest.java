package org.ihtsdo.db.util;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

public class NidPairTest {

    @Test
    public void testNidPairLong() {
        for (int i = 0; i < 1000; i++) {
            Random r = new Random();
            int nid1 = r.nextInt();
            int nid2 = r.nextInt();
            NidPair np = new NidPair(nid1, nid2);
            Assert.assertEquals(nid1, np.getNid1());
            Assert.assertEquals(nid2, np.getNid2());
            NidPair np2 = new NidPair(np.asLong());
            Assert.assertEquals(nid1, np2.getNid1());
            Assert.assertEquals(nid2, np2.getNid2());
        }
    }

}
