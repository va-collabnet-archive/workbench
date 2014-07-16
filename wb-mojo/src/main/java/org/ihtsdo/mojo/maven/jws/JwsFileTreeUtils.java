/**
 * Copyright (c) 2014 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.mojo.maven.jws;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonParser;

/**
 *
 * @author Marc Campbell
 */
public class JwsFileTreeUtils {

    public static JwsFileTreeNode convertJsonToTreeNode() {
        return null;
    }

    public static JsonArray convertTreeNodeToJson(JwsFileTreeNode tree) {
        JsonArrayBuilder nodeBuilder = Json.createArrayBuilder();
        convertTreeNodeRecursively(tree, nodeBuilder);
        JsonArray jsonResult = nodeBuilder.build();

        return jsonResult;
    }

    private static void convertTreeNodeRecursively(JwsFileTreeNode node,
            JsonArrayBuilder builder) {
        JsonObjectBuilder objBuilder = Json.createObjectBuilder();
        objBuilder.add("path", node.nodePath);
        objBuilder.add("type", node.nodeType);
        if (node.sha1 != null && !node.sha1.isEmpty()) {
            objBuilder.add("sha1", node.sha1);
        }
        List<JwsFileTreeNode> childrenList = node.children;
        if (node.children != null && !node.children.isEmpty()) {
            JsonArrayBuilder childBuilder = Json.createArrayBuilder();
            for (JwsFileTreeNode childNode : childrenList) {
                convertTreeNodeRecursively(childNode, childBuilder);
            }
            objBuilder.add("children", childBuilder);
        }
        builder.add(objBuilder);
    }

    public static JwsFileTreeNode createJwsTreeFromDirectory(String sDir,
            String sRelativeRootDir,
            JwsFileTreeNode node) {
        File[] faFiles = new File(sDir).listFiles();
        for (File file : faFiles) {
            if (file.isFile()) { // file.getName().matches("^(.*?)")
                // ADD FILE NODE
                String sPath = file.getAbsolutePath().substring(sRelativeRootDir.length()+ 1);
                // System.out.println(sPath); // :VERBOSE:
                node.addChild(sPath, createSha1(file));
            }
            if (file.isDirectory()) {
                // ADD DIRECTORY NODE
                JwsFileTreeNode dirNode = new JwsFileTreeNode();
                dirNode.parent = node;
                dirNode.nodeType = "folder";
                String sPath = file.getAbsolutePath().substring(sRelativeRootDir.length() + 1);
                dirNode.nodePath = sPath;
                node.children.add(dirNode);
                // System.out.println(sPath); // :VERBOSE:
                createJwsTreeFromDirectory(file.getAbsolutePath(), sRelativeRootDir, dirNode);
            }
        }
        return node;
    }

    public static String createSha1(File file) {
        try {
            // MessageDigest md5 = MessageDigest.getInstance("MD5");
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DigestInputStream dis = new DigestInputStream(bis, sha1);

            // read the file and update the hash calculation
            while (dis.read() != -1) {
                // no inner executable statements. work do by dis.read()
            }

            // get the hash value as byte array
            byte[] hash = sha1.digest();

            /* alternate string Formatter based on C's printf */
//        Formatter formatter = new Formatter();
//        for (byte b : hash) {
//            formatter.format("%02x", b);
//        }
//        return formatter.toString();
            StringBuilder hexStringBuilder = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                hexStringBuilder.append(Integer.toHexString(0xFF & hash[i]));
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(JwsFileTreeUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JwsFileTreeUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JwsFileTreeUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JwsFileTreeNode readJsonFile(String inPath)
            throws FileNotFoundException {

        FileInputStream fis;
        fis = new FileInputStream(inPath);
        JsonParser jsonParser = Json.createParser(fis);

        JwsFileTreeNode rootNode = readJsonRecursively(jsonParser, null, null);

        return rootNode;
    }

    private static JwsFileTreeNode readJsonRecursively(JsonParser parser, JwsFileTreeNode currentNode, JwsFileTreeNode parent) {
        JwsFileTreeNode node = currentNode;
        boolean addChildrenMode = false;
        String keyName = null;
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            // System.out.println(event);
            switch (event) {
                case START_ARRAY:
                    if (node != null) { // ignore START_ARRAY prior to root
                        addChildrenMode = true;
                    }
                    break;
                case START_OBJECT:
                    if (addChildrenMode) {
                        JwsFileTreeNode childNode = new JwsFileTreeNode();
                        childNode.parent = node;
                        readJsonRecursively(parser, childNode, node);
                        if (node == null) {
                            throw new UnsupportedOperationException("node should not be null in addChildren mode");
                        }
                        node.children.add(childNode);
                    } else {
                        node = new JwsFileTreeNode();
                        if (parent != null) {
                            if (parent.children == null) {
                                parent.children = new LinkedList<>();
                            }
                            node.parent = parent;
                            parent.children.add(node);
                        }
                    }
                    break;
                case KEY_NAME:
                    keyName = parser.getString();
                    break;
                case VALUE_STRING:
                    setStringValues(node, keyName, parser.getString());
                    break;
                case VALUE_NUMBER: // setNumberValues(... , keyName, parser.getLong());
                    break;
                case VALUE_FALSE: // setBooleanValues(... , false);
                    break;
                case VALUE_TRUE: // setBooleanValues(... , true);
                    break;
                case VALUE_NULL: // don't set anything
                    break;
                case END_OBJECT:
//                    if (node != null) {
//                        System.out.println(node.toStringWithParentPaths());
//                    }
                    return node;
                case END_ARRAY:
                    break;
                default:
                // we are not looking for other events
            }
        }
        return node;
    }

    private static void setStringValues(JwsFileTreeNode node,
            String key, String value) {
        switch (key) {
            case "type":
                node.nodeType = value;
                break;
            case "name":  // no longer used
                node.nodePath = value;
                break;
            case "path":
                node.nodePath = value;
                break;
            case "sha1":
                node.sha1 = value;
                break;
            default:
                System.out.println("Unknown Key = " + key);
        }
    }

    public static void writeJsonFile(JsonArray jsonArray, String outPath)
            throws FileNotFoundException {
        File fpOut = new File(outPath);
        fpOut.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(fpOut);
        try (JsonWriter jsonWriter = Json.createWriter(os)) {
            jsonWriter.writeArray(jsonArray);
        }
    }

    private static String createIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

}
