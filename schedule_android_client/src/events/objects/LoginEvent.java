package events.objects;

import java.util.EventObject;
import io.packet.server.LoginPacket;

public class LoginEvent extends EventObject {

	public LoginPacket.Status status;
	
	public LoginEvent(Object source, LoginPacket.Status status) {
		super(source);
		this.status = status;
	}
}
