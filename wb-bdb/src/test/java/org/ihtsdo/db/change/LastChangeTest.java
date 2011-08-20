/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.db.change;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.db.change.LastChange.Change;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author kec
 */
public class LastChangeTest {

   /**
    * Test of touch method, of class LastChange.
    */
   @Test
   public void testTouch() {
      int nid = Integer.MIN_VALUE + 20;

      BdbCommitSequence.nextSequence();
      BdbCommitSequence.nextSequence();
      LastChange.touch(nid, Change.COMPONENT);

      int componentCommitSequence = BdbCommitSequence.getCommitSequence();

      assertEquals(LastChange.getLastTouch(nid, Change.COMPONENT), componentCommitSequence);
      BdbCommitSequence.nextSequence();
      BdbCommitSequence.nextSequence();
      BdbCommitSequence.nextSequence();

      int xrefCommitSequence = BdbCommitSequence.getCommitSequence();

      LastChange.touch(nid, Change.XREF);
      assertFalse(componentCommitSequence == xrefCommitSequence);
      assertEquals(LastChange.getLastTouch(nid, Change.XREF), xrefCommitSequence);
   }
}
