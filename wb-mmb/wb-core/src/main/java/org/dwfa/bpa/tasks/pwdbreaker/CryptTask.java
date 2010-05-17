/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Apr 19, 2005
 */
package org.dwfa.bpa.tasks.pwdbreaker;

import java.util.UUID;

import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;

import org.dwfa.bpa.process.GenericTaskEntry;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;

public class CryptTask extends GenericTaskEntry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Integer tries;

    public byte[] word;

    public String encrypted;

    public UUID masterId;

    static String[] charMap = { "^@", "^A", "^B", "^C", "^D", "^E", "^F", "^G", "^H", "^I", "^J", "^K", "^L", "^M",
                               "^N", "^O", "^P", "^Q", "^R", "^S", "^T", "^U", "^V", "^W", "^X", "^Y", "^Z", "^[",
                               "^\\", "^]", "^^", "^_", " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+",
                               ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<",
                               "=", ">", "?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                               "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "[", "\\", "]", "^",
                               "_", "`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
                               "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "{", "|", "}", "~", "^?", };

    public CryptTask() {

    }

    /**
     * @param tries
     * @param word
     * @param encrypted
     */
    public CryptTask(Integer tries, byte[] word, String encrypted, UUID masterId) {
        super();
        this.tries = tries;
        this.word = word;
        this.encrypted = encrypted;
        this.masterId = masterId;
        this.name = "CryptTask";
    }

    public Entry execute(I_Work worker, JavaSpace05 space) throws TaskFailedException {
        PoisonPill template = new PoisonPill(masterId);
        try {
            if (space.readIfExists(template, null, JavaSpace.NO_WAIT) != null) {
                return null;
            }
        } catch (Exception ex) {
            // continue on
        }
        // System.out.println("Word: '" + getPrintableWord(word) +
        // "' encrypted: " + JCrypt.crypt(word));
        // System.out.println("Word: '" + new String(word) + "' crypt: " +
        // JCrypt.crypt(word));
        int num = tries.intValue();
        for (int i = 0; i < num; i++) {
            if (encrypted.equals(JCrypt.crypt(word))) {
                CryptResult result = new CryptResult(word, this.masterId);
                return result;
            }
            nextWord(word);
        }
        CryptResult result = new CryptResult(null, this.masterId);
        return result;
    }

    static void nextWord(byte[] word) {
        int pos = 5;
        for (;;) {
            word[pos]++;
            if ((word[pos] & 0x80) != 0) {
                word[pos--] = (byte) '!';
            } else {
                break;
            }
        }
    }

    public static String getPrintableWord(byte[] word) {
        String string = "";
        for (int i = 0; i < word.length; i++) {
            string = string + charMap[word[i]];
        }
        return string;
    }
}
