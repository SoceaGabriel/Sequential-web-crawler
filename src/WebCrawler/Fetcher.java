package WebCrawler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Fetcher {

	//Coada de explorare
	private Queue<String> URLFrontier;
	//Domenii vizitate
	private ArrayList<String> visitedDomain;
	//Domenii respinse
	private ArrayList<String> rejectedDomain;
	//URL-uri respinse
	private ArrayList<String> rejectedURL;
 	
	//Domeniul URL-ului curent
	private String baseURL;
	//Variabila auxiliara care retine domeniul URL-ului care este analizat din lista de URL-uri care trebuie bagate in coada de explorare
	private String baseURLAux;
	
	/*
	 * Constructorul clasei
	 */
	public Fetcher() {
		
		baseURL = "";
		baseURLAux = "";
		
		URLFrontier = new LinkedList<>(); 
		visitedDomain =  new ArrayList<String>();
		rejectedDomain = new ArrayList<String>();
		rejectedURL = new ArrayList<String>();
	}
	
	/*
	 * Initializam coada cu adresa URL indicata
	 */
	private void InitQueue() {
		URLFrontier.add("http://riweb.tibeica.com/crawl/");
		URLFrontier.add("http://httpd.apache.org/docs-2.0/dso.html");
	}
	
	/*
	 * Motorul acesti aplicatii
	 */
	public void run() {
		
		//Initializam coada de URL-uri
		InitQueue();
		String url = "";
		String robots = "";
		Document doc = null;
		Elements allLinks = null;
		
		while(!URLFrontier.isEmpty()) {
			
			//scoatem un URL din coada
			url = URLFrontier.poll();
			baseURL = URLAnalysis.getDomain(url);
			System.out.println("Processing: " + url);

			//Se parseaza tagul meta -> robots
			try {
				doc = Jsoup.parse(new URL(url), 3000);
				robots = doc.select("meta[name=robots]").attr("content");
			} catch (IOException e) {
				System.out.println("Jsoup: Nu s-a putut accesa URL-ul pentru a verifica tag-ul META ROBOTS!");
			}
			
			//Daca nu exista tagul meta -> robots
			if(robots.contentEquals("")) {
				
				//Se salveaza pagina respectiva
				HTTPClient httpClient = new HTTPClient();
				httpClient.communication(url);
				//Se scoate lista de link-uri din pagina respectiva
				allLinks = doc.select("a[abs:href]");
				
			}else {
				//Daca exista tagul meta -> robots si este all sau index
				if(robots.contains("all") || robots.contains("index")) {
					
					//Se salveaza pagina respectiva
					HTTPClient httpClient = new HTTPClient();
					httpClient.communication(url);
					
				}
				//Daca exista tagul meta -> robots si este all sau follow
				if(robots.contains("all") || robots.contains("follow")) {
					
					//Se scoate lista de link-uri din pagina respectiva
					allLinks = doc.select("a[abs:href]");
				}
			}
			
			String currentURL = "";
			//Analizam lista de URL-uri si pe cele valide le bagam in coada
			for (Element elem : allLinks) {
				
				//Daca URL-ul nu este vid (#)
				if(!elem.attr("href").startsWith("#")){
				
					currentURL = elem.attr("abs:href");
					robotsVerify(currentURL);
					currentURL = "";
				}
			}
		}
	}
	
	
	/*
	 * Functia care verifica daca domeniul curent a fost vizitat sau nu si proceseaza fisierul robots.txt daca exista
	 */
	public void robotsVerify(String currentURL) {
		
		baseURLAux = URLAnalysis.getDomain(currentURL);
		
		//System.out.println("Visited domain add: " + baseURLAux); //riweb.tibeica.com
		//Daca domeniul a mai fost vizitat
		if(visitedDomain.contains(baseURLAux)) {
			
			//Daca domeniul nu este in lista de domenii respinse
			if(!rejectedDomain.contains(baseURLAux)) {
				
				boolean isOk = true;
				String currentLink = currentURL.substring(7, currentURL.length());
				//Daca URL-ul nu este in lista de URL-uri respinse
				for(int i=0; i<rejectedURL.size(); ++i) {
					if(currentLink.startsWith(rejectedURL.get(i))) {
						isOk = false;
					}
				}
				if(isOk == true) {
					
					currentURL = URLAnalysis.getURLToPutInQueue(currentURL);
					File test = new File("WebCrawlerResult/" + currentURL);
					//Verificare daca protocolul folosit este http, verificare daca URL-ul curent 
					//nu exista deja in URLFrontier, verificare daca URL-ul curent nu a mai fost explorat
					if(!currentURL.contentEquals("") && !URLFrontier.contains(currentURL) && !test.isFile()) {
						System.out.println("URL adaugat: " + currentURL);
						URLFrontier.add(currentURL);
					}
				}
			}
			
		}else{
			//Daca domeniul nu a mai fost vizitat
			//Se cere robots.txt de la domeniu respectiv
			HTTPClient reqRobots = new HTTPClient();
			reqRobots.requestRobotsTxt(baseURLAux);
			
			//Daca fisierul robots.txt nu exista
			if(reqRobots.getStatusCodeRobot().contentEquals("404 Not Found")) {
				
        		//Adaugam domeniul in lista de domenii explorate
        		visitedDomain.add(baseURLAux);
        		//Adaugam URL-ul in URL-uri de explorat
        		currentURL = URLAnalysis.getURLToPutInQueue(currentURL);
        		if(!currentURL.contentEquals("")) {
        			System.out.println("URL adaugat: " + currentURL);
					URLFrontier.add(currentURL);
        		}
        		
				
			}else if(reqRobots.getStatusCodeRobot().contentEquals("200 OK")) {
				
				//Daca robots.txt exista
        		//Adaugam domeniul in lista de domenii explorate
        		visitedDomain.add(baseURLAux);
        		//Preluam lista de URL-uri care nu pot fi explorate
        		ArrayList<String> rejURLCrawl = new ArrayList<String>();
        		ArrayList<String> rejURLStar = new ArrayList<String>();
        		if(reqRobots.getIsCrawler() == true) {
        			
        			rejURLCrawl = reqRobots.getRejectedURLCrawler();
        			if(rejURLCrawl.size() != 0) {
            			for(int i=0; i<rejURLCrawl.size(); ++i) {
            				rejectedURL.add(rejURLCrawl.get(i));
            			}
            		}
        		}else {
        			rejURLStar = reqRobots.getRejectedURLStar();
        			if(rejURLStar.size() != 0) {
            			for(int i=0; i<rejURLStar.size(); ++i) {
            				rejectedURL.add(rejURLStar.get(i));
            			}
            		}
        		}
        		
        		
        		//Daca URL-ul curent nu se afla in niciuna din liste atunci se poate explora si este trecut in coada de explorare
        		if(!rejURLCrawl.contains(currentURL) && rejURLCrawl != null) {
        			URLFrontier.add(currentURL);
        		}
        		if(!rejURLStar.contains(currentURL) && rejURLStar!= null) {
        			URLFrontier.add(currentURL);
        		}
			}
		}
	}

}
