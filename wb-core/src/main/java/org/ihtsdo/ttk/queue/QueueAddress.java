/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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



package org.ihtsdo.ttk.queue;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.preferences.PreferenceObject;
import org.ihtsdo.ttk.preferences.PreferenceWithDefaultEnumBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Objects;

/**
 *
 * @author kec
 */
public class QueueAddress implements PreferenceObject {
   private String address;

   public QueueAddress() {
      this.address = Fields.QUEUE_ADDRESS.getDefaultValue();
   }

   public QueueAddress(EnumBasedPreferences preferences) {
      this.address = preferences.get(Fields.QUEUE_ADDRESS);
   }

   public QueueAddress(String address) {
      this.address = address;
   }

   enum Fields implements PreferenceWithDefaultEnumBI<String> {
      QUEUE_ADDRESS;

      @Override
      public String getDefaultValue() {
         return "dwa1@informatics.com";
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final QueueAddress other = (QueueAddress) obj;

      if (!Objects.equals(this.address, other.address)) {
         return false;
      }

      return true;
   }

   @Override
   public void exportFields(EnumBasedPreferences preferences) {
      preferences.put(Fields.QUEUE_ADDRESS, this.address);
   }

   @Override
   public int hashCode() {
      int hash = 7;

      hash = 53 * hash + Objects.hashCode(this.address);

      return hash;
   }

   @Override
   public String toString() {
      return "QueueAddress: " + address;
   }

   public String getAddress() {
      return address;
   }
}
