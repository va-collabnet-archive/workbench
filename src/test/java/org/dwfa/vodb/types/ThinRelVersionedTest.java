package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;

public class ThinRelVersionedTest extends TestCase {

   
   @Override
   protected void setUp() throws Exception {
       super.setUp();
   }
   
   public void testAddTuples() {
      int relId = 0;
      int componentOneId= 1;
      int componentTwoId= 2;
      
      int path0 = 3;
      int path1 = 4;
      int path2 = 5;
      
      int type0 = 6;
      int type1 = 7;
      int type2 = 8;
      
      int time0 = 9;
      int time1 = 10;
      int time2 = 11;
      
      int stat0 = 12;
      int stat1 = 13;
      int stat2 = 14;
      
      int refinability = 15;
      int characteristic = 16;
      
      ThinRelVersioned rel = new ThinRelVersioned(relId, componentOneId, componentTwoId,
            2);
      ThinRelPart part0 = new ThinRelPart();
      part0.setCharacteristicId(characteristic);
      part0.setGroup(0);
      part0.setPathId(path0);
      part0.setRefinabilityId(refinability);
      part0.setRelTypeId(type0);
      part0.setStatusId(stat0);
      part0.setVersion(time0);
      rel.addVersion(part0);
      
      ThinRelPart part1 = new ThinRelPart();
      part1.setCharacteristicId(characteristic);
      part1.setGroup(0);
      part1.setPathId(path1);
      part1.setRefinabilityId(refinability);
      part1.setRelTypeId(type1);
      part1.setStatusId(stat1);
      part1.setVersion(time1);
      rel.addVersion(part1);
      
      ThinRelPart part2 = new ThinRelPart();
      part2.setCharacteristicId(characteristic);
      part2.setGroup(0);
      part2.setPathId(path2);
      part2.setRefinabilityId(refinability);
      part2.setRelTypeId(type2);
      part2.setStatusId(stat2);
      part2.setVersion(time2);
      rel.addVersion(part2);
      
      assertEquals(3, rel.getVersions().size());
      
      I_IntSet allowedStatus = null;
      I_IntSet allowedTypes = null;
      Set<I_Position> positions = null;
      List<I_RelTuple> returnRels = null;
       
      boolean addUncommitted = false;
      
      returnRels = new ArrayList<I_RelTuple>();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(3, returnRels.size());
      addUncommitted = true;
      
      returnRels = new ArrayList<I_RelTuple>();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(3, returnRels.size());
      
      allowedStatus = new IntSet();
      allowedStatus.add(stat0);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());

