package com.ikhokha.techcheck;

import com.ikhokha.techcheck.util.CommentCondition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class CommentAnalyzer implements Runnable {

    // Used to check url on string
    private static final String URL_REGEX = "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)";

    //Metrics list for reporting
    final static List<CommentCondition> commentConditionList = initializeMetrics();

    private File file;
    private ConcurrentMap<String, Integer> totalResults;

    // Concurrent execution count down
    private CountDownLatch countDownLatch;
	
	public CommentAnalyzer(final File file,
            final ConcurrentMap<String, Integer> totalResults,
            final CountDownLatch countDownLatch) {
		this.file = file;
		this.totalResults = totalResults;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void run() {

        final Map<String, Integer> resultsMap = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				
				for(final CommentCondition commentCondition : commentConditionList){
					if (commentCondition.getCondition().test(line)){
						incOccurrence(resultsMap,commentCondition.getKey());
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + file.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO Error processing file: " + file.getAbsolutePath());
			e.printStackTrace();
		}

		// add count results into total results sequential
		synchronized (totalResults){
		    addReportResults(resultsMap, totalResults);
        }

		countDownLatch.countDown();

	}

    /**
     * Initialize metrics for creating comment reports
     * This method can be used to read metrics from data source
     * @return List of metrics
     */
	private static List initializeMetrics() {
		List<CommentCondition> commentConditions = new ArrayList<>();

		commentConditions
				.add(new CommentCondition("SHORTER_THAN_15").condition(line -> line.length() < 15));
		commentConditions
				.add(new CommentCondition("MOVER_MENTIONS")
                        .condition(line -> line.toLowerCase().contains("mover")));
		commentConditions
				.add(new CommentCondition("SHAKER_MENTIONS")
                        .condition(line -> line.toLowerCase().contains("shaker")));
		commentConditions
                .add(new CommentCondition("QUESTIONS")
                        .condition(line -> line.contains("?")));
        commentConditions
                .add(new CommentCondition("SPAM")
                        .condition(line -> Pattern.compile(URL_REGEX).matcher(line).find()));

		return commentConditions;
	}

	/**
	 * This method increments a counter by 1 for a match type on the countMap. Uninitialized keys will be set to 1
	 * @param countMap the map that keeps track of counts
	 * @param key the key for the value to increment
	 */
	private void incOccurrence(Map<String, Integer> countMap, String key) {
		
		countMap.putIfAbsent(key, 0);
		countMap.put(key, countMap.get(key) + 1);
	}

    /**
     * This method adds the result counts from a source map to the target map
     * @param source the source map
     * @param target the target map
     */
    private static void addReportResults(Map<String, Integer> source, Map<String, Integer> target) {

        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            target.putIfAbsent(entry.getKey(), 0);
            target.put(entry.getKey(), target.get(entry.getKey()) + entry.getValue());
        }

    }
}
