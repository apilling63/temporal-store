package console;

import store.TemporalStore;

import common.CommandProcessorException;

/**
 * This class interprets the commands as passes them to the TemporalStore for actioning
 *
 */
public class CommandProcessor {

	private final TemporalStore store = new TemporalStore();
	public static final String OK_PREFIX = "OK ";
	public static final String ERR_PREFIX = "ERR ";

	String process(String command) {
		try {
			nullEmptyCheck(command);
			String[] args = command.split("\\s+");
			
			// simply switch on the command type then extract the values
			switch(args[0]) {
				case "CREATE": {
					checkNumArgs(args, 4);
					Integer id = getIdFromString(args[1]);			
					Long timestamp = getTimestampFromString(args[2]);			
					String data = getDataFromString(args[3]);
					return createOKMessage(store.create(id, timestamp, data));				
				}
				case "GET": {
					checkNumArgs(args, 3);
					Integer id = getIdFromString(args[1]);			
					Long timestamp = getTimestampFromString(args[2]);			
					return createOKMessage(store.get(id, timestamp));	
				}
				case "UPDATE": {
					checkNumArgs(args, 4);
					Integer id = getIdFromString(args[1]);			
					Long timestamp = getTimestampFromString(args[2]);			
					String data = getDataFromString(args[3]);
					return createOKMessage(store.update(id, timestamp, data));	
				}		
				case "LATEST": {
					checkNumArgs(args, 2);
					Integer id = getIdFromString(args[1]);			
					return createOKMessage(store.latest(id));	
				}					
				case "DELETE": {
					if (args.length < 2 || args.length > 3) {
						throw new CommandProcessorException("Wrong number of arguments");
					}
	
					Integer id = getIdFromString(args[1]);			
					
					if (args.length == 3) {
						Long timestamp = getTimestampFromString(args[2]);			
						return createOKMessage(store.delete(id, timestamp));	
					} else {
						return createOKMessage(store.delete(id));						
					}	
				}				
				default :
					throw new CommandProcessorException(args[0] + " is an invalid command");
			}
		
		} catch(CommandProcessorException e) {
			return ERR_PREFIX + e.getMessage();
		}
	}
	
	
	
	//    STATIC HELPER METHODS     //
	
	private static void checkNumArgs(String[] args, int numExpected) throws CommandProcessorException {
		if (args.length != numExpected) {
			throw new CommandProcessorException("Wrong number of arguments");
		}
	}
	
	private static Integer getIdFromString(String input) throws CommandProcessorException {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new CommandProcessorException("Invalid id value, must be an Integer");
		}
	}
	
	private static Long getTimestampFromString(String input) throws CommandProcessorException {
		try {
			return Long.parseLong(input);
		} catch (NumberFormatException e) {
			throw new CommandProcessorException("Invalid timestamp value, must be a Long");
		}
	}	
	
	private static String getDataFromString(String input) throws CommandProcessorException {
		if (input.length() > 16) {
			throw new CommandProcessorException("Data must be no more than 16 characters long");	
		}
		
		return input;
	}	
	
	private static void nullEmptyCheck(String input) throws CommandProcessorException {
		if (input == null || "".equals(input)) {
			throw new CommandProcessorException("No command details detected");	
		}		
	}
	
	private static String createOKMessage(String suffix) {
		return OK_PREFIX + suffix;
	}
}
