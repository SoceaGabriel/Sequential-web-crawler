package WebCrawler;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestClass {

	public static void main(String[] args) {

		Fetcher fetcher = new Fetcher();
		fetcher.run();
	}

}
