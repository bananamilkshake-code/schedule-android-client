package events.listeners;

import events.objects.Event;

public interface EventListener {
	public void handle(Event event);
}
