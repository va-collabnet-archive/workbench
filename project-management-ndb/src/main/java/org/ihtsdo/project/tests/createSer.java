/**
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
package org.ihtsdo.project.tests;

import java.io.File;
import java.io.IOException;

import org.ihtsdo.project.tasks.LaunchIssuePanel;
import org.ihtsdo.project.tasks.LaunchIssueRepoPanel;

/**
 * The Class createSer.
 */
public class createSer {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
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
		//LaunchProjectManager lap=new LaunchProjectManager();
            // Display the original object graph
          //  System.out.println("Original data structure: " + lap );

            // Output it to a file
            File f = new File("c:\\org.ihtsdo.project.tasks.LaunchProjectManager.task");
            System.out.println("Storing to a file...");
          //  Serializer.store(lap, f);
//
//
            LaunchIssuePanel lap2=new LaunchIssuePanel();
                // Display the original object graph
                System.out.println("Original data structure: " + lap2 );

                // Output it to a file
                f = new File("c:\\org.ihtsdo.project.tasks.LaunchIssuePanel.task");
                System.out.println("Storing to a file...");
                Serializer.store(lap2, f);

                LaunchIssueRepoPanel lap3=new LaunchIssueRepoPanel();
                    // Display the original object graph
                    System.out.println("Original data structure: " + lap3 );

                    // Output it to a file
                    f = new File("c:\\org.ihtsdo.project.tasks.LaunchIssueRepoPanel.task");
                    System.out.println("Storing to a file...");
                    Serializer.store(lap3, f);

//
                    org.ihtsdo.project.tasks.LaunchIssueListPanel lap4=new org.ihtsdo.project.tasks.LaunchIssueListPanel();
                        // Display the original object graph
                        System.out.println("Original data structure: " + lap4 );

                        // Output it to a file
                        f = new File("c:\\org.ihtsdo.project.tasks.LaunchIssueListPanel.task");
                        System.out.println("Storing to a file...");
                        Serializer.store(lap4, f);
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
 

