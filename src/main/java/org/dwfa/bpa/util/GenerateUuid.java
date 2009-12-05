/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
/*
 * Created on Mar 21, 2005
 */
package org.dwfa.bpa.util;

import java.math.BigInteger;
import java.util.UUID;

/**
 * @author kec
 *
 */
public class GenerateUuid {

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            UUID id = UUID.randomUUID();
            long lsb = id.getLeastSignificantBits();
            long msb = id.getMostSignificantBits();
            BigInteger lsbBigInt = new BigInteger(Long.toOctalString(lsb), 8);
            BigInteger msbBigInt = new BigInteger(Long.toOctalString(msb), 8);

            System.out.println("uuid: " + id);
            System.out.println("lsb: " + lsbBigInt);
            System.out.println("msb: " + msbBigInt);
            System.out.println("combined: " + msbBigInt + lsbBigInt);

        }
    }
}
