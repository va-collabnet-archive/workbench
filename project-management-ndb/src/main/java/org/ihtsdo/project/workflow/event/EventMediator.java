package org.ihtsdo.project.workflow.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.ihtsdo.project.workflow.event.GenericEvent.EventType;


public class EventMediator{

	private HashMap<EventType, ArrayList<EventHandler<GenericEvent>>> eventObservers = new HashMap<EventType, ArrayList<EventHandler<GenericEvent>>>();
	private static EventMediator instance;

	private EventMediator() {
	}

	public void suscribe(EventType event, EventHandler handler){
		ArrayList<EventHandler<GenericEvent>> handlerList = null;
		if(eventObservers.containsKey(event)){
			handlerList = eventObservers.get(event);
		}else{
			handlerList = new ArrayList<EventHandler<GenericEvent>>();
		}
		handlerList.add(handler);
		eventObservers.put(event, handlerList);
	}
	
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
	
	public static EventMediator getInstance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new EventMediator();
			return instance;
		}
	}

}
