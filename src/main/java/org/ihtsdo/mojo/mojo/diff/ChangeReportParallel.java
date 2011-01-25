/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.mojo.diff;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;

/**
 *
 * @goal change-report-parallel
 *
 * @phase generate-resources
 *
 * @requiresDependencyResolution compile
 */
public class ChangeReportParallel extends ChangeReportBase {

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
      try {
         // test_p = false;
         test_descendants_p = false;
         //
         super.execute();
         report_dir.mkdirs();
         I_TermFactory tf = Terms.get();
         //getLog().info("Getting SNOMED concepts in DFS order.");
         //ArrayList<Integer> all_concepts = getAllConcepts();
         // getLog().info("Processing: " + all_concepts.size());
         String file_name = report_dir + "/" + "change_report.xml";
         out_xml = new PrintWriter(new BufferedWriter(
                 new OutputStreamWriter(new FileOutputStream(file_name),
                 "UTF-8")));
         out_xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         out_xml.println(startElement("change_report"));
         getLog().info("starting iteration.");
         Ts.get().iterateConceptDataInSequence(new Processor());
         getLog().info("finished iteration.");

         if (out != null) {
            out.close();
         }
         out_xml.println(endElement("change_report"));
         out_xml.close();
         getLog().info("Generating summary report.");
         doSummaryReport();
         getLog().info("Generating concept order list.");
         doConceptList(report_dir + "/concepts.html");
         getLog().info("Sorting for alphabetic order list.");
         sortConcepts(changed_concepts);
         getLog().info("Generating alphabetic order list.");
         doConceptList(report_dir + "/alpha.html");

      } catch (Exception e) {
         throw new MojoFailureException(e.getLocalizedMessage(), e);
      }
   }

   private class Processor implements ProcessUnfetchedConceptDataBI {

      AtomicInteger i = new AtomicInteger();
      NidBitSetBI allConcepts;

      public Processor() throws IOException {
         allConcepts = Ts.get().getAllConceptNids();
      }

      @Override
      public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
         I_GetConceptData c = (I_GetConceptData) fetcher.fetch();
         if (c.getPrimUuid().equals(UUID.fromString("53798ccb-f784-3120-af08-2a0041e43bc3"))) {
            getLog().info("Found test example: " + c);
         }
         changes = "";
         changes_xml = "";
         if (debug_p) {
            System.out.println("Concept: " + c.getInitialText());
         }
         compareAttributes(c);
         compareDescriptions(c);
         compareRelationships(c);
         i.incrementAndGet();
         if (i.get() % 10000 == 0) {
            System.out.println("Processed: " + i);
         }
         if (!changes.equals("")) {
            changed_concepts.add(cNid);
            getOut();
            concept_to_page.put(cNid, cur_page);
            out.println("<p><table border=\"1\" width=\"700\">"
                    + "<col width=\"140\"/><col width=\"270\"/><col width=\"270\"/>"
                    + changes + "</table>");
            out_xml.println(startElement("changed_concept") + "\n"
                    + changes_xml + endElement("changed_concept") + "\n");
         }
      }

      @Override
      public NidBitSetBI getNidSet() throws IOException {
         return allConcepts;
      }

      @Override
      public boolean continueWork() {
         return true;
      }
   }
}
