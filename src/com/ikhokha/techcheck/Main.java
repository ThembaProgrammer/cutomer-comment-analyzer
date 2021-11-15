package com.ikhokha.techcheck;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class Main {

	public static void main(String[] args) {

		final ConcurrentMap<String, Integer> totalResults = new ConcurrentHashMap<>();
		final File docPath = new File("docs");
		final File[] commentFiles = docPath.listFiles((d, n) -> n.endsWith(".txt"));
		final CountDownLatch countDownLatch = new CountDownLatch(commentFiles.length);

		for (final File commentFile : commentFiles) {
			new Thread(new CommentAnalyzer(commentFile,totalResults, countDownLatch)).start();
		}

		try {
			countDownLatch.await();
		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.getCause());
		}

		System.out.println("RESULTS\n=======");
		totalResults.forEach((k,v) -> System.out.println(k + " : " + v));
	}
}
