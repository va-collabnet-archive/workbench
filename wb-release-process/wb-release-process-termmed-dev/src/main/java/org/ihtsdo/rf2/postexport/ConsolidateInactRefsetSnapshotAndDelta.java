package org.ihtsdo.rf2.postexport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;

public class ConsolidateInactRefsetSnapshotAndDelta extends AbstractTask {

	private static final int STATUS_COLUMN = 6;
	private File snapshotSortedPreviousfile;
	private File snapshotSortedExportedfile;
	private File snapshotFinalFile;
	private Integer[] fieldsToCompare;
	private int index;
	private String releaseDate;
	private String newLine="\r\n";
	private int colLen;
	private File deltaFinalFile;
	private BufferedWriter bw;
	private BufferedWriter dbw;

	public ConsolidateInactRefsetSnapshotAndDelta(FILE_TYPE fType,
			File snapshotSortedPreviousfile, File snapshotSortedExportedfile,
			File snapshotFinalFile, File deltaFinalFile, String releaseDate) {
		this.snapshotSortedPreviousfile=snapshotSortedPreviousfile;	
		this.snapshotSortedExportedfile=snapshotSortedExportedfile;
		this.snapshotFinalFile=snapshotFinalFile;
		this.deltaFinalFile=deltaFinalFile;
		this.fieldsToCompare=fType.getColumnsToCompare();
		this.index=fType.getSnapshotIndex();
		this.releaseDate=releaseDate;
	}

	@Override
	public void execute() throws Exception {

		try {


			FileOutputStream fos = new FileOutputStream( snapshotFinalFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			bw = new BufferedWriter(osw);

			FileOutputStream dfos = new FileOutputStream( deltaFinalFile);
			OutputStreamWriter dosw = new OutputStreamWriter(dfos,"UTF-8");
			dbw = new BufferedWriter(dosw);

			FileInputStream fis = new FileInputStream(snapshotSortedPreviousfile	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br1 = new BufferedReader(isr);

			FileInputStream fis2 = new FileInputStream(snapshotSortedExportedfile	);
			InputStreamReader isr2 = new InputStreamReader(fis2,"UTF-8");
			BufferedReader br2 = new BufferedReader(isr2);


			double lines=0;
			String line1;
			String header=br1.readLine();
			br2.readLine();

			bw.append(header);
			bw.append(newLine);
			dbw.append(header);
			dbw.append(newLine);
			
			String[] columns=header.split("\t",-1);
			colLen=columns.length;
			String[] splittedLine1;
			String line2;
			String[] splittedLine2=null;

			line2=br2.readLine();
			if (line2!=null){
				splittedLine2=line2.split("\t",-1);
			}			

			while ((line1= br1.readLine()) != null) {
				splittedLine1 = line1.split("\t",-1);

				if (line2!=null){
					int comp = splittedLine1[index].compareTo(splittedLine2[index]);
					if ( comp<0){
							addPreviousLine(splittedLine1);
							lines++;
					}else{
						if (comp>0){
							while (comp>0){
								if (splittedLine2[STATUS_COLUMN].compareTo("")!=0){
									addExportedLine(splittedLine2);
									lines++;
								}
								line2=br2.readLine();
								if (line2==null){
									comp=-1;
									break;
								}
								splittedLine2=line2.split("\t",-1);
								comp = splittedLine1[index].compareTo(splittedLine2[index]);
							}
							if ( comp<0){
								addPreviousLine(splittedLine1);
								lines++;
							}
						}
						while(comp==0){
							// if status id blank then to take previous status
							if (splittedLine2[STATUS_COLUMN].compareTo("")==0){
								splittedLine2[STATUS_COLUMN]=splittedLine1[STATUS_COLUMN];
							}
							if (fieldsCompare(splittedLine1,splittedLine2)!=0){
								addExportedLine(splittedLine2);
								lines++;
							}else{
								addPreviousLine(splittedLine1);
								lines++;		
							}
							line2=br2.readLine();
							if (line2==null){
								break;
							}
							splittedLine2=line2.split("\t",-1);
							comp = splittedLine1[index].compareTo(splittedLine2[index]);

						}
					}
				}else{
					addPreviousLine(splittedLine1);
					lines++;
				}
			}

			if (line2!=null){

				if (splittedLine2[STATUS_COLUMN].compareTo("")!=0){
					addExportedLine(splittedLine2);
					lines++;
				}
				while ((line2= br2.readLine()) != null) {
					splittedLine2=line2.split("\t",-1);
					if (splittedLine2[STATUS_COLUMN].compareTo("")!=0){
						addExportedLine(splittedLine2);
						lines++;
					}
				}
			}
			br1.close();
			br2.close();
			bw.close();
			dbw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void addExportedLine( String[] splittedLine) throws Exception {
		StringBuffer sb=new StringBuffer();
		for (int i = 0; i < colLen; i++) {
			if (i==1){
				sb.append(releaseDate);
				
			}else{
			
				sb.append(splittedLine[i]);
			}
			if (i + 1 < colLen) {
				sb.append('\t');
			}
		}
		sb.append(newLine);
		String tmp=sb.toString();
		bw.append(tmp);
		dbw.append(tmp);
	}

	private void addPreviousLine(String[] splittedLine) throws Exception {
		for (int i = 0; i < splittedLine.length; i++) {
			bw.append(splittedLine[i]);
			if (i + 1 < splittedLine.length) {
				bw.append('\t');
			}
		}
		bw.append(newLine);
	}


	private int fieldsCompare(String[] splittedLine1, String[] splittedLine2) {
		int iComp;
		for (int i : fieldsToCompare){
			iComp=splittedLine1[i].compareTo(splittedLine2[i]);
			if (iComp!=0)
				return iComp;
		}
		return 0;
	}

	

}

