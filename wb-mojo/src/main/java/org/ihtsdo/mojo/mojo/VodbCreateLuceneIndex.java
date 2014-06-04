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
package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.lucene.LuceneManager;
import org.ihtsdo.mojo.maven.MojoUtil;

public class VodbCreateLuceneIndex extends AbstractMojo {

    private enum IndexerType {
        Snowball, Standard, Fuzzy
    }

    private IndexerType indexType = IndexerType.Fuzzy;
    /**
     * Location of the lucene directory.
     * 
     * @parameter expression=
     * 
     * 
     * 
     * 
     *            
     *            "${project.build.directory}/generated-resources/berkeley-db/lucene-custom"
     * @required
     */
    private File luceneDirFile;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    private class Indexer implements I_ProcessDescriptions {

        private IndexWriter writer;

        public Indexer() throws IOException {
            super();
            luceneDirFile.mkdirs();
			Directory luceneDir = new SimpleFSDirectory(luceneDirFile);
			
        IndexWriterConfig config = new IndexWriterConfig(LuceneManager.version, new StandardAnalyzer(LuceneManager.version));
        MergePolicy mergePolicy = new LogByteSizeMergePolicy();

        config.setMergePolicy(mergePolicy);
        config.setSimilarity(new LuceneManager.ShortTextSimilarity());

        switch (indexType) {
            case Standard:
            case Fuzzy:

                writer = new IndexWriter(luceneDir, config);
                break;
            case Snowball:
                writer = new IndexWriter(luceneDir, config);
                break;
            }

        }

        public void processDescription(I_DescriptionVersioned<?> desc) throws Exception {
            Document doc = new Document();
            doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("cnid", Integer.toString(desc.getConceptNid()), Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("tnid", Integer.toString(desc.getFirstTuple().getTypeNid()), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
            String lastDesc = null;
            for (I_DescriptionTuple tuple : desc.getTuples()) {
                if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
                    doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.ANALYZED));
                }

            }
            writer.addDocument(doc);
        }

        public void close() throws IOException {
            writer.close();
            writer.close();
        }

    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + luceneDirFile.getCanonicalPath(),
                this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        I_TermFactory termFactory = Terms.get();

        try {
            Indexer descIndexer = new Indexer();
            termFactory.iterateDescriptions(descIndexer);
            descIndexer.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

    }

}
