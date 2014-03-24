package bomberman.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import bomberman.common.Command;

public class ClientLogger extends Logger {

	public ClientLogger(String file) {
		super(file);
	}
	
	public void logCommandLatency(Command command) {
		writeStringToLog(COMMAND + "," + OPERATION + "=" + command.getOperation() + "," + TIME + "=" + (System.currentTimeMillis() - command.getSentTime()));
	}

}
