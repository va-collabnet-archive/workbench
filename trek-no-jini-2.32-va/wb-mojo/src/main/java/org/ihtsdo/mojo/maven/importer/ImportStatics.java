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

/**
 * @author Adam Flinton
 */

package org.ihtsdo.mojo.maven.importer;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.util.id.Type5UuidFactory;

public class ImportStatics {
	
	//Normal Text File Names
	public static String RELT = "relationships.txt";
	public static String CONCEPTT = "concepts.txt";
	public static String DESCT = "descriptions.txt";
	public static String IDST = "ids.txt";
	public static String ILLICIT = "illicit_words.txt";
	public static String LICIT = "licit_words.txt";
	//RefSets Test File Names
	public static String BREF = "boolean.refset";
	public static String CREF = "concept.refset";
	public static String CIREF = "conint.refset";
	public static String MREF = "measurement.refset";
	public static String IREF = "integer.refset";
	public static String SREF = "string.refset";
	public static String LREF = "language.refset";

	// UUID
	
	/** Generates a UUID using a UUID as a seed plus a string
	 *  Returns a UUID
	 * @param in_uuid
	 * @param extraS
	 * @return
	 */
	public static UUID getGenUUID(UUID in_uuid, String extraS) {
		UUID ret_uuid = null;
		try {
			ret_uuid = Type5UuidFactory.get(in_uuid, extraS);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret_uuid;
	}

	/** Generates a UUID using a UUID as a seed plus a string
	 *  Returns a String
	 * @param in_uuid
	 * @param extraS
	 * @return
	 */
	public static String getGenUUID_S(UUID in_uuid, String extraS) {
		return getGenUUID(in_uuid, extraS).toString();
	}
	/**
	 * Gets a random UUID
	 * Returns a String
	 * @return
	 */
	public static String getRandomUUID_S() {
		return UUID.randomUUID().toString();
		// return getGenUUID(in_uuid,extraS).toString();
	}

	/** Gets a Path UUID using a string as a seed
	 *  Returns a String
	 * @param extraS
	 */
	public static String getPathUUID_S(String extraS) {
		return getPathUUID(extraS).toString();
	}

	/** Gets a Path UUID using a string as a seed
	 *  Returns a UUID
	 * @param extraS
	 * @return
	 */
	public static UUID getPathUUID(String extraS) {
		return getGenUUID(Type5UuidFactory.PATH_ID_FROM_FS_DESC, extraS);
	}

	/**
	 * Encapsulates concept.getUids().iterator().next().toString(); 
	 * @param concept
	 * @return the UUID as a string
	 * @throws Exception
	 */
	public static String getU4C(I_ConceptualizeUniversally concept) throws Exception{
		return concept.getUids().iterator().next().toString();
	}



}
