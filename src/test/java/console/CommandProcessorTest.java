package console;

import org.junit.Assert;
import org.junit.Test;

public class CommandProcessorTest {

	@Test
	public void testGivenCommands() {
		CommandProcessor cp = new CommandProcessor();
		testCommand(cp, "CREATE 0      100  1.5", CommandProcessor.OK_PREFIX, "1.5");
		testCommand(cp, "UPDATE 0      105  1.6", CommandProcessor.OK_PREFIX, "1.5");
		testCommand(cp, "GET           0      100", CommandProcessor.OK_PREFIX, "1.5");
		testCommand(cp, "GET           0      110", CommandProcessor.OK_PREFIX, "1.6");
		testCommand(cp, "LATEST 0      ", CommandProcessor.OK_PREFIX, "105", "1.6");
		testCommand(cp, "LATEST        1", CommandProcessor.ERR_PREFIX, "No history exists for identifier '1'");
		testCommand(cp, "CREATE 1      110           2.5", CommandProcessor.OK_PREFIX, "2.5");
		testCommand(cp, "CREATE        1      115           2.4", CommandProcessor.ERR_PREFIX, "A history already exists for identifier '1'");
		testCommand(cp, "UPDATE        1      115           2.4", CommandProcessor.OK_PREFIX, "2.5");
		testCommand(cp, "UPDATE        1      120           2.3", CommandProcessor.OK_PREFIX, "2.4");
		testCommand(cp, "UPDATE 1      125           2.2", CommandProcessor.OK_PREFIX, "2.3");
		testCommand(cp, "LATEST        1      ", CommandProcessor.OK_PREFIX, "125", "2.2");
		testCommand(cp, "GET    1      120", CommandProcessor.OK_PREFIX, "2.3");
		testCommand(cp, "UPDATE 1      120    2.35", CommandProcessor.OK_PREFIX, "2.3");
		testCommand(cp, "GET           1      122", CommandProcessor.OK_PREFIX, "2.35");
		testCommand(cp, "DELETE 1      122", CommandProcessor.OK_PREFIX, "2.35");
		testCommand(cp, "GET           1      125", CommandProcessor.OK_PREFIX, "2.35");
		testCommand(cp, "DELETE 1", CommandProcessor.OK_PREFIX, "2.35");	
	}
	
	@Test
	public void testEmptyStoreSequence() {
		CommandProcessor cp = new CommandProcessor();
		
		// get on an empty store, get error
		testCommand(cp, "GET  1 1", CommandProcessor.ERR_PREFIX, "1");
		
		// latest on an empty store, get error
		testCommand(cp, "LATEST 1", CommandProcessor.ERR_PREFIX, "1");
		
		// delete on an empty store, get error
		testCommand(cp, "DELETE  1 1", CommandProcessor.ERR_PREFIX, "1");
		testCommand(cp, "DELETE  1 1", CommandProcessor.ERR_PREFIX, "1");

		// update on empty store, get error
		testCommand(cp, "UPDATE  1 1 1", CommandProcessor.ERR_PREFIX, "1");
	}
	
