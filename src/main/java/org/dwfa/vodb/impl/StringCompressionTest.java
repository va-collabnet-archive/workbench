package org.dwfa.vodb.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StringCompressionTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File descFile = new File("/Users/kec/acews/ace-sub/ace-db/dev/src/main/resources/org/snomed/2006-07-31/sct_descriptions_20060731.txt");
		FileReader descFileReader = new FileReader(descFile);
		LineNumberReader descReader = new LineNumberReader(descFileReader);
		
		int lines = 0;
		int tags = 0;
		int tokens = 0;
		long tokenBytes = 0;
		long bytesSaved = 0;
		Map<String, Integer> uniqueTokens = new TreeMap<String, Integer>();
		ArrayList<String> tokenList = new ArrayList<String>();
		descReader.readLine(); // skip first line. 
		while (descReader.ready()) {
			String line = descReader.readLine();
			List<Integer> encoding = new ArrayList<Integer>();
			String[] fields = line.split("\t");
			if (fields[3].endsWith(")")) {
				tags++;
			}
			for (String fieldToken: fields[3].split(" ")) {
				tokens++;
				tokenBytes = tokenBytes + fieldToken.getBytes().length;
				if (uniqueTokens.containsKey(fieldToken) == false) {
					Integer id = tokenList.size();
					tokenList.add(fieldToken);
					uniqueTokens.put(fieldToken, id);
					encoding.add(id);
				} else {
					Integer id = uniqueTokens.get(fieldToken);
					encoding.add(id);
				}
			}
			bytesSaved = bytesSaved + (fields[3].getBytes().length - (encoding.size() * 4));
			lines++;
			
		}
		descReader.close();
		long uniqueTokenBytes = 0;
		for (String uniqueToken: uniqueTokens.keySet()) {
			uniqueTokenBytes = uniqueTokenBytes + uniqueToken.getBytes().length;
		}
		System.out.println("Read " + lines + " lines. ");
		System.out.println("Found " + tokens + " tokens. ");
		System.out.println("Found " + tokenBytes + " token bytes. ");
		System.out.println("Found " + uniqueTokens.size() + " unique tokens. ");
		System.out.println("Found " + uniqueTokenBytes + " unique token bytes. ");
		System.out.println("Found " + tags + " tags. ");
		System.out.println("Bytes saved " + bytesSaved + " bytesSaved. ");
		
		String one = "Anatomical organisational pattern";
		String two = "Anatomical organizational pattern";
		String three = "Anatomical organizational pattern (body structure)";
		
		int bytes = one.getBytes().length + two.getBytes().length + three.getBytes().length;
		
		System.out.println("Uncompressed size: " + bytes);

		bytes = compressString(one).length + compressString(two).length + compressString(three).length;

		System.out.println("Compressed size: " + bytes);

		bytes = compressString(one + two + three).length;

		System.out.println("Compresseds size: " + bytes);
	}

	private static byte[] compressString(String str) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zout = new ZipOutputStream(out);
		zout.putNextEntry(new ZipEntry("0"));
		zout.write(str.getBytes());
		zout.closeEntry();
		byte[] compressed = out.toByteArray();
		zout.close();
		return compressed;
	}

}
/*
Body structure, altered from its original anatomical structure
Body structure, altered from its original anatomical structure (morphologic abnormality)
Morphologically altered structure
Morphologic change
Morphologic alteration
*/