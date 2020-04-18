package store;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import common.CommandProcessorException;

public class TemporalStore {

	// use concurrent hash map for thread safety
	// TODO should really add the 1000 to config file
	private final ConcurrentHashMap<Integer, ConcurrentNavigableMap<Long, String>> internalStore = new ConcurrentHashMap<>(10000);
	
	/**
       - Creates a new history for the given identifier, if there is no existing history. Adds an 
       observation to the newly created history for the given timestamp and data. CREATE should
       not be executed if the provided identifier already has a history. Returns the data which 
       was inserted, to confirm insertion.
	 * 
	 * @param id
	 * @param timestamp
	 * @param data
	 * @return
	 * @throws CommandProcessorException
	 */
	public String create(Integer id, Long timestamp, String data) throws CommandProcessorException {
		// Create a new thread safe sorted map.  Use a tree map for fast, ordered lookups
		ConcurrentNavigableMap<Long, String> newMap = new ConcurrentSkipListMap<Long, String>();
		newMap.put(timestamp, data);
		
		// use thread safe, atomic putIfAbsent
		if (internalStore.putIfAbsent(id, newMap) != null) {
			// there is an existing history, so throw exception
			throw new CommandProcessorException("A history already exists for identifier '" + id + "'");
		} else {
			return data;
		}
	}
	
	/**
       - Inserts an observation for the given identifier, timestamp and data. Returns the data from 
       the prior observation for that timestamp. 
	 * @param id
	 * @param timestamp
	 * @param data
	 * @return
	 * @throws CommandProcessorException
	 */
	public String update(Integer id, Long timestamp, String data) throws CommandProcessorException {
		ConcurrentNavigableMap<Long, String> existingMap = getMapOrThrow(id);
		
		Entry<Long, String> currentEntry = existingMap.floorEntry(timestamp);
		String toReturn = currentEntry == null ? "No existing data for timestamp: " + timestamp : currentEntry.getValue();
		existingMap.put(timestamp, data);

		return toReturn;				
	}	
	
	/**
       - deletes the history for the given identifier, and returns
       the observation with the greatest timestamp from the history which has been deleted. 
	 * @param id
	 * @return
	 * @throws CommandProcessorException
	 */
	public String delete(Integer id) throws CommandProcessorException {
		ConcurrentNavigableMap<Long, String> existingMap = internalStore.remove(id);
		
		if (existingMap == null) {
			throw new CommandProcessorException("No history exists for identifier '" + id + "'");		
		} else {
			return existingMap.size() == 0 ? "No existing data for id: " + id : existingMap.lastEntry().getValue();	
		}	
	}	

	/**
       - deletes all observations for the given identifier from that 
       timestamp forward. Returns the current observation at the given timestamp, or an error if 
       none exists
	 * @param id
	 * @param timestamp
	 * @return
	 * @throws CommandProcessorException
	 */
	public String delete(Integer id, Long timestamp) throws CommandProcessorException {
		ConcurrentNavigableMap<Long, String> existingMap = getMapOrThrow(id);
		
		Entry<Long, String> currentEntry = existingMap.floorEntry(timestamp);
		String toReturn = currentEntry == null ? "No existing data for timestamp: " + timestamp : currentEntry.getValue();
				
		ConcurrentNavigableMap<Long, String> subMapToRetain = existingMap.headMap(timestamp);
		subMapToRetain = new ConcurrentSkipListMap<>(subMapToRetain);
		internalStore.put(id, subMapToRetain);
		
		return toReturn;			
	}
	
	/**
       Returns the data from the observation for the given identifier at the given timestamp, or
       an error if there is no available observation. 
	 * @param id
	 * @param timestamp
	 * @return
	 * @throws CommandProcessorException
	 */
	public String get(Integer id, Long timestamp) throws CommandProcessorException {
		ConcurrentNavigableMap<Long, String> existingMap = getMapOrThrow(id);
		Entry<Long, String> currentEntry = existingMap.floorEntry(timestamp);
		return currentEntry == null ? "No existing data for timestamp: " + timestamp : currentEntry.getValue();	
	}		
	
	/**
       - Locates the observation with the greatest timestamp from the history for the given identifier,
       and responds with its timestamp and data. 
	 * @param id
	 * @return
	 * @throws CommandProcessorException
	 */
	public String latest(Integer id) throws CommandProcessorException {
		ConcurrentNavigableMap<Long, String> existingMap = getMapOrThrow(id);
		String toReturn = "No existing data for id: " + id;
		Entry<Long, String> lastEntry = existingMap.lastEntry();
		
		if (lastEntry != null) {
			toReturn = lastEntry.getKey() + " " + lastEntry.getValue();
		}
		
		return toReturn;	
	}	
	
	private ConcurrentNavigableMap<Long, String> getMapOrThrow(Integer id) throws CommandProcessorException {
		ConcurrentNavigableMap<Long, String> existingMap = internalStore.get(id);
		
		if (existingMap == null) {
			throw new CommandProcessorException("No history exists for identifier '" + id + "'");		
		} else {
			return existingMap;
		}
	}
}
