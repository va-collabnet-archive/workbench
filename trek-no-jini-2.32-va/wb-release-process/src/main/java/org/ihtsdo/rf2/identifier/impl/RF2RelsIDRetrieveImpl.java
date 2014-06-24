package org.ihtsdo.rf2.identifier.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RF2RelsIDRetrieveImpl {


	private File sortedSnapshotRf2RelationshipsPrevious;
	private File sortedSnapshotRf2RelationshipExported;
	private File rf2OutputRelationships;
	private File outputUUIDsToAssign;
	private BufferedReader brP;
	private BufferedWriter bw;
	private BufferedWriter bwu;
	private String[] splittedLineP;
	private String concept1;
	private String nextLineP;
	private ArrayList<I_Tuple> tuplePG;
	private String conceptP;
	private ArrayList<I_Tuple> tupleEG;
	private BufferedReader brE;
	private String nextLineE;
	private String[] splittedLineE;
	private String conceptE;
	private File outputDifferences;
	private BufferedWriter bwd;
	private boolean switched;

	public RF2RelsIDRetrieveImpl( File sortedSnapshotRf2RelationshipsPrevious,
			File sortedSnapshotRf2RelationshipExported, File rf2OutputRelationships,
			File outputUUIDsToAssign, File outputDifferences) throws IOException {
		super();
		this.sortedSnapshotRf2RelationshipsPrevious = sortedSnapshotRf2RelationshipsPrevious;
		this.sortedSnapshotRf2RelationshipExported = sortedSnapshotRf2RelationshipExported;
		this.rf2OutputRelationships=rf2OutputRelationships;
		this.outputUUIDsToAssign=outputUUIDsToAssign;
		this.outputDifferences=outputDifferences;
	}
	public void execute() throws Exception{

		FileOutputStream fos = new FileOutputStream( rf2OutputRelationships);
		OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
		bw = new BufferedWriter(osw);

		FileOutputStream fosu = new FileOutputStream( outputUUIDsToAssign);
		OutputStreamWriter oswu = new OutputStreamWriter(fosu,"UTF-8");
		bwu = new BufferedWriter(oswu);

		FileOutputStream fosd = new FileOutputStream( outputDifferences);
		OutputStreamWriter oswd = new OutputStreamWriter(fosd,"UTF-8");
		bwd = new BufferedWriter(oswd);

		FileInputStream fis = new FileInputStream(sortedSnapshotRf2RelationshipExported	);
		InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		brE = new BufferedReader(isr);

		FileInputStream fis2 = new FileInputStream(sortedSnapshotRf2RelationshipsPrevious	);
		InputStreamReader isr2 = new InputStreamReader(fis2,"UTF-8");
		brP = new BufferedReader(isr2);

		//		Prepare files
		String header=brE.readLine();
		brP.readLine();

		bw.append(header);
		bw.append("\r\n");

		bwu.append("WB_UUID");
		bwu.append("\r\n");

		bwd.append("WB_SCTID");
		bwd.append("\t");
		bwd.append("ASSIGNED_SCTID");
		bwd.append("\r\n");


		nextLineP=brP.readLine();
		if (nextLineP!=null){
			splittedLineP=nextLineP.split("\t",-1);
		}

		nextLineE=brE.readLine();
		if (nextLineE!=null){
			splittedLineE=nextLineE.split("\t",-1);
		}
		//		get next concept relationships previous
		getNext_PreviousConceptRels();			

		//		repeat
		int comp=-1;
		do{
			//		
			//	 		get next concept relationships exported
			if (comp<=0)
				getNext_ExportedConceptRels();
			else
				getNext_PreviousConceptRels();	
			
			if (conceptE==null) break;

			if (conceptP==null){
				comp=-1;
			}else{
				comp=conceptE.compareTo(conceptP);
			}
			//			if same concept
			if (comp==0){
				//				
				//				compare AllRelationshipsToFinish (exported tuples, previous tuples)
				//
				AllRelationshipsToFinish();
				//				write concept relationships exported
				writeRelationships();
				//
				//				save exported tuples that can not be assigned
				writeIdNotAssignedListAndDifferenceList();
				//				
				//				get next concept relationships previous
				getNext_PreviousConceptRels();			
				//		
				//			else if exported concept lowest (is new)
			}else if (comp<0){
				//				
				//				write concept relationships exported
				writeRelationships();
				//
				//				save exported tuples that can not be assigned
				writeIdNotAssignedListAndDifferenceList();
				//			end if
			}
			//		
			//		until end of concept relationships exported
		}while (conceptE!=null);

		brE.close();
		brP.close();

		bw.close();
		bwu.close();
		bwd.close();

		System.gc();
	}
	private void writeIdNotAssignedListAndDifferenceList() throws IOException {
		for (I_Tuple tuple:tupleEG){
			String newRelId=((TupleE)tuple).newRelId;
			if (newRelId==null){
				if (tuple.getRelId().contains("-")){
					bwu.append(tuple.getRelId());
					bwu.append("\r\n");
				}else{
					bwd.append(tuple.getRelId());
					bwd.append("\t");
					bwd.append("");
					bwd.append("\r\n");

				}
			}else if ( !tuple.getRelId().contains("-") && newRelId.compareTo(tuple.getRelId())!=0 ){
				bwd.append(tuple.getRelId());
				bwd.append("\t");
				bwd.append(newRelId);
				bwd.append("\r\n");

			}
		}

	}
	private void writeRelationships() throws IOException {
		for (I_Tuple tuple:tupleEG){
			bw.append(tuple.writeTuple());
			bw.append("\r\n");
		}

	}
	private void getNext_PreviousConceptRels()
	throws IOException {
		if (nextLineP!=null){
			conceptP = splittedLineP[4];
			TupleP tupleP= new TupleP(splittedLineP, nextLineP);
			tuplePG=new ArrayList<I_Tuple>();
			tuplePG.add(tupleP);

			while ((nextLineP=brP.readLine())!=null){
				splittedLineP=nextLineP.split("\t",-1);
				concept1=splittedLineP[4];
				if (concept1.compareTo(conceptP)==0){
					tupleP= new TupleP(splittedLineP, nextLineP);
					tuplePG.add(tupleP);
				}else{
					break;
				}

			}
		}else{
			conceptP=null;
		}
	}
	private void getNext_ExportedConceptRels()
	throws IOException {
		if (nextLineE!=null){
			conceptE = splittedLineE[4];
			TupleE tupleE= new TupleE(splittedLineE, nextLineE);
			tupleEG=new ArrayList<I_Tuple>();
			tupleEG.add(tupleE);

			while ((nextLineE=brE.readLine())!=null){
				splittedLineE=nextLineE.split("\t",-1);
				concept1=splittedLineE[4];
				if (concept1.compareTo(conceptE)==0){
					tupleE= new TupleE(splittedLineE, nextLineE);
					tupleEG.add(tupleE);
				}else{
					break;
				}

			}
		}else{
			conceptE=null;
		}
	}

	abstract class Tuple implements I_Tuple {

		public String relId;
		public String effTime;
		public String status;
		public String typeC2;
		public String group;
		public String groupId;
		public String line;
		public Tuple( String[] splittedLine, String line){
			this.relId=splittedLine[0];
			this.status=splittedLine[2];
			this.effTime=splittedLine[1];
			this.typeC2=splittedLine[7]  + "#" + splittedLine[5];
			this.group= splittedLine[6];
			this.groupId="";
			this.line=line;
		}

		/* (non-Javadoc)
		 * @see org.ihtsdo.rf2.identifier.impl.I_Tuple#writeTuple()
		 */
		public String writeTuple(){
			return line;
		}
		public String getGroupNumber(){
			return group;
		}
		public String getGroupId(){
			return groupId;
		}
		public String getTypeC2(){
			return typeC2;
		}
		public String getRelId(){
			return relId;
		}
		public void setGroupId(String groupId){
			this.groupId=groupId ;
		}

		public String getStatus(){
			return status;
		}
		public String getEffTime(){
			return effTime;
		}
		public String getLineWOId(){
			return line.substring(line.indexOf("\t"));
		}
	}
	class TupleP extends Tuple {

		public boolean assigned;
		public TupleP( String[] splittedLine, String line){
			super (splittedLine, line);
			assigned=false;
		}

	}

	class TupleE extends Tuple {

		public String newRelId;
		public TupleE( String[] splittedLine, String line){
			super (splittedLine, line);
		}

		public String writeTuple(){
			if (newRelId!=null)
				return newRelId + getLineWOId();

			return line;
		}
	}

	private void AllRelationshipsToFinish(){

		int withoutId=getCountWOId();
		int previousWithoutId=9999999;
		//		repeat 
		//
		while(withoutId>0 && withoutId<previousWithoutId){
			//		set previousLength to length of exported tuples without id
			//
			previousWithoutId=withoutId;
			//		compareRelationships(tuple[] exported tuples, tuple[] previous tuples)
			//
			compareRelationships();

			withoutId=getCountWOId();
			//		until length exported tuples without id equal to previousLength
		}
	}

	private int getCountWOId() {
		int ret=0;
		for (I_Tuple tupleE:tupleEG){
			if (((TupleE)tupleE).newRelId==null) ret++ ;
		}
		return ret;
	}

	private void compareRelationships(){

		for (I_Tuple tuple:tupleEG){
			TupleE tupleE=(TupleE)tuple;
			//		if relationship has not id assigned by this process
			if (tupleE.newRelId==null){
				//
				//		set existsPrev to true if relationship exists in previous tuples (not assigned yet)
				boolean existPrev= getExistsPrev(tupleE);
				//
				//		if existsPrev 
				if (existPrev){
					//
					ArrayList<I_Tuple> tupleED=getDuplicatedExported(tupleE);
					//		set isduplicatedExp to true if there is identical relationship in exported tuples (without assigned id yet)
					//					
					//		set isduplicatedPrev to true if there is identical relationship in previous tuples (not assigned yet)
					ArrayList<I_Tuple> tuplePD=getDuplicatedPrevious(tupleE);
					//
					//		if not isduplicatedExp and not isduplicatedPrev
					if (tupleED.size()==1 && tuplePD.size()==1){
						//
						//		set prev id to exported relationship
						tupleE.newRelId=tuplePD.get(0).getRelId();
						//
						//		set previous relationship to assigned
						((TupleP)tuplePD.get(0)).assigned=true;
						continue;
						//
						//		end if
					}
					//
					//		if isduplicatedExp and not isduplicatedPrev
					if (tupleED.size()>1 && tuplePD.size()==1){
						//
						if (tupleE.getStatus().compareTo("0")==0){
							tupleE.newRelId=tuplePD.get(0).getRelId();
						}else{
							TupleE bmTupleE=(TupleE)getBestMatchTuple(tuplePD.get(0), tupleED);

							//		if can determine which exported duplicated relationship match to previous relationship
							if (bmTupleE!=null){
								//
								//		set prev id to best match exported duplicated relationship 
								bmTupleE.newRelId=tuplePD.get(0).getRelId();
								//
								//		set previous relationship to assigned
							}
							//	
							//		else
							else{
								//
								//			set  prev id to exported relationship
								tupleE.newRelId=tuplePD.get(0).getRelId();
								//
								//			set previous relationship to assigned
								//
								//			end if
							}
						}
						((TupleP)tuplePD.get(0)).assigned=true;
						continue;
						//
						//			end if
					}
					//
					//			if not isduplicatedExp and isduplicatedPrev
					if (tupleED.size()==1 && tuplePD.size()>1){
						//
						//			if can determine which previous duplicated relationship match to exported tuple
						TupleP bmTupleP=(TupleP)getBestMatchTuple(tupleE, tuplePD);
						//
						if (bmTupleP!=null){

							//			set best match id of previous duplicated relationship to exported relationship
							tupleE.newRelId=bmTupleP.getRelId();
							//
							//			set best match previous relationship to assigned
							bmTupleP.assigned=true;
							continue;
						}
						//
						//			else
						else{
							//
							//				set first relationship id in previous duplicated tuples to exported relationship
							tupleE.newRelId=tuplePD.get(0).getRelId();
							//
							//				set first relationship in previous duplicated tuples to assigned
							((TupleP)tuplePD.get(0)).assigned=true;
							continue;
							//
							//				end if
						}
						//
						//				end if	
					}
					//
					//				if isduplicatedExp and isduplicatedPrev
					if (tupleED.size()>1 && tuplePD.size()>1){
						//
						TupleP bmTupleP=(TupleP)getBestMatchTuple(tupleE, tuplePD);
						//
						if (bmTupleP!=null){

							//									set best match id of previous duplicated relationship to exported relationship
							tupleE.newRelId=bmTupleP.getRelId();
							//
							//									set best match previous relationship to assigned
							bmTupleP.assigned=true;
							continue;
						}
						//
						//				end if
					}
					//
					//				end if
					//
				}
			}
		}


	}

	private ArrayList<I_Tuple> getDuplicatedPrevious(TupleE tupleE) {
		ArrayList<I_Tuple> tuplePD=new ArrayList<I_Tuple>();
		for (I_Tuple tuple: tuplePG){
			TupleP tupleP=(TupleP) tuple;
			if (!tupleP.assigned && tupleP.typeC2.compareTo(tupleE.typeC2)==0){
				tuplePD.add(tupleP);
			}
		}
		return tuplePD;
	}

	private ArrayList<I_Tuple> getDuplicatedExported(TupleE tupleE) {
		ArrayList<I_Tuple> tupleED=new ArrayList<I_Tuple>();
		for (I_Tuple tuple: tupleEG){
			TupleE aTupleE=(TupleE)tuple;
			if (aTupleE.newRelId==null && aTupleE.typeC2.compareTo(tupleE.typeC2)==0) {
				tupleED.add(aTupleE);

			}
		}
		return tupleED;
	}

	private boolean getExistsPrev(TupleE tupleE) {
		for (I_Tuple tuple: tuplePG){
			TupleP tupleP=(TupleP) tuple;
			if (tupleP.typeC2.compareTo(tupleE.typeC2)==0) return true;
		}
		return false;
	}

	private I_Tuple getBestMatchTuple(I_Tuple rel, ArrayList<I_Tuple> duplicatedRels){

		I_Tuple tuple=null;
		if (rel.getStatus().compareTo("1")==0){

			tuple=determineBestMatchByGroupId ( rel,  duplicatedRels);

			if (tuple!=null)
				return tuple;
			tuple=determineBestMatchByStatusEffTime( rel, duplicatedRels);

			if (tuple!=null)
				return tuple;

			tuple= determineBestMatchByRelId( rel, duplicatedRels);
		}else{

			tuple= determineBestMatchByRelId( rel, duplicatedRels);
			if (tuple!=null)
				return tuple;

			switched=false;
			tuple=determineBestMatchByExactLine( rel, duplicatedRels);
			if (tuple!=null)
				return tuple;

			if (!switched)
				tuple=determineBestMatchByGroupId ( rel,  duplicatedRels);


		}

		return tuple;
	}

	private I_Tuple determineBestMatchByExactLine(I_Tuple rel,ArrayList<I_Tuple> duplicatedRels) {

		String relline=rel.getLineWOId();
		I_Tuple candidate=null;
		for (I_Tuple tuple:duplicatedRels){

			//		if rel.groupId=relD.groupId
			if(relline.compareTo(tuple.getLineWOId())==0){
				//
				//		set bestMatch to relD
				if (candidate!=null){
					switched=true;
					return null;
				}

				candidate=tuple;
				//
				//		exit
				//
				//		end if
			}
			//
			//		next
		}
		return candidate;
	}
	private I_Tuple determineBestMatchByGroupId (I_Tuple rel, ArrayList<I_Tuple> duplicatedRels){

		//		set groupid for rel
		setGroupId(rel);
		//
		//		for each relD in duplicatedRels
		for (I_Tuple tuple:duplicatedRels){

			//		set groupid for relD
			setGroupId (tuple);
			//
			//		if rel.groupId=relD.groupId
			if(rel.getGroupId().compareTo(tuple.getGroupId())==0){
				//
				//		set bestMatch to relD
				return tuple;
				//
				//		exit
				//
				//		end if
			}
			//
			//		next
		}
		return null;
	}

	private void setGroupId(I_Tuple rel) {
		if (rel.getStatus().compareTo("0")==0){
			rel.setGroupId("IR" + rel.getTypeC2());
			return;
		}
		if (rel.getGroupId().compareTo("")==0 && rel.getGroupNumber().compareTo("0")!=0){
			ArrayList<I_Tuple> tupleG=null;
			if (rel instanceof TupleE){
				tupleG=tupleEG;
			}else{
				tupleG=tuplePG;
			}
			List<String>typeTgt=new ArrayList<String>();
			ArrayList<I_Tuple> tupleGroup=new ArrayList<I_Tuple>();
			for (I_Tuple tuple:tupleG){
				if (tuple.getGroupNumber().compareTo(rel.getGroupNumber())==0 && 
						tuple.getStatus().compareTo("1")==0){
					typeTgt.add(tuple.getTypeC2());
					tupleGroup.add(tuple);
				}
			}
			Collections.sort(typeTgt);
			String groupId="";
			for (int i=0;i<typeTgt.size();i++){
				groupId+="R" + typeTgt.get(i);
			}
			for (I_Tuple tuple:tupleGroup){
				tuple.setGroupId(groupId);
			}
		}
	}
	private I_Tuple determineBestMatchByRelId(I_Tuple rel, ArrayList<I_Tuple> duplicatedRels){

		//		for each relD in duplicatedRels
		for (I_Tuple tuple:duplicatedRels){
			//
			//		if rel.relationshipId==relD.relationshipId
			if (rel.getRelId().compareTo(tuple.getRelId())==0){
				//
				//		set bestMatch to relD
				return tuple;
				//
				//		exit
				//
				//		end if
			}
			//
			//		next
		}
		return null;
	}

	private I_Tuple determineBestMatchByStatusEffTime(I_Tuple rel, ArrayList<I_Tuple> duplicatedRels){

		//		for each relD in duplicatedRels
		for (I_Tuple tuple:duplicatedRels){
			//
			//		if rel.status==relD.status and rel.effectiveTime=relD.effectiveTime
			if (rel.getStatus().compareTo(tuple.getStatus())==0 && rel.getEffTime().compareTo(tuple.getEffTime())==0){
				//
				//		set bestMatch to relD
				return tuple;
				//
				//		exit
				//
				//		end if
			}
			//
			//		next
		}
		//
		//		for each relD in duplicatedRels
		for (I_Tuple tuple:duplicatedRels){
			//
			//		if rel.effectiveTime==relD.effectiveTime
			if (rel.getEffTime().compareTo(tuple.getEffTime())==0){
				//
				//		set bestMatch to relD
				return tuple;
				//
				//		exit
				//
				//		end if
			}
			//
			//		next
		}
		//
		//		for each relD in duplicatedRels
		for (I_Tuple tuple:duplicatedRels){
			//
			//		if rel.status==relD.status
			if (rel.getStatus().compareTo(tuple.getStatus())==0 ){
				//
				//		set bestMatch to relD
				//
				//		exit
				return tuple;
				//
				//		end if
			}
			//
			//		next
		}

		return null;
	}
}
