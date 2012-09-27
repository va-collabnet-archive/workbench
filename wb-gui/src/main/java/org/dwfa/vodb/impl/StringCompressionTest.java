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
package org.dwfa.vodb.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StringCompressionTest {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        File descFile = new File(
            "/Users/kec/acews/ace-sub/ace-db/dev/src/main/resources/org/snomed/2006-07-31/sct_descriptions_20060731.txt");
        FileReader descFileReader = new FileReader(descFile);
        LineNumberReader descReader = new LineNumberReader(descFileReader);

        int lines = 0;
        int tags = 0;
        int tokens = 0;
        long tokenBytes = 0;
        long bytesSaved = 0;
        Map<String, Integer> uniqueTokens = new TreeMap<String, Integer>();
        ArrayList<String> tokenList = new ArrayList<String>();
        Set<String> semanticTags = new HashSet<String>();
        descReader.readLine(); // skip first line.
        while (descReader.ready()) {
            String line = descReader.readLine();
            List<Integer> encoding = new ArrayList<Integer>();
            String[] fields = line.split("\t");
            if (fields[3].endsWith(")")) {
                int tagStart = fields[3].lastIndexOf(" (");
                if (tagStart > 0) {
                    String semanticTag = fields[3].substring(tagStart);
                    semanticTags.add(semanticTag);
                    tags++;
                } else {
                    System.out.println("messed up tag: " + fields[3]);
                }

            }
            for (String fieldToken : fields[3].split(" ")) {
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
        for (String uniqueToken : uniqueTokens.keySet()) {
            uniqueTokenBytes = uniqueTokenBytes + uniqueToken.getBytes().length;
        }
        System.out.println("Read " + lines + " lines. ");
        System.out.println("Found " + tokens + " tokens. ");
        System.out.println("Found " + tokenBytes + " token bytes. ");
        System.out.println("Found " + uniqueTokens.size() + " unique tokens. ");
        System.out.println("Found " + uniqueTokenBytes + " unique token bytes. ");
        System.out.println("Found " + tags + " tags. ");
        System.out.println("Found unique " + semanticTags.size() + " tags. ");
        System.out.println(semanticTags);
        System.out.println("Bytes saved " + bytesSaved + " bytesSaved. ");

        String one = "Anatomical organisational pattern";
        String two = "Anatomical organizational pattern";
        String three = "Anatomical organizational pattern (body structure)";

        int bytes = one.getBytes().length + two.getBytes().length + three.getBytes().length;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(one.getBytes("UTF-8"));
        baos.write('\0');
        baos.write(two.getBytes("UTF-8"));
        baos.write('\0');
        baos.write(three.getBytes("UTF-8"));
        baos.write('\0');
        byte[] toCompress = baos.toByteArray();

        System.out.println("Uncompressed size: " + bytes);

        byte[] deflateCompressArray = compressArray(toCompress, Deflater.BEST_COMPRESSION);
        System.out.println("deflate size, best compression: " + deflateCompressArray.length);
        deflateCompressArray = compressArray(toCompress, Deflater.BEST_SPEED);
        System.out.println("deflate size, BEST_SPEED: " + deflateCompressArray.length);
        deflateCompressArray = compressArray(toCompress, Deflater.HUFFMAN_ONLY);
        System.out.println("deflate size, HUFFMAN_ONLY: " + deflateCompressArray.length);
        deflateCompressArray = compressArray(toCompress, Deflater.DEFAULT_STRATEGY);
        System.out.println("deflate size, DEFAULT_STRATEGY: " + deflateCompressArray.length);
        deflateCompressArray = compressArray(toCompress, Deflater.DEFAULT_COMPRESSION);
        System.out.println("deflate size, DEFAULT_COMPRESSION: " + deflateCompressArray.length);

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

    // example at
    // http://java.sun.com/j2se/1.5.0/docs/api/java/util/zip/Deflater.html
    private static byte[] compressArray(byte[] toCompress, int level) throws IOException {
        ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
        DeflaterOutputStream dout = new DeflaterOutputStream(compressedOut, new Deflater(level));
        dout.write(toCompress);
        dout.close();
        return compressedOut.toByteArray();
    }

}
/*
 * Body structure, altered from its original anatomical structure
 * Body structure, altered from its original anatomical structure (morphologic
 * abnormality)
 * Morphologically altered structure
 * Morphologic change
 * Morphologic alteration
 */
