package org.dwfa.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;


public class JarCreator {
	public static void recursiveAddToZip(JarOutputStream output, File parent,
			String prefix) throws IOException {
		if (parent == null) {
			return;
		}
		for (File child : parent.listFiles()) {
			if (child.isDirectory()) {
				recursiveAddToZip(output, child, prefix);
			} else {
				addToZip(prefix, child, output, null);
			}
		}
	}

	public static void addToZip(String prefix, File f, JarOutputStream output,
			String comment) throws IOException {
		ZipEntry entry = new ZipEntry(prefix + FileIO.getRelativePath(f));
		if (f.exists()) {
			entry.setSize(f.length());
			entry.setTime(f.lastModified());			
		} else {
			entry.setSize(0);
			entry.setTime(System.currentTimeMillis());			
		}
		entry.setComment(comment);
		output.putNextEntry(entry);
		if (f.exists()) {
			FileInputStream fis = new FileInputStream(f);
			byte[] buf = new byte[10240];
			for (int i = 0;; i++) {
				int len = fis.read(buf);
				if (len < 0)
					break;
				output.write(buf, 0, len);
			}
		}
		output.closeEntry();
	}

	public static void addToZip(Class<?> theClass, JarOutputStream output)
			throws IOException, ClassNotFoundException {
		String classFileName = theClass.getName().replace('.', '/') + ".class";
		ZipEntry entry = new ZipEntry(classFileName);
		output.putNextEntry(entry);

		URL classUrl = theClass.getResource("/" + classFileName);
		
		InputStream classInputStream = classUrl.openStream();
		int size = classInputStream.available();
		byte[] data = new byte[size];
		classInputStream.read(data, 0, size);
		output.write(data, 0, size);
		output.closeEntry();
	}

}
