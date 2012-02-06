/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;

/**
 * This class defines utility routines that use Java serialization.
 */
public class Serializer {
  
  /**
   * Serialize the object o (and any Serializable objects it refers to) and
   * store its serialized state in File f.
   *
   * @param o the o
   * @param f the f
   * @throws IOException Signals that an I/O exception has occurred.
   */
  static void store(Serializable o, File f) throws IOException {
    ObjectOutputStream out = // The class for serialization
    new ObjectOutputStream(new FileOutputStream(f));
    out.writeObject(o); // This method serializes an object graph
    out.close();
  }

  /**
   * Deserialize the contents of File f and return the resulting object.
   *
   * @param f the f
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  static Object load(File f) throws IOException, ClassNotFoundException {
    ObjectInputStream in = // The class for de-serialization
    new ObjectInputStream(new FileInputStream(f));
    return in.readObject(); // This method deserializes an object graph
  }

  /**
   * Use object serialization to make a "deep clone" of the object o. This
   * method serializes o and all objects it refers to, and then deserializes
   * that graph of objects, which means that everything is copied. This
   * differs from the clone() method of an object which is usually implemented
   * to produce a "shallow" clone that copies references to other objects,
   * instead of copying all referenced objects.
   *
   * @param o the o
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ClassNotFoundException the class not found exception
   */
  static Object deepclone(final Serializable o) throws IOException,
      ClassNotFoundException {
    // Create a connected pair of "piped" streams.
    // We'll write bytes to one, and them from the other one.
    final PipedOutputStream pipeout = new PipedOutputStream();
    PipedInputStream pipein = new PipedInputStream(pipeout);

    // Now define an independent thread to serialize the object and write
    // its bytes to the PipedOutputStream
    Thread writer = new Thread() {
      public void run() {
        ObjectOutputStream out = null;
        try {
          out = new ObjectOutputStream(pipeout);
          out.writeObject(o);
        } catch (IOException e) {
        } finally {
          try {
            out.close();
          } catch (Exception e) {
          }
        }
      }
    };
    writer.start(); // Make the thread start serializing and writing

    // Meanwhile, in this thread, read and deserialize from the piped
    // input stream. The resulting object is a deep clone of the original.
    ObjectInputStream in = new ObjectInputStream(pipein);
    return in.readObject();
  }
}
