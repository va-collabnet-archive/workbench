package org.ihtsdo.mojo.refset;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.tk.api.PathBI;

/**
 * 
 * Produce an HTML report of all refset metadata in the ACE db.
 * 
 * <p>
 * Specify the report file using the list_file parameter.
 * 
 * @goal refset-listing
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class RefsetListing extends AbstractMojo {

    /**
     * The uuid of the path.
     * 
     * @parameter
     */
    private String path_uuid = null;

    private PathBI path = null;

    /**
     * The uuid of the refset concept. Can be set to some descendant to limit
     * the scope of the report.
     * 
     * @parameter default-value="3e0cd740-2cc6-3d68-ace7-bad2eb2621da"
     */
    private String refset_con_uuid;

    private I_GetConceptData refset_con;

    /**
     * Set to true to sort by name. Default order is hierarchical with siblings
     * sorted by name.
     * 
     * @parameter default-value=false
     */
    private boolean sort_by_name;

    /**
     * List refset to file
     * 
     * @parameter default-value="refsets.html"
     */
    private File list_file;

    /**
     * This file will contain the generated report, in a location that can be utilised by Maven's site plugin.
     * 
     * @parameter expression="${project.build.directory}/site/xdoc/reports/refset_listing.html"
     */
    private File site_output_file;

    private void listRefset() throws Exception {
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        getLog().info("refset list");
        list_file.getParentFile().mkdirs();
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.list_file)));
        final I_TermFactory tf = Terms.get();
        if (this.path_uuid != null) {
            this.path = tf.getPath(Arrays.asList(UUID.fromString(this.path_uuid)));
        }
        // I_IntSet allowed_status = tf.newIntSet();
        // allowed_status.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
        refset_con = tf.getConcept(Arrays.asList(UUID.fromString(this.refset_con_uuid)));
        out.println("<html>");
        out.println("<head>");
        out.println("<style type=\"text/css\">");
        out.println("BODY {font:10pt sans-serif}");
        out.println("TABLE {font:10pt sans-serif; border-collapse:collapse}");
        out.println("TD {border: 1px solid; padding:5px}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>");
        out.println("Refset Report");
        out.println("</h2>");
        out.println("<p>");
        out.println(escapeString(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date())));
        ArrayList<Integer> refsets = getCoreDescendants(refset_con.getConceptNid(), getActiveStatus(), this.path);
        if (this.sort_by_name) {
            Collections.sort(refsets, new Comparator<Integer>() {
                public int compare(Integer obj1, Integer obj2) {
                    try {
                        String s1 = tf.getConcept(obj1).getInitialText();
                        String s2 = tf.getConcept(obj2).getInitialText();
                        return s1.compareTo(s2);
                    } catch (Exception e) {
                    }
                    return obj1.compareTo(obj2);
                }
            });
        }
        for (Integer con_id : refsets) {
            I_GetConceptData con = tf.getConcept(con_id);
            out.println("<h3>");
            out.println(escapeString(con.getInitialText()));
            out.println("</h3>");
            out.println("<table border=\"1\">");
            for (UUID id : con.getUids()) {
                out.println("<tr>");
                out.println("<td>");
                out.println("ID");
                out.println("<td>");
                out.println(escapeString(String.valueOf(id)));
            }
            for (String r : Arrays.asList("dd413e49-c124-3b05-8c25-0da5922379d3",
                "7a981930-621f-3935-b26c-47f54413a59d", "41fbef7f-7210-3288-97cb-c860dfc90601",
                "f60922c9-cb3d-3099-8960-1097d2c5afdc")) {
                I_GetConceptData r_con = tf.getConcept(Arrays.asList(UUID.fromString(r)));
                if (r_con != null) {
                    boolean found = false;
                    String head = r_con.getInitialText().replace(" rel", "");
                    I_IntSet r_set = tf.newIntSet();
                    r_set.add(r_con.getConceptNid());
                    for (I_GetConceptData val_con : con.getSourceRelTargets(getActiveStatus(), r_set, null, 
                        config.getPrecedence(), config.getConflictResolutionStrategy())) {
                        // for (int val_id : getRelationship(con.getConceptNid(),
                        // r_con.getConceptNid(), null, Integer.MAX_VALUE)) {
                        // I_GetConceptData val_con = tf.getConcept(val_id);
                        if (val_con != null) {
                            found = true;
                            out.println("<tr>");
                            out.println("<td>");
                            out.println(escapeString(head));
                            out.println("<td>");
                            out.println(escapeString(val_con.getInitialText()));
                        }
                    }
                    if (!found) {
                        out.println("<tr>");
                        out.println("<td>");
                        out.println(escapeString(head));
                        out.println("<td>");
                    }
                }
            }
            // comments rel "ff1b55d3-2b7b-382c-ae42-eceffcc47c71"
            // promotion rel "9a801240-b3b0-3475-8a7b-07111d3ff564"
            for (String r : Arrays.asList("ff1b55d3-2b7b-382c-ae42-eceffcc47c71",
                "9a801240-b3b0-3475-8a7b-07111d3ff564")) {
                I_GetConceptData r_con = tf.getConcept(Arrays.asList(UUID.fromString(r)));
                if (r_con != null) {
                    boolean found = false;
                    String head = r_con.getInitialText().replace(" rel", "");
                    I_IntSet r_set = tf.newIntSet();
                    r_set.add(r_con.getConceptNid());
                    for (I_GetConceptData val_con : con.getSourceRelTargets(getActiveStatus(), r_set, null, 
                        config.getPrecedence(), config.getConflictResolutionStrategy())) {
                        if (val_con != null) {
                            found = true;
                            out.println("<tr>");
                            out.println("<td>");
                            out.println(escapeString(head));
                            out.println("<td>");
                            // out.println(val_con.getInitialText());
                            //
                            for (I_ExtendByRef mem : tf.getRefsetExtensionMembers(val_con.getConceptNid())) {
                                I_GetConceptData mem_con = tf.getConcept(mem.getComponentId());
                                I_ExtendByRefPart p = mem.getMutableParts().get(0);
                                if (p instanceof I_ExtendByRefPartStr) {
                                    I_ExtendByRefPartStr pccs = (I_ExtendByRefPartStr) p;
                                    out.println("<tr>");
                                    out.println("<td>");
                                    out.println("<td>");
                                    out.println(escapeString(pccs.getStringValue()));
                                } else {
                                    getLog().info("Wrong type: " + p.getClass() + " " + mem_con.getInitialText());
                                }
                            }
                            //
                        }
                    }
                    if (!found) {
                        out.println("<tr>");
                        out.println("<td>");
                        out.println(escapeString(head));
                        out.println("<td>");
                    }
                }
            }
            out.println("</table>");
        }
        out.println("</body>");
        out.println("</html>");
        out.close();

        // make a copy of the report file, in Maven's site folder
        site_output_file.getParentFile().mkdirs();
        copy(list_file, site_output_file);
    }

    private void copy(File source, File destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);
        byte[] buf = new byte[1024];
        int length;
        while ((length = in.read(buf)) > 0) {
            out.write(buf, 0, length);
        }
        in.close();
        out.close();
    }


    // active status "32dc7b19-95cc-365e-99c9-5095124ebe72"

    // Set<? extends I_GetConceptData> childStatuses =
    // status.getDestRelOrigins(null, allowedTypes, null, true, true);

    private I_IntSet getActiveStatus() throws Exception {
        final I_TermFactory tf = Terms.get();
        I_IntSet ret = tf.newIntSet();
        for (Integer s : getCoreDescendants(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid(), null, null)) {
            ret.add(s);
        }
        return ret;
    }

    private ArrayList<Integer> getCoreDescendants(int concept_id, I_IntSet allowed_status, PathBI path)
            throws Exception {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        getCoreDescendants1(concept_id, allowed_status, path, ret);
        return ret;
    }

    private void getCoreDescendants1(int concept_id, I_IntSet allowed_status, PathBI path, ArrayList<Integer> ret)
            throws Exception {
        if (ret.contains(concept_id))
            return;
        ret.add(concept_id);
        for (int ch : getCoreChildren(concept_id, allowed_status, path)) {
            getCoreDescendants1(ch, allowed_status, path, ret);
        }
    }

    private ArrayList<Integer> getCoreChildren(int concept_id, I_IntSet allowed_status, PathBI path) throws Exception {
        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        ArrayList<Integer> ret = new ArrayList<Integer>();
        final I_TermFactory tf = Terms.get();
        I_GetConceptData c = tf.getConcept(concept_id);
        I_IntSet isa_rels = tf.newIntSet();
        isa_rels.add(SNOMED.Concept.IS_A.localize().getNid());
        isa_rels.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        for (I_GetConceptData d : c.getDestRelOrigins(allowed_status, isa_rels, null, 
            config.getPrecedence(), config.getConflictResolutionStrategy())) {
            ret.add(d.getConceptNid());
        }
        Collections.sort(ret, new Comparator<Integer>() {
            public int compare(Integer obj1, Integer obj2) {
                try {
                    String s1 = tf.getConcept(obj1).getInitialText();
                    String s2 = tf.getConcept(obj2).getInitialText();
                    return s1.compareTo(s2);
                } catch (Exception e) {
                }
                return obj1.compareTo(obj2);
            }
        });
        return ret;
    }

    private String escapeString(String str) {
        str = str.replaceAll("\\&", "&amp;");
        str = str.replaceAll("\\\"", "&quot;");
        str = str.replaceAll("\\'", "&apos;");
        str = str.replaceAll("\\<", "&lt;");
        str = str.replaceAll("\\>", "&gt;");
        return str;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            this.listRefset();
        } catch (Exception e) {
            throw new MojoFailureException(e.getLocalizedMessage(), e);
        }
    }

    // generated UUID fb98edf7-779f-4560-b410-4675b94a9d0a current

}
