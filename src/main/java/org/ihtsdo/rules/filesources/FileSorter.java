package org.ihtsdo.rules.filesources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;


public class FileSorter extends AbstractTask {
	
	private File inputFile;
	private File outputFile;
	private File tempFolder;
	private int[] sortColumns;
	private final Integer SPLIT_SIZE = 150000;

	public void execute() {
		try {
			FileHelper.emptyFolder(tempFolder);

			long start1 = System.currentTimeMillis();
			//int ln = countLines(file);

			FileInputStream fis = new FileInputStream(inputFile);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);

			double lines = 1;
			List<String[]> list = new ArrayList<String[]>();
			String nextLine;
			String header=br.readLine();
			while ((nextLine = br.readLine()) != null) {
//				nextLine = new String(nextLine.getBytes(),"UTF-8");
				list.add(nextLine.split("\t"));
				if (lines % SPLIT_SIZE == 0) {
					FileOutputStream fos = new FileOutputStream(new File(tempFolder, "fileno" + lines / SPLIT_SIZE + ".txt"));
					OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
					writeFile(list, osw, sortColumns);
					list = new ArrayList<String[]>();
				}
				lines++;
			}
			if (lines % SPLIT_SIZE != 0) {
				FileOutputStream fos = new FileOutputStream(new File(tempFolder, "fileno" + lines / SPLIT_SIZE + ".txt"));
				OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
				writeFile(list, osw, sortColumns);
			}
			br.close();
			sortFile(outputFile, tempFolder, header);
			long end1 = System.currentTimeMillis();
			long elapsed1 = (end1 - start1);
			System.out.println("Lines in output file  : " + lines);
			System.out.println("Completed in " + elapsed1 + " ms");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public FileSorter(File inputFile, File outputFile, File tempFolder,
			int[] sortColumns) {
		super();
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.tempFolder = tempFolder;
		this.sortColumns = sortColumns;
	}


	public void writeFile(List<String[]> datos, OutputStreamWriter osw, int[] orderColumns) {
		try {
			BufferedWriter bw = new BufferedWriter(osw);
			Collections.sort(datos, new ArrayComparator(orderColumns, false));

			for (String[] row : datos) {
				for (int i = 0; i < row.length; i++) {
					bw.append(row[i]);
					if (i + 1 < row.length) {
						bw.append('\t');
					}
					
				}
				bw.newLine();
			}
			bw.close();
			datos = null;
			System.gc();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public void sortFile( File outputFile, File tempFolder, String header) {
		try {

			if (outputFile.exists()){
				outputFile.delete();
			}
			FileOutputStream fos = new FileOutputStream(outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter sortedBw = new BufferedWriter(osw);
			sortedBw.append(header);
			sortedBw.newLine();
			// Merge
			File[] files = tempFolder.listFiles();

			HashMap<BufferedReader, String[]> readers = new HashMap<BufferedReader, String[]>();
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isHidden()) {
					FileInputStream fis = new FileInputStream(files[i]);
					InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
					BufferedReader bufReader = new BufferedReader(isr);
					String row;
					if ((row = bufReader.readLine()) != null) {
						readers.put(bufReader, row.split("\t"));
					} else {
						bufReader.close();
						files[i].delete();
						readers.remove(bufReader);
					}
				}
			}
			int readerCount = 0;
			int totalReaders = readers.size();
			while (!readers.isEmpty()) {
				String[] smallestRow = CommonUtils.getSmallestArray(readers,sortColumns);
				for (int i = 0; i < smallestRow.length; i++) {
					sortedBw.append(smallestRow[i]);
					if (i + 1 < smallestRow.length) {
						sortedBw.append('\t');
					}
				}
				sortedBw.newLine();

				Iterator<BufferedReader> it = readers.keySet().iterator();
				BufferedReader smallest = null;
				while (it.hasNext()) {
					BufferedReader bufferedReader = (BufferedReader) it.next();
					if (readers.get(bufferedReader).equals(smallestRow)) {
						smallest = bufferedReader;
						break;
					}
				}
				if (smallest.ready()) {
					readers.put(smallest, smallest.readLine().split("\t"));
				} else {
					readerCount++;
					smallest.close();
					readers.remove(smallest);
				}
			}

			sortedBw.close();

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void notifyObservers(String currentTask) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Collection<Condition> getConditions() {
		// TODO Auto-generated method stub
		return null;
	}

}
