package com.open.schedule.events.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.open.schedule.events.objects.Event.Type;
import com.open.schedule.events.listeners.EventListener;

public class EventWarehouse {
	
	private static EventWarehouse INSTANCE = null;
	
	private Queue<Event> events = new ConcurrentLinkedQueue<Event>();
	private Map<Type, ArrayList<EventListener>> listeners = new HashMap<Event.Type, ArrayList<EventListener>>();
	
	private EventWarehouse() {

		for (Event.Type type : Event.Type.values())
			listeners.put(type, new ArrayList<EventListener>());
		
		new Thread() {
			@Override
			public void run() {
				while(true) {
					Event event = (Event) events.poll();
					
					if (event == null)
						continue;
					
					ArrayList<EventListener> listeners = getListeners(event.type);
					for (EventListener listener : listeners) {
						listener.handle(event);
					}
				}
			}
		}.start();
	}
	
	public static EventWarehouse getInstance() {
		if (INSTANCE == null)
			INSTANCE = new EventWarehouse();
		
		return INSTANCE;
	}
	
	public void addEvent(Event event) {
		events.add(event);
	}
	
	public void addListener(EventListener listener, Event.Type type) {
		getListeners(type).add(listener);
	}
	
	public void removeListener(EventListener listener, Event.Type type) {
		getListeners(type).remove(listener);
	}
	
	private ArrayList<EventListener> getListeners(Event.Type type) {
		return (ArrayList<EventListener>)listeners.get(type);
	}
}