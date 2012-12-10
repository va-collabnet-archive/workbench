package org.ihtsdo.rf2.util.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal patch-bundle-duplicated-desc-lang
 * 
 */
public class PatchRF2DescLang_20130131_mojo extends AbstractMojo {


	public void execute()  {	

			File desc=new File("destination/Full/sct2_Description_Full-en_INT_20130131.txt");
			File lang=new File("destination/Full/der2_cRefset_LanguageFull-en_INT_20130131.txt");
			PatchRF2DescLang_20130131 pdl=new PatchRF2DescLang_20130131(desc,lang);
			pdl.execute();
			pdl=null;
			System.gc();

			desc=new File("destination/Snapshot/sct2_Description_Snapshot-en_INT_20130131.txt");
			lang=new File("destination/Snapshot/der2_cRefset_LanguageSnapshot-en_INT_20130131.txt");
			pdl=new PatchRF2DescLang_20130131(desc,lang);
			pdl.execute();
			pdl=null;
			System.gc();

			desc=new File("destination/Delta/sct2_Description_Delta-en_INT_20130131.txt");
			lang=new File("destination/Delta/der2_cRefset_LanguageDelta-en_INT_20130131.txt");
			pdl=new PatchRF2DescLang_20130131(desc,lang);
			pdl.execute();
			pdl=null;
			System.gc();


	}

}