/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.contradiction.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.ihtsdo.helper.bdb.MultiEditorContradictionDetector;
import org.ihtsdo.tk.uuid.UuidT5Generator;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author akf
 */
public class ContradictionTest {
    
    int userProjectPathNid = 100;
    int anotherUserProjectPathNid = 200;
    boolean ignoreNonVisibleAth = true;
    ConcurrentHashMap<Integer, ConcurrentSkipListSet<UUID>> projectPathNidTimeAthMap = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<UUID>>(); //make
    ConcurrentSkipListSet<Integer> conflictSaps; //gets populated during conflict detection
    ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>> timeAthStampNidMap = new ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>>();
    ConcurrentSkipListSet<Integer> componentsMissingCommitRecord = new ConcurrentSkipListSet<Integer>(); //gets populated during conflict detection
    HashMap<UUID, String> allConceptComputedAthAllMap = new HashMap<UUID, String> (); //make
    MockConcept conceptA;
    MockConcept conceptB;
    MockConcept conceptC;
    UUID ath1;
    UUID ath2;
    UUID ath3;
    UUID ath4;
    UUID ath5;
    UUID ath6;
    UUID ath7;
    UUID ath8;
   

    @Before
    public void setUp() throws Exception {
        //set up class variables
        ignoreNonVisibleAth = true;
    
        //author uuid strings
        String author1 = "4b1ae8f0-cd2e-11e1-9b23-0800200c9a66";
        String author2 = "5c8b0571-cd2e-11e1-9b23-0800200c9a66";
        String author3 = "67db4ac0-cd2e-11e1-9b23-0800200c9a66";
        
        //stamp nids
        int stamp1 = 1;
        int stamp2 = 2;
        int stamp3 = 3;
        int stamp4 = 4;
        int stamp5 = 5;
        int stamp6 = 6;
        int stamp7 = 7;
        int stamp8 = 8;
        int stamp9 = 9;
        int stamp10 = 10;
        int stamp11 = 11;
        
        //times
        Long time1 = System.currentTimeMillis();
        Long time2 = System.currentTimeMillis() + 1;
        Long time3 = System.currentTimeMillis() + 2;
        Long time4 = System.currentTimeMillis() + 3;
        Long time5 = System.currentTimeMillis() + 4;
        
        //author time hashes
        ath1 = makeAuthorTimeHash(author1, time1);
        ath2 = makeAuthorTimeHash(author2, time2);
        ath3 = makeAuthorTimeHash(author3, time3);
        ath4 = makeAuthorTimeHash(author1, time4);
        ath5 = makeAuthorTimeHash(author2, time5);
        ath6 = makeAuthorTimeHash(author3, time5);
        ath7 = makeAuthorTimeHash(author2, time1);
        ath8 = makeAuthorTimeHash(author3, time4);
        
        //make time author -> stamp nid map
        //maps which stamps are valid for an Author Time Hash (ATH)
        timeAthStampNidMap = new ConcurrentHashMap<UUID, ConcurrentSkipListSet<Integer>>();
        ConcurrentSkipListSet<Integer> set1 = new ConcurrentSkipListSet<Integer>();
        set1.add(stamp1);
        set1.add(stamp2);
        timeAthStampNidMap.put(ath1, set1);
        ConcurrentSkipListSet<Integer> set2 = new ConcurrentSkipListSet<Integer>();
        set2.add(stamp3);
        timeAthStampNidMap.put(ath2, set2);
        ConcurrentSkipListSet<Integer> set3 = new ConcurrentSkipListSet<Integer>();
        set3.add(stamp4);
        timeAthStampNidMap.put(ath3, set3);
        ConcurrentSkipListSet<Integer> set4 = new ConcurrentSkipListSet<Integer>();
        set4.add(stamp5);
        timeAthStampNidMap.put(ath4, set4);
        ConcurrentSkipListSet<Integer> set5 = new ConcurrentSkipListSet<Integer>();
        set5.add(stamp6);
        timeAthStampNidMap.put(ath5, set5);
        ConcurrentSkipListSet<Integer> set6 = new ConcurrentSkipListSet<Integer>();
        set6.add(stamp7);
        timeAthStampNidMap.put(ath6, set6);
        ConcurrentSkipListSet<Integer> set7 = new ConcurrentSkipListSet<Integer>();
        set7.add(stamp8);
        timeAthStampNidMap.put(ath7, set7);
        ConcurrentSkipListSet<Integer> set8 = new ConcurrentSkipListSet<Integer>();
        set8.add(stamp9);
        timeAthStampNidMap.put(ath8, set8);
        
        //make project path -> time author map]
        ConcurrentSkipListSet<UUID> athTimeSet = new ConcurrentSkipListSet<UUID>();
        athTimeSet.add(ath1);
        athTimeSet.add(ath2);
        athTimeSet.add(ath3);
        athTimeSet.add(ath4);
        athTimeSet.add(ath5);
        athTimeSet.add(ath6);
        projectPathNidTimeAthMap.put(userProjectPathNid, athTimeSet);
        ConcurrentSkipListSet<UUID> anotherAthTimeSet = new ConcurrentSkipListSet<UUID>();
        anotherAthTimeSet.add(ath7);
        anotherAthTimeSet.add(ath8);
        projectPathNidTimeAthMap.put(anotherUserProjectPathNid, anotherAthTimeSet);
        
        //make concepts
        HashSet<UUID> set1conceptA = new HashSet<UUID>();
        HashSet<UUID> set2conceptA = new HashSet<UUID>();
        ArrayList<HashSet<UUID>> listConceptA = new ArrayList<HashSet<UUID>>();
        HashSet<Integer> stampsConceptA = new HashSet<Integer>();
        set1conceptA.add(ath1);
        set2conceptA.add(ath1);
        set2conceptA.add(ath2);
        listConceptA.add(set1conceptA);
        listConceptA.add(set2conceptA);
        HashMap<Integer, ArrayList<HashSet<UUID>>> bigMapA = new HashMap<Integer, ArrayList<HashSet<UUID>>>();
        bigMapA.put(userProjectPathNid, listConceptA);
        stampsConceptA.add(stamp1);
        stampsConceptA.add(stamp3);
        conceptA = new MockConcept(1, bigMapA, stampsConceptA);
        
        HashSet<UUID> set1conceptB = new HashSet<UUID>();
        HashSet<UUID> set2conceptB = new HashSet<UUID>();
        ArrayList<HashSet<UUID>> listConceptB = new ArrayList<HashSet<UUID>>();
        HashSet<Integer> stampsConceptB = new HashSet<Integer>();
        set1conceptB.add(ath1);
        set2conceptB.add(ath2);
        listConceptB.add(set1conceptB);
        listConceptB.add(set2conceptB);
        HashMap<Integer, ArrayList<HashSet<UUID>>> bigMapB = new HashMap<Integer, ArrayList<HashSet<UUID>>>();
        bigMapB.put(userProjectPathNid, listConceptB);
        stampsConceptB.add(stamp2);
        stampsConceptB.add(stamp3);
        conceptB = new MockConcept(2, bigMapB, stampsConceptB);
        HashSet<UUID> set3conceptB = new HashSet<UUID>();
        HashSet<UUID> set4conceptB = new HashSet<UUID>();
        ArrayList<HashSet<UUID>> anotherListConceptB = new ArrayList<HashSet<UUID>>();
        set3conceptB.add(ath7);
        set4conceptB.add(ath8);
        anotherListConceptB.add(set3conceptB);
        anotherListConceptB.add(set4conceptB);
        conceptB.getStampsNids().add(stamp10);
        conceptB.getStampsNids().add(stamp11);
        conceptB.athTimesCommit.put(anotherUserProjectPathNid, anotherListConceptB);
        
        HashSet<UUID> set1conceptC = new HashSet<UUID>();
        HashSet<UUID> set2conceptC = new HashSet<UUID>();
        HashSet<UUID> set3conceptC = new HashSet<UUID>();
        HashSet<UUID> set4conceptC = new HashSet<UUID>();
        ArrayList<HashSet<UUID>> listConceptC = new ArrayList<HashSet<UUID>>();
        HashSet<Integer> stampsConceptC = new HashSet<Integer>();
        set1conceptC.add(ath3);
        set2conceptC.add(ath3);
        set2conceptC.add(ath4);
        set3conceptC.add(ath3);
        set3conceptC.add(ath4);
        set3conceptC.add(ath5);
        set4conceptC.add(ath3);
        set4conceptC.add(ath4);
        set4conceptC.add(ath6);
        listConceptC.add(set1conceptC);
        listConceptC.add(set2conceptC);
        listConceptC.add(set3conceptC);
        listConceptC.add(set4conceptC);
        HashMap<Integer, ArrayList<HashSet<UUID>>> bigMapC = new HashMap<Integer, ArrayList<HashSet<UUID>>>();
        bigMapC.put(userProjectPathNid, listConceptC);
        stampsConceptC.add(stamp4);
        stampsConceptC.add(stamp5);
        stampsConceptC.add(stamp6);
        stampsConceptC.add(stamp7);
        conceptC = new MockConcept(3, bigMapC, stampsConceptC);
    }
    
    
    
    
    @Test
    public void testContradictionDetectionConceptA(){
        //CONCEPT A -- no contradicion

        //make concept specific maps
        ArrayList<HashSet<UUID>> commitRefsetAthSetsList = conceptA.getAthTimesCommitByPath(userProjectPathNid);
        ArrayList<HashSet<UUID>> replacementSet = new ArrayList<HashSet<UUID>>();

        HashMap<UUID, String> conceptComputedAthAllMap = new HashMap<UUID, String>();
        Iterator<HashSet<UUID>> iterator = commitRefsetAthSetsList.iterator();
        while(iterator.hasNext()){
            HashSet<UUID> uuidSet = iterator.next();
            for(UUID uuid: uuidSet){
                if(!conceptComputedAthAllMap.containsKey(uuid)){
                    String string = allConceptComputedAthAllMap.get(uuid);
                    conceptComputedAthAllMap.put(uuid, string);
                }
            }
        }
        HashMap<UUID, String> conceptComputedAthDiffAllMap = conceptComputedAthAllMap; //No difference for now in test

        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String> (); //leave empty
        HashMap<UUID, String> conceptMissingAthMap = new HashMap<UUID, String> (); //leave empty
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList = new ArrayList<HashSet<UUID>>(); //leave empty
        HashSet<UUID> accumDiffSet = new HashSet<UUID>(); 
        try {
            accumDiffSet = MultiEditorContradictionDetector.contradictionDetectionAlgorithm(
                                                  conceptA.getId(),
                                                  userProjectPathNid,
                                                  ignoreNonVisibleAth,
                                                  projectPathNidTimeAthMap,
                                                  conflictSaps,
                                                  timeAthStampNidMap,
                                                  componentsMissingCommitRecord,
                                                  commitRefsetAthSetsList, //concept level
                                                  replacementSet, //concept level
                                                  conceptComputedAthAllMap, //concept level
                                                  conceptComputedAthDiffAllMap, //concept level
                                                  conceptComputedAthMap, //empty
                                                  conceptMissingAthMap, //empty
                                                  truthRefsetAthSetsList); //empty
        } catch (IOException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Assert.assertTrue("ConceptA has a false contradiction."
                + " Concept should have no contradictions." + accumDiffSet, accumDiffSet.isEmpty());
    }
    
    @Test
    public void testContradictionDetectionConceptB(){
        //CONCEPT B -- ATH1 and ATH2

        //make concept specific maps
        ArrayList<HashSet<UUID>> commitRefsetAthSetsList = conceptB.getAthTimesCommitByPath(userProjectPathNid);
        ArrayList<HashSet<UUID>> replacementSet = new ArrayList<HashSet<UUID>>();

        HashMap<UUID, String> conceptComputedAthAllMap = new HashMap<UUID, String>();
        Iterator<HashSet<UUID>> iterator = commitRefsetAthSetsList.iterator();
        while(iterator.hasNext()){
            HashSet<UUID> uuidSet = iterator.next();
            for(UUID uuid: uuidSet){
                if(!conceptComputedAthAllMap.containsKey(uuid)){
                    String string = allConceptComputedAthAllMap.get(uuid);
                    conceptComputedAthAllMap.put(uuid, string);
                }
            }
        }
        HashMap<UUID, String> conceptComputedAthDiffAllMap = conceptComputedAthAllMap; //No difference for now in test

        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String> (); //leave empty
        HashMap<UUID, String> conceptMissingAthMap = new HashMap<UUID, String> (); //leave empty
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList = new ArrayList<HashSet<UUID>>(); //leave empty
        HashSet<UUID> accumDiffSet = new HashSet<UUID>(); 
        try {
            accumDiffSet = MultiEditorContradictionDetector.contradictionDetectionAlgorithm(
                                                  conceptB.getId(),
                                                  userProjectPathNid,
                                                  ignoreNonVisibleAth,
                                                  projectPathNidTimeAthMap,
                                                  conflictSaps,
                                                  timeAthStampNidMap,
                                                  componentsMissingCommitRecord,
                                                  commitRefsetAthSetsList, //concept level
                                                  replacementSet, //concept level
                                                  conceptComputedAthAllMap, //concept level
                                                  conceptComputedAthDiffAllMap, //concept level
                                                  conceptComputedAthMap, //empty
                                                  conceptMissingAthMap, //empty
                                                  truthRefsetAthSetsList); //empty
        } catch (IOException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("AccumDiffSet: " + accumDiffSet);
//        System.out.println("ATH 1: " + ath1);
//        System.out.println("ATH 2: " + ath2);
//        System.out.println("ATH 3: " + ath3);
//        System.out.println("ATH 4: " + ath4);
//        System.out.println("ATH 5: " + ath5);
//        System.out.println("ATH 6: " + ath6);
        Assert.assertTrue("ConceptB doesn't have the correct contradictions.",accumDiffSet.contains(ath1) && accumDiffSet.contains(ath2));
    }
    
    @Test
    public void testSecondPathContradictionDetectionConceptB(){
        //CONCEPT B -- ATH1 and ATH2

        //make concept specific maps
        ArrayList<HashSet<UUID>> commitRefsetAthSetsList = conceptB.getAthTimesCommitByPath(anotherUserProjectPathNid);
        
        ArrayList<HashSet<UUID>> replacementSet = new ArrayList<HashSet<UUID>>();

        HashMap<UUID, String> conceptComputedAthAllMap = new HashMap<UUID, String>();
        Iterator<HashSet<UUID>> iterator = commitRefsetAthSetsList.iterator();
        while(iterator.hasNext()){
            HashSet<UUID> uuidSet = iterator.next();
            for(UUID uuid: uuidSet){
                if(!conceptComputedAthAllMap.containsKey(uuid)){
                    String string = allConceptComputedAthAllMap.get(uuid);
                    conceptComputedAthAllMap.put(uuid, string);
                }
            }
        }
        HashMap<UUID, String> conceptComputedAthDiffAllMap = conceptComputedAthAllMap; //No difference for now in test

        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String> (); //leave empty
        HashMap<UUID, String> conceptMissingAthMap = new HashMap<UUID, String> (); //leave empty
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList = new ArrayList<HashSet<UUID>>(); //leave empty
        HashSet<UUID> accumDiffSet = new HashSet<UUID>(); 
        try {
            accumDiffSet = MultiEditorContradictionDetector.contradictionDetectionAlgorithm(
                                                  conceptB.getId(),
                                                  anotherUserProjectPathNid,
                                                  ignoreNonVisibleAth,
                                                  projectPathNidTimeAthMap,
                                                  conflictSaps,
                                                  timeAthStampNidMap,
                                                  componentsMissingCommitRecord,
                                                  commitRefsetAthSetsList, //concept level
                                                  replacementSet, //concept level
                                                  conceptComputedAthAllMap, //concept level
                                                  conceptComputedAthDiffAllMap, //concept level
                                                  conceptComputedAthMap, //empty
                                                  conceptMissingAthMap, //empty
                                                  truthRefsetAthSetsList); //empty
        } catch (IOException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("AccumDiffSet: " + accumDiffSet);
//        System.out.println("ATH 1: " + ath1);
//        System.out.println("ATH 2: " + ath2);
//        System.out.println("ATH 3: " + ath3);
//        System.out.println("ATH 4: " + ath4);
//        System.out.println("ATH 5: " + ath5);
//        System.out.println("ATH 6: " + ath6);
//        System.out.println("ATH 7: " + ath7);
//        System.out.println("ATH 8: " + ath8);
        Assert.assertTrue("ConceptB doesn't have the correct contradictions.",
                accumDiffSet.contains(ath7) && accumDiffSet.contains(ath8));
    }
        
   @Test
    public void testContradictionDetectionConceptC(){
        //CONCEPT B -- ATH1 and ATH2

        //make concept specific maps
        ArrayList<HashSet<UUID>> commitRefsetAthSetsList = conceptC.getAthTimesCommitByPath(userProjectPathNid);
        ArrayList<HashSet<UUID>> replacementSet = new ArrayList<HashSet<UUID>>();

        HashMap<UUID, String> conceptComputedAthAllMap = new HashMap<UUID, String>();
        Iterator<HashSet<UUID>> iterator = commitRefsetAthSetsList.iterator();
        while(iterator.hasNext()){
            HashSet<UUID> uuidSet = iterator.next();
            for(UUID uuid: uuidSet){
                if(!conceptComputedAthAllMap.containsKey(uuid)){
                    String string = allConceptComputedAthAllMap.get(uuid);
                    conceptComputedAthAllMap.put(uuid, string);
                }
            }
        }
        HashMap<UUID, String> conceptComputedAthDiffAllMap = conceptComputedAthAllMap; //No difference for now in test

        HashMap<UUID, String> conceptComputedAthMap = new HashMap<UUID, String> (); //leave empty
        HashMap<UUID, String> conceptMissingAthMap = new HashMap<UUID, String> (); //leave empty
        ArrayList<HashSet<UUID>> truthRefsetAthSetsList = new ArrayList<HashSet<UUID>>(); //leave empty
        HashSet<UUID> accumDiffSet = new HashSet<UUID>(); 
        try {
            accumDiffSet = MultiEditorContradictionDetector.contradictionDetectionAlgorithm(
                                                  conceptC.getId(),
                                                  userProjectPathNid,
                                                  ignoreNonVisibleAth,
                                                  projectPathNidTimeAthMap,
                                                  conflictSaps,
                                                  timeAthStampNidMap,
                                                  componentsMissingCommitRecord,
                                                  commitRefsetAthSetsList, //concept level
                                                  replacementSet, //concept level
                                                  conceptComputedAthAllMap, //concept level
                                                  conceptComputedAthDiffAllMap, //concept level
                                                  conceptComputedAthMap, //empty
                                                  conceptMissingAthMap, //empty
                                                  truthRefsetAthSetsList); //empty
        } catch (IOException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ContradictionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("AccumDiffSet: " + accumDiffSet);
//        System.out.println("ATH 1: " + ath1);
//        System.out.println("ATH 2: " + ath2);
//        System.out.println("ATH 3: " + ath3);
//        System.out.println("ATH 4: " + ath4);
//        System.out.println("ATH 5: " + ath5);
//        System.out.println("ATH 6: " + ath6);
        Assert.assertTrue("ConceptC doesn't have the correct contradictions.", accumDiffSet.contains(ath5) && accumDiffSet.contains(ath6));
    }
    
    private UUID makeAuthorTimeHash(String authorUuidString, long time) throws
            NoSuchAlgorithmException, UnsupportedEncodingException{
        String str = authorUuidString + Long.toString(time);
        UUID type5Uuid = UuidT5Generator.get(UuidT5Generator.AUTHOR_TIME_ID, str);
        allConceptComputedAthAllMap.put(type5Uuid, str);
        return type5Uuid;
    }
    
    private class MockConcept{
        int id;
        HashMap<Integer, ArrayList<HashSet<UUID>>> athTimesCommit = new HashMap<Integer, ArrayList<HashSet<UUID>>>();
        HashMap<Integer, ArrayList<HashSet<UUID>>> athTimesAdj = new HashMap<Integer, ArrayList<HashSet<UUID>>>();
        HashSet<Integer> stampsNids;

        public MockConcept(int id, HashMap<Integer, ArrayList<HashSet<UUID>>> athTimesCommit, HashSet<Integer> stampsNids) {
            this.id = id;
            this.athTimesCommit = athTimesCommit;
            this.stampsNids = stampsNids;
        }

        public int getId() {
            return id;
        }

        public ArrayList<HashSet<UUID>> getAthTimesCommitByPath(int pathId) {
            return athTimesCommit.get(pathId);
        }
        
        public HashSet<Integer> getStampsNids() {
            return stampsNids;
        }

        public ArrayList<HashSet<UUID>> getAthTimesAdjByPath(int pathId) {
            return athTimesAdj.get(pathId);
        }
        
        
    }
}
