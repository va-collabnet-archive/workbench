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
package org.ihtsdo.project.view.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.ihtsdo.project.view.event.GenericEvent.EventType;


/**
 * The Class EventMediator.
 */
public class EventMediator{

	/** The event observers. */
	private HashMap<EventType, ArrayList<EventHandler<GenericEvent>>> eventObservers = new HashMap<EventType, ArrayList<EventHandler<GenericEvent>>>();
	
	/** The instance. */
	private static EventMediator instance;

	/**
	 * Instantiates a new event mediator.
	 */
	private EventMediator() {
	}

	/**
	 * Suscribe.
	 *
	 * @param event the event
	 * @param handler the handler
	 */
	public void suscribe(EventType event, EventHandler handler){
		ArrayList<EventHandler<GenericEvent>> handlerList = null;
		if(eventObservers.containsKey(event)){
			handlerList = eventObservers.get(event);
		}else{
			handlerList = new ArrayList<EventHandler<GenericEvent>>();
		}
		if(!handlerList.contains(handler)){
			handlerList.add(handler);
		}else{
			handlerList.remove(handler);
			handlerList.add(handler);
		}
		for (EventHandler<GenericEvent> eventHandler : handlerList) {
			System.out.println(eventHandler.getParent());
		}
		eventObservers.put(event, handlerList);
	}
	
	/**
	 * Fire event.
	 *
	 * @param genericEvent the generic event
	 */
	public void fireEvent(GenericEvent genericEvent){
		Set<EventType> keys = eventObservers.keySet();
		for (EventType eventType : keys) {
			if(eventType.equals(genericEvent.getAssociatedType())){
				ArrayList<EventHandler<GenericEvent>> suscribers = eventObservers.get(eventType);
				for (EventHandler<GenericEvent> handlers : suscribers) {
					handlers.handleEvent(genericEvent);
				}
				break;
			}
		}
	}
	
	/**
	 * Gets the single instance of EventMediator.
	 *
	 * @return single instance of EventMediator
	 */
	public static EventMediator getInstance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new EventMediator();
			return instance;
		}
	}

}
