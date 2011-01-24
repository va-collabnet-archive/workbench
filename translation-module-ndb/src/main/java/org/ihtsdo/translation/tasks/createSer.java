package org.ihtsdo.translation.tasks;

import java.io.File;
import java.io.IOException;

public class createSer {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException,
    ClassNotFoundException {
		// 
            // Create a simple object graph
            /*DataStructure ds = new DataStructure();
            ds.message = "hello world";
            ds.data = new int[] { 1, 2, 3, 4 };
            ds.other = new DataStructure();
            ds.other.message = "nested structure";
            ds.other.data = new int[] { 9, 8, 7 };
*/
		OpenTranslationForSelectedConceptToMP lap=new OpenTranslationForSelectedConceptToMP();
            // Display the original object graph
            System.out.println("Original data structure: " + lap );

            // Output it to a file
            File f = new File("/Users/ar/documents/org.ihtsdo.translation.tasks.OpenTranslationForSelectedConceptToMP.task");
            System.out.println("Storing to a file...");
            Serializer.store(lap, f);
//
//
//            LaunchIssuePanel lap2=new LaunchIssuePanel();
//                // Display the original object graph
//                System.out.println("Original data structure: " + lap2 );
//
//                // Output it to a file
//                f = new File("c:\\org.dwfa.termmed.projectmanager.tasks.LaunchIssuePanel.task");
//                System.out.println("Storing to a file...");
//                Serializer.store(lap2, f);
//
//                LaunchIssueRepoPanel lap3=new LaunchIssueRepoPanel();
//                    // Display the original object graph
//                    System.out.println("Original data structure: " + lap3 );
//
//                    // Output it to a file
//                    f = new File("c:\\org.dwfa.termmed.projectmanager.tasks.LaunchIssueRepoPanel.task");
//                    System.out.println("Storing to a file...");
//                    Serializer.store(lap3, f);
//
////
//                    org.dwfa.termmed.projectmanager.tasks.LaunchIssueListPanel lap4=new org.dwfa.termmed.projectmanager.tasks.LaunchIssueListPanel();
//                        // Display the original object graph
//                        System.out.println("Original data structure: " + lap4 );
//
//                        // Output it to a file
//                        f = new File("c:\\org.dwfa.termmed.projectmanager.tasks.LaunchIssueListPanel.task");
//                        System.out.println("Storing to a file...");
//                        Serializer.store(lap4, f);
//                        
//
//                        SMExpertAndWaitAction lap5=new SMExpertAndWaitAction();
//                            // Display the original object graph
//                            System.out.println("Original data structure: " + lap5 );
//
//                            // Output it to a file
//                            f = new File("c:\\org.dwfa.ace.task.SMExpertAndWaitAction.task");
//                            System.out.println("Storing to a file...");
//                            Serializer.store(lap5, f);
//                            
//                            IniProcess lap6=new IniProcess();
//                            // Display the original object graph6
//                            System.out.println("Original data structure: " + lap6 );
//
//                            // Output it to a file
//                            f = new File("c:\\org.dwfa.ace.task.IniProcess.task");
//                            System.out.println("Storing to a file...");
//                            Serializer.store(lap6, f);
//                                                        
//                            TranslationViewAndWaitAction lap7=new TranslationViewAndWaitAction();
//                            // Display the original object graph
//                            System.out.println("Original data structure: " + lap7 );
//
//                            // Output it to a file
//                            f = new File("c:\\org.dwfa.ace.task.TranslationViewAndWaitAction.task");
//                            System.out.println("Storing to a file...");
//                            Serializer.store(lap7, f);
//                            
//                            ValidateRoleFromAttachment lap8=new ValidateRoleFromAttachment();
//                            // Display the original object graph
//                            System.out.println("Original data structure: " + lap8 );
//
//                            // Output it to a file
//                            f = new File("c:\\org.dwfa.ace.task.ValidateRoleFromAttachment.task");
//                            System.out.println("Storing to a file...");
//                            Serializer.store(lap8, f);
//                            
//                            MoveToQueue lap9=new MoveToQueue();
//                            // Display the original object graph
//                            System.out.println("Original data structure: " + lap9 );
//
//                            // Output it to a file
//                            f = new File("c:\\org.dwfa.ace.task.MoveToQueue.task");
//                            System.out.println("Storing to a file...");
//                            Serializer.store(lap9, f);
            // Read it back from the file, and display it again
          /*  ds = (DataStructure) Serializer.load(f);
            System.out.println("Read from the file: " + ds);
*/
            // Create a deep clone and display that. After making the copy
            // modify the original to prove that the clone is "deep".
           /* DataStructure ds2 = (DataStructure) Serializer.deepclone(ds);
            ds.other.message = null;
            ds.other.data = null; // Change original
            System.out.println("Deep clone: " + ds2);*/
          }
        }
 

