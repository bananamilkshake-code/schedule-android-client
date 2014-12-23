package com.open.schedule.events.listeners;

import com.open.schedule.events.objects.Event;

public interface EventListener {
	public void handle(Event event);
}
