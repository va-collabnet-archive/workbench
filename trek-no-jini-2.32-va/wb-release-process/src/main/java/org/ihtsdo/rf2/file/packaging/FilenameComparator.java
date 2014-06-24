package org.ihtsdo.rf2.file.packaging;

import java.io.File;
import java.util.Comparator;

public class FilenameComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			if (o1 instanceof File && o2 instanceof File) {
				File f = (File) o1;
				File f1 = (File) o2;

				return f.getName().toString().compareTo(f1.getName().toString());
			}
			return 1;
		}

}
