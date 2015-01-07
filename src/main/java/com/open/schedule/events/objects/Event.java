package com.open.schedule.events.objects;

public class Event {
	protected final Object owner;
	protected final Type type;
	protected final Object data;
	public Event(Object owner, Type type, Object data) {
		this.owner = owner;
		this.type = type;
		this.data = data;

		EventWarehouse.getInstance().addEvent(this);
	}

	public Type getType() {
		return type;
	}

	public Object getOwner() {
		return owner;
	}

	public Object getData() {
		return data;
	}

	public enum Type {
		LOGIN,
		REGISTER
	}
}
