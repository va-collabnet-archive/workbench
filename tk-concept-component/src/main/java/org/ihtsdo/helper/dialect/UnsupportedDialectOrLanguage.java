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

package org.ihtsdo.helper.dialect;

// TODO: Auto-generated Javadoc
/**
 * The Class UnsupportedDialectOrLanguage.
 *
 * @author kec
 */
public class UnsupportedDialectOrLanguage extends Exception {

   /**
    * Instantiates a new unsupported dialect or language.
    *
    * @param thrwbl the thrwbl
    */
   public UnsupportedDialectOrLanguage(Throwable thrwbl) {
      super(thrwbl);
   }

   /**
    * Instantiates a new unsupported dialect or language.
    *
    * @param string the string
    * @param thrwbl the thrwbl
    */
   public UnsupportedDialectOrLanguage(String string, Throwable thrwbl) {
      super(string, thrwbl);
   }

   /**
    * Instantiates a new unsupported dialect or language.
    *
    * @param string the string
    */
   public UnsupportedDialectOrLanguage(String string) {
      super(string);
   }

   /**
    * Instantiates a new unsupported dialect or language.
    */
   public UnsupportedDialectOrLanguage() {
   }

}
