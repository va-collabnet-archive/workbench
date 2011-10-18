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
   */
  static void store(Serializable o, File f) throws IOException {
    ObjectOutputStream out = // The class for serialization
    new ObjectOutputStream(new FileOutputStream(f));
    out.writeObject(o); // This method serializes an object graph
    out.close();
  }

  /**
   * Deserialize the contents of File f and return the resulting object
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
