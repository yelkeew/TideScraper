# TideScraper
Java-based web scraper for tide data

## Notes
* Scrapes https://www.tide-forecast.com for location-based data on low-tides occurring after sunrise before sunset
* Uses [Jsoup](https://jsoup.org/) library for scraping
* Build/run with Eclipse on Windows 10 using Java 1.8
* Alternately, from the (Windows) command line:
    * `> cd TideScraper`
    * `> mvn clean package`
    * `> java -cp "target\TideScraper-0.0.1.jar;target\deps\*" com.instavector.tidescraper.TideScraperMain`