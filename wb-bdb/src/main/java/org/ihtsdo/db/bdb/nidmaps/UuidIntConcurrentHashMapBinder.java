/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.db.bdb.nidmaps;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author aimeefurber
 */
public class UuidIntConcurrentHashMapBinder extends TupleBinding<ConcurrentHashMap<UUID,Integer>>{
    public enum DB_TYPE{
        READ_ONLY(0), MUTABLE(1);
        private int type;
        
        DB_TYPE(int type){
            this.type = type;
        }
        
        public int getInt(){
            return type;
        }
    }
    
    public UuidIntConcurrentHashMapBinder() {
    }
    
     @Override
        public ConcurrentHashMap<UUID,Integer> entryToObject(TupleInput input) {
            int type = input.readInt();
            int length = input.readInt();
            
            ConcurrentHashMap<UUID,Integer> map = new ConcurrentHashMap<>(length);
            for (int i = 0; i < length; i++) {
                long lsb = input.readLong();
                long msb = input.readLong();
                int nid = input.readInt();
                UUID uuid = new UUID(msb, lsb);
                map.put(uuid, nid);
            }
            return map;
        }

        @Override
        public void objectToEntry(ConcurrentHashMap<UUID,Integer> map, TupleOutput output) {
            output.writeInt(DB_TYPE.MUTABLE.getInt()); 
            output.writeInt(map.size());
            
            Set<Map.Entry<UUID, Integer>> entrySet = map.entrySet();
            for(Map.Entry<UUID, Integer> entry : entrySet){
                output.writeLong(entry.getKey().getLeastSignificantBits());
                output.writeLong(entry.getKey().getMostSignificantBits());
                output.writeInt(entry.getValue());
            }
        }
    
}
