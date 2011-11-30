package org.ihtsdo.rf2.file.delta.snapshot.validation;

import java.io.File;
import java.util.HashMap;

import org.ihtsdo.rf2.file.delta.snapshot.utils.FileAnalyzer;
import org.ihtsdo.rf2.file.delta.snapshot.utils.FileAnalyzer.FileType;
import org.ihtsdo.rf2.file.delta.snapshot.utils.SortAnalyzer;
import org.ihtsdo.rf2.file.delta.snapshot.utils.SortedSnapshotAnalyzer;

public class InputFileValidator {
	
	static HashMap<String,Boolean> isSortedFile=new HashMap<String,Boolean>();
	static HashMap<String,Boolean> isSortedSnapFile=new HashMap<String,Boolean>();
	public static boolean isSorted(File file, int[] sortColumns) {
		if (isSortedFile.containsKey(file.getAbsolutePath())){
			return isSortedFile.get(file.getAbsolutePath());
		}else{
			SortAnalyzer sa=new SortAnalyzer(file, sortColumns);
			Boolean ret=sa.isSortedFile();
			isSortedFile.put(file.getAbsolutePath(), ret);
			sa=null;
			System.gc();
			return ret;
		}
	}
	
	public static boolean isSnapshot(File file, int[] idColumns) {
		if (isSortedSnapFile.containsKey(file.getAbsolutePath())){
			return isSortedSnapFile.get(file.getAbsolutePath());
		}else{
			SortedSnapshotAnalyzer ssa=new SortedSnapshotAnalyzer(file, idColumns);
			Boolean ret=ssa.isSortedSnapshotFile();
			isSortedSnapFile.put(file.getAbsolutePath(), ret);
			ssa=null;
			System.gc();
			return ret;
		}
	}
	
	public static boolean isValid(File file, FileType fileType, File validationConfig) {
		return FileAnalyzer.validate(file, fileType, validationConfig);
	}

}
