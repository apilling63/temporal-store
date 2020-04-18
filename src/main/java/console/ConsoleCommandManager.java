package console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
  
/**
 * Entry point to interact with standard input and output
 *
 */
public class ConsoleCommandManager {
    
	public static void main(String[] args) throws IOException {         
        boolean keepRunning = true;
        CommandProcessor cp = new CommandProcessor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
       
        String command;
        
        while (keepRunning && (command = reader.readLine()) != null) {                               
            if ("QUIT".equals(command)) {
                keepRunning = false;
            } else {
            	System.out.println(cp.process(command));
            }
        }
    }     
}