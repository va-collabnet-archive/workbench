/*
 * Created on Mar 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