	@Test
	public void testMultipleIds() {
		CommandProcessor cp = new CommandProcessor();

		//create 3 ids
		testCommand(cp, "CREATE  1 1 1A", CommandProcessor.OK_PREFIX, "1A");
		testCommand(cp, "CREATE  2 1 2A", CommandProcessor.OK_PREFIX, "2A");
		testCommand(cp, "CREATE  3 1 3A", CommandProcessor.OK_PREFIX, "3A");
		
		// delete all 3 by id
		testCommand(cp, "DELETE  1", CommandProcessor.OK_PREFIX);
		testCommand(cp, "DELETE  2", CommandProcessor.OK_PREFIX);
		testCommand(cp, "DELETE  3", CommandProcessor.OK_PREFIX);
		
		// try to delete them again
		testCommand(cp, "DELETE  1", CommandProcessor.ERR_PREFIX);
		testCommand(cp, "DELETE  2", CommandProcessor.ERR_PREFIX);
		testCommand(cp, "DELETE  3", CommandProcessor.ERR_PREFIX);
		
		// create them again
		testCommand(cp, "CREATE  1 1 1A", CommandProcessor.OK_PREFIX, "1A");
		testCommand(cp, "CREATE  2 1 2A", CommandProcessor.OK_PREFIX, "2A");
		testCommand(cp, "CREATE  3 1 3A", CommandProcessor.OK_PREFIX, "3A");	
		
		// get data
		testCommand(cp, "GET  1 1", CommandProcessor.OK_PREFIX, "1A");
		testCommand(cp, "GET  2 1", CommandProcessor.OK_PREFIX, "2A");
		testCommand(cp, "GET  3 1", CommandProcessor.OK_PREFIX, "3A");

		// update existing data
		testCommand(cp, "UPDATE  1 1 1B", CommandProcessor.OK_PREFIX, "1A");
		testCommand(cp, "UPDATE  2 1 2B", CommandProcessor.OK_PREFIX, "2A");
		testCommand(cp, "UPDATE  3 1 3B", CommandProcessor.OK_PREFIX, "3A");
		
		// get data
		testCommand(cp, "GET  1 1", CommandProcessor.OK_PREFIX, "1B");
		testCommand(cp, "GET  2 1", CommandProcessor.OK_PREFIX, "2B");
		testCommand(cp, "GET  3 1", CommandProcessor.OK_PREFIX, "3B");	
		
		// update with new timestamp and data
		testCommand(cp, "UPDATE  1 2 1C", CommandProcessor.OK_PREFIX);
		testCommand(cp, "UPDATE  2 2 2C", CommandProcessor.OK_PREFIX);
		testCommand(cp, "UPDATE  3 2 3C", CommandProcessor.OK_PREFIX);	
		
		// get new data
		testCommand(cp, "GET  1 2", CommandProcessor.OK_PREFIX, "1C");
		testCommand(cp, "GET  2 2", CommandProcessor.OK_PREFIX, "2C");
		testCommand(cp, "GET  3 2", CommandProcessor.OK_PREFIX, "3C");	
		
		// delete one timestamp
		testCommand(cp, "DELETE  1 2", CommandProcessor.OK_PREFIX, "1C");
		testCommand(cp, "DELETE  2 2", CommandProcessor.OK_PREFIX, "2C");
		testCommand(cp, "DELETE  3 2", CommandProcessor.OK_PREFIX, "3C");
		
		// check delete worked
		testCommand(cp, "GET  1 2", CommandProcessor.OK_PREFIX, "1B");
		testCommand(cp, "GET  2 2", CommandProcessor.OK_PREFIX, "2B");
		testCommand(cp, "GET  3 2", CommandProcessor.OK_PREFIX, "3B");	
		
		// delete id
		testCommand(cp, "DELETE  1", CommandProcessor.OK_PREFIX, "1B");
		testCommand(cp, "DELETE  2", CommandProcessor.OK_PREFIX, "2B");
		testCommand(cp, "DELETE  3", CommandProcessor.OK_PREFIX, "3B");		
		
		// check delete worked
		testCommand(cp, "GET  1 1", CommandProcessor.ERR_PREFIX, "1");
		testCommand(cp, "GET  2 1", CommandProcessor.ERR_PREFIX, "2");
		testCommand(cp, "GET  3 1", CommandProcessor.ERR_PREFIX, "3");		
	}
	
	@Test
	public void testBadCommands() {
		CommandProcessor cp = new CommandProcessor();

		// test null
		testCommand(cp, null, CommandProcessor.ERR_PREFIX);

		// test empty
		testCommand(cp, "", CommandProcessor.ERR_PREFIX);
		
		// test not enough create args
		testCommand(cp, "CREATE", CommandProcessor.ERR_PREFIX);
		
		// test too many create args
		testCommand(cp, "CREATE 1 1 1 1", CommandProcessor.ERR_PREFIX);
		
		// test malformed id
		testCommand(cp, "CREATE A 1 1", CommandProcessor.ERR_PREFIX);

		// test malformed timestamp
		testCommand(cp, "CREATE 1 A 1", CommandProcessor.ERR_PREFIX);	
		
		// test too long data
		testCommand(cp, "CREATE 1 1 12345678901234567", CommandProcessor.ERR_PREFIX);	
		
		// test malformed command
		testCommand(cp, "MALFORMED", CommandProcessor.ERR_PREFIX);
		
		// TODO add more here
	}
	
	
	private void testCommand(CommandProcessor cp, String command, String expectedPrefix, String... expectedContents) {
		String response = cp.process(command);
		Assert.assertTrue(response.startsWith(expectedPrefix));
		
		for (String expected : expectedContents) {
			Assert.assertTrue(response.contains(expected));
		}
		
	}
}