      allowedStatus.add(stat1);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());
      
      allowedStatus.add(stat2);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(3, returnRels.size());
      
      Path origin = new Path(path0, null);
      Position latestOnOrigin = new Position(Integer.MAX_VALUE, origin);
      Set<I_Position> latestOnOriginSet = new HashSet<I_Position>();
      latestOnOriginSet.add(latestOnOrigin);
      
      returnRels.clear();
      positions = latestOnOriginSet;
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());

      Path p1 = new Path(path1, new ArrayList<I_Position>(latestOnOriginSet));
      Position latestOnP1 = new Position(Integer.MAX_VALUE, p1);
      Set<I_Position> latestOnP1Set = new HashSet<I_Position>();
      latestOnP1Set.add(latestOnP1);
      positions = latestOnP1Set;
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());
      
      Path p2 = new Path(path2, new ArrayList<I_Position>(latestOnOriginSet));
      Position latestOnP2 = new Position(Integer.MAX_VALUE, p2);
      Set<I_Position> latestOnP2Set = new HashSet<I_Position>();
      latestOnP2Set.add(latestOnP2);
      positions = latestOnP2Set;
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());
      
      Set<I_Position> latestOnP1andP2Set = new HashSet<I_Position>();
      latestOnP1andP2Set.add(latestOnP1);
      latestOnP1andP2Set.add(latestOnP2);
      positions = latestOnP1andP2Set;
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());

      returnRels.clear();
      allowedStatus = null;
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());

      returnRels.clear();
      allowedStatus = null;
      allowedTypes = new IntSet();
      allowedTypes.add(type0);
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(0, returnRels.size());

      allowedTypes.add(type1);
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());

      allowedTypes.add(type2);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());

   }
   public void testAddTuples2() {
      int relId = 0;
      int componentOneId= 1;
      int componentTwoId= 2;
      
      int path0 = 3;
      int path1 = 4;
      int path2 = 5;
      
      int type0 = 6;
      int type1 = 7;
      int type2 = 8;
      
      int time0 = 9;
      int time1 = 10;
      int time2 = 11;
      
      int stat0 = 12;
      int stat1 = 13;
      int stat2 = 14;
      
      int refinability = 15;
      int characteristic = 16;
      
      ThinRelVersioned rel = new ThinRelVersioned(relId, componentOneId, componentTwoId,
            2);
      ThinRelPart part0 = new ThinRelPart();
      part0.setCharacteristicId(characteristic);
      part0.setGroup(0);
      part0.setPathId(path0);
      part0.setRefinabilityId(refinability);
      part0.setRelTypeId(type0);
      part0.setStatusId(stat0);
      part0.setVersion(time0);
      rel.addVersion(part0);
      
      ThinRelPart part1 = new ThinRelPart();
      part1.setCharacteristicId(characteristic);
      part1.setGroup(0);
      part1.setPathId(path0);
      part1.setRefinabilityId(refinability);
      part1.setRelTypeId(type0);
      part1.setStatusId(stat1);
      part1.setVersion(time1);
      rel.addVersion(part1);
      
      
      assertEquals(2, rel.getVersions().size());
      
      I_IntSet allowedStatus = null;
      I_IntSet allowedTypes = null;
      Set<I_Position> positions = null;
      List<I_RelTuple> returnRels = null;
       
      boolean addUncommitted = false;
      
      returnRels = new ArrayList<I_RelTuple>();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());
      addUncommitted = true;
      
      returnRels = new ArrayList<I_RelTuple>();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());
      
      allowedStatus = new IntSet();
      allowedStatus.add(stat0);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(0, returnRels.size());

      allowedStatus.add(stat1);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());
      
      allowedStatus.add(stat2);
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(2, returnRels.size());
      
      Path origin = new Path(path0, null);
      Position latestOnOrigin = new Position(Integer.MAX_VALUE, origin);
      Set<I_Position> latestOnOriginSet = new HashSet<I_Position>();
      latestOnOriginSet.add(latestOnOrigin);
      
      returnRels.clear();
      positions = latestOnOriginSet;
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());

      Path p1 = new Path(path1, new ArrayList<I_Position>(latestOnOriginSet));
      Position latestOnP1 = new Position(Integer.MAX_VALUE, p1);
      Set<I_Position> latestOnP1Set = new HashSet<I_Position>();
      latestOnP1Set.add(latestOnP1);
      positions = latestOnP1Set;
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());
      
      Path p2 = new Path(path2, new ArrayList<I_Position>(latestOnOriginSet));
      Position latestOnP2 = new Position(Integer.MAX_VALUE, p2);
      Set<I_Position> latestOnP2Set = new HashSet<I_Position>();
      latestOnP2Set.add(latestOnP2);
      positions = latestOnP2Set;
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());
      
      Set<I_Position> latestOnP1andP2Set = new HashSet<I_Position>();
      latestOnP1andP2Set.add(latestOnP1);
      latestOnP1andP2Set.add(latestOnP2);
      positions = latestOnP1andP2Set;
      returnRels.clear();
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());

      returnRels.clear();
      allowedStatus = null;
      rel.addTuples(allowedStatus, allowedTypes, positions, returnRels, addUncommitted);
      assertEquals(1, returnRels.size());

   }

   @Override
   protected void tearDown() throws Exception {
      super.tearDown();
   }

}
