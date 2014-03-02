package bomberman;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	private static BufferedWriter log;
	
	public Logger(String fileName) {
		try {
			log = new BufferedWriter(new FileWriter(new File(fileName)));
		} catch (IOException e) {
			System.out.println("ERROR: Could not create log file: " + fileName);
			e.printStackTrace();
		}
	}
	
	public Logger() {
		this("logs/bomberman-" + getDate() + ".log");
	}
	
	public void logCommand(Command command) throws IOException {
		synchronized (log) {
			log.write("COMMAND: Player=" + command.getPlayer() + ", Operation=" + command.getOperation());
			log.newLine();
			log.flush();
		}
	}
	
	public void logRefresh() throws IOException {
		synchronized (log) {
			log.write("REFRESH: Server sent refreshed grid to client(s)");
			log.newLine();
			log.flush();
		}
	}
	
	public void close() throws IOException {
		log.close();
	}
	
	private static String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat();
		formatter.applyPattern("yyyy-MM-dd-HH-mm");
		return formatter.format(new Date());
	}
	
}
