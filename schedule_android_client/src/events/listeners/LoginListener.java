package events.listeners;

import java.util.EventListener;

import events.objects.LoginEvent;

public interface LoginListener extends EventListener {
	public void loginEvent(LoginEvent event);
}
