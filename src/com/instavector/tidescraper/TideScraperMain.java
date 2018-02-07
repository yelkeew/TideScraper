package com.instavector.tidescraper;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TideScraperMain {

	private static final String TIDE_URL_PREFIX = "https://www.tide-forecast.com/locations/";
	private static final String TIDE_URL_SUFFIX = "/tides/latest";

	// State of tide table parsing
	private enum ParseState {
		ST_UNDEFINED, ST_NEW_DAY, ST_SUNRISE, ST_SUNSET
	}

	// Format location for URL pattern
	private String formatLocation(String location) {
		return location.replace(",", "").replace(" ", "-");
	}

	// Scrape tide table for geographic location - display low tides for each day after sunrise, before sunset
	private void scrape(String location) {
		String url = TIDE_URL_PREFIX + formatLocation(location) + TIDE_URL_SUFFIX;

		try {
			Document doc = Jsoup.connect(url).get();
			if (null == doc) {
				System.out.println("ERROR: couldn't get content for " + url);
				return;
			}

			System.out.println("Daytime Low Tides for " + location);

			// Get rows for first table in page
			Elements days = doc.select("table").get(0).select("tr");
			if (days.isEmpty()) {
				System.out.println("ERROR: couldn't get tide table from page");
				return;
			}

			ParseState state = ParseState.ST_UNDEFINED;
			boolean enoughTideDataForDay = false;
			boolean daylightLowTideOccurred = false;
			StringBuilder sb = new StringBuilder("");

			for (Element day : days) {
				// System.out.println(day.toString().replace("\n", ""));
				// Get first cell in row, see if it's a date or a time
				Elements columns = day.select("td");
				Element firstColumn = columns.get(0);
				String firstColumnClass = firstColumn.attr("class");
				if ("date".equals(firstColumnClass)) {
					if (0 != sb.length()) {
						if (!enoughTideDataForDay) {
							sb.append("Too late in day, no tide data available");
						}
						System.out.println(sb.toString());
						sb.setLength(0);
					}
					sb.append("   " + String.format("%-22s", firstColumn.text()) + " | ");
					state = ParseState.ST_NEW_DAY;
					daylightLowTideOccurred = false;
				}
				Element lastColumn = columns.get(columns.size() - 1);
				String eventText = lastColumn.text();
				if ("Sunrise".equals(eventText)) {
					if (ParseState.ST_NEW_DAY != state) {
						System.out.println("ERROR: sunrise without new day ???");
					} else {
						state = ParseState.ST_SUNRISE;
					}
				} else if ("Low Tide".equals(eventText)) {
					// Print low-tide level after sunrise, before sunset
					if (ParseState.ST_SUNRISE == state) {
						enoughTideDataForDay = true;
						Elements levelColumns = columns.select("td.level");
						if (!daylightLowTideOccurred) {  // Check for multiple daylight low tides
							sb.append("Low Tide:");
						} else {
							sb.append(",");
						}
						for (Element lc : levelColumns) {
							// Display metric & imperial low tide measurement
							sb.append(" " + lc.text());
						}
						daylightLowTideOccurred = true;
					}
				} else if ("Sunset".equals(eventText)) {
					if (ParseState.ST_SUNRISE != state) {
						System.out.println("ERROR: sunset before sunrise ???");
					} else {
						if (!daylightLowTideOccurred) {
							enoughTideDataForDay = true;
							sb.append("No daylight low tide");
						}
						state = ParseState.ST_SUNSET;
					}
				}
			} // end -- iterating over days in table
		} catch (IOException e) {
			System.out.println("ERROR: couldn't get/scrape " + url);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		String[] locations = { "Half Moon Bay, California", "Huntington Beach", // ", California", // Tide forecase DB
				// doesn't like "California" in this
				// case
				"Providence, Rhode Island", "Wrightsville Beach, North Carolina" };

		TideScraperMain scraper = new TideScraperMain();
		for (String loc : locations) {
			scraper.scrape(loc);
		}
	}

}
