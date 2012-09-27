/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.concurrency;

// TODO: Auto-generated Javadoc
//~--- JDK imports ------------------------------------------------------------

/**
 * The Class ConcurrencyLocks.
 *
 * @author kec
 */
public abstract class ConcurrencyLocks {
   
   /** The concurrency level. */
   protected final int   concurrencyLevel;
   
   /** The sshift. */
   private int   sshift           = 0;
   
   /** The ssize. */
   private int   ssize            = 1;
   
   /** The segment mask. */
   protected int segmentMask;
   
   /** The segment shift. */
   protected int segmentShift;
   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new concurrency locks.
    */
   public ConcurrencyLocks() {
       concurrencyLevel = 128;
      setup();
   }

   /**
    * Instantiates a new concurrency locks.
    *
    * @param concurrencyLevel the concurrency level
    */
   public ConcurrencyLocks(int concurrencyLevel) {
      this.concurrencyLevel = concurrencyLevel;
      setup();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Setup.
    */
   private void setup() {
      while (ssize < concurrencyLevel) {
         ++sshift;
         ssize <<= 1;
      }

      segmentShift = 32 - sshift;
      segmentMask  = ssize - 1;
      
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concurrency level.
    *
    * @return the concurrency level
    */
   public int getConcurrencyLevel() {
      return concurrencyLevel;
   }
}
