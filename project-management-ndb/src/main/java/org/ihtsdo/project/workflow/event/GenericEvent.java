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
package org.ihtsdo.project.workflow.event;

/**
 * The Class GenericEvent.
 */
public abstract class GenericEvent {
	
	/**
	 * The Enum EventType.
	 */
	public enum EventType {
		
		/** The NE w_ ta g_ added. */
		NEW_TAG_ADDED,
		
		/** The ITE m_ tagged. */
		ITEM_TAGGED, 
		
		/** The TA g_ removed. */
		TAG_REMOVED, 
		
		/** The ITE m_ stat e_ changed. */
		ITEM_STATE_CHANGED,
		
		/** The OUTBO x_ conten t_ changed. */
		OUTBOX_CONTENT_CHANGED,
		
		/** The OPE n_ inbo x_ item. */
		OPEN_INBOX_ITEM, 
		
		/** The TRANSLATIO n_ stat e_ changed. */
		TRANSLATION_STATE_CHANGED, 
		
		/** The TOD o_ content s_ changed. */
		TODO_CONTENTS_CHANGED, 
		
		/** The ITE m_ destinatio n_ changed. */
		ITEM_DESTINATION_CHANGED, 
		
		/** The ITE m_ sen t_ t o_ specia l_ folder. */
		ITEM_SENT_TO_SPECIAL_FOLDER, 
		
		/** The INBO x_ ite m_ selected. */
		INBOX_ITEM_SELECTED, 
		
		/** The INBO x_ column s_ changed. */
		INBOX_COLUMNS_CHANGED, 
		
		/** The SEN d_ bac k_ t o_ inbox. */
		SEND_BACK_TO_INBOX, 
		
		/** The EMPT y_ inbo x_ ite m_ selected. */
		EMPTY_INBOX_ITEM_SELECTED, 
 /** The ITE m_ remove d_ fro m_ todo. */
 ITEM_REMOVED_FROM_TODO;
	}
	
	/**
	 * Gets the associated type.
	 *
	 * @return the associated type
	 */
	public abstract EventType getAssociatedType();
	
}
