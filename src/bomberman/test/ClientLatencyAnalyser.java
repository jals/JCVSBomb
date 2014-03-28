package bomberman.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class ClientLatencyAnalyser {
	
	private static ArrayList<Long> moveLatencies = new ArrayList<Long>();
	private static ArrayList<Long> bombLatencies = new ArrayList<Long>();
	
	public static void main(String args[]) {
		File directory = new File("logs");
		
		if (!directory.exists() || !directory.isDirectory()) {
			System.out.println("Error: Logs directory not found");
			return;
		}
		
		File[] files = directory.listFiles();
		
		for (int i=0; i<files.length; i++) {
			if (files[i].getName().contains("client")) {
				System.out.println(files[i].getName());
				computeLatencies(files[i]);
				System.out.println("\n");
			}
		}
	}

	public static void computeLatencies(File file) {
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("Error: File not found");
			return;
		}
		
		try {
			String line = reader.readLine();

			while(line != null) {
				try {
					String[] split = line.split(",");
					String operation = split[1].split("=")[1];
					String latency = split[2].split("=")[1];
					Long l = new Long(latency);
					
					if (operation.contains("MOVE")) {
						moveLatencies.add(l);
					} else if (operation.equals("DROP_BOMB")) {
						bombLatencies.add(l);
					}
				} catch (Exception e) {
					// Ignore
				}
				
				line = reader.readLine();
			}
			
			reader.close();
		
			long total = 0;
			long grandTotal = 0;
			
			// Add up the move latencies
			for(Long l : moveLatencies) {
				total += l;
			}
			
			if (moveLatencies.size() > 0) {
				System.out.println("Average latency for a move operation: \t\t" + (total/moveLatencies.size()) + "ms");
			}
			
			grandTotal = total;
			total = 0;
			
			// Add up the bomb latencies
			for (Long l : bombLatencies) {
				total += l;
			}
			
			if (bombLatencies.size() > 0) {
				System.out.println("Average latency for a drop bomb operation: \t" + (total/bombLatencies.size()) + "ms");
			}
			
			
			// Compute the total latency
			grandTotal += total;
			int totalLatencies = moveLatencies.size() + bombLatencies.size();
			
			if (totalLatencies > 0 && bombLatencies.size() > 0) {
				System.out.println("Average total latency: \t\t\t\t" + (grandTotal/totalLatencies) + "ms");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
