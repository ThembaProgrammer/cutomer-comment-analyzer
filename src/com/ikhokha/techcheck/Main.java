package com.ikhokha.techcheck;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {

		final ConcurrentMap<String, Integer> totalResults = new ConcurrentHashMap<>();
		final File docPath = new File("docs");
		final File[] commentFiles = docPath.listFiles((d, n) -> n.endsWith(".txt"));

		final ExecutorService executor = Executors.newFixedThreadPool(10);

		for (final File commentFile : commentFiles) {
			executor.submit(new CommentAnalyzer(commentFile,totalResults));
		}

		try {
			executor.shutdown();
			executor.awaitTermination(2, TimeUnit.MINUTES);
		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getCause());
		}

		System.out.println("RESULTS\n=======");
		totalResults.forEach((k,v) -> System.out.println(k + " : " + v));
	}
}
