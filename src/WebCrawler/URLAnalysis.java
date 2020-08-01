package WebCrawler;

public class URLAnalysis {

	private static String domain;
	private static String path;
	
	/*
	 * Constructorul clasei
	 */
	public URLAnalysis() {
		
		domain = "";
		path = "";
	}
	
	/*
	 * Functia care scoate domeniul dintr-un URL
	 */
	public static String getDomain(String url) {
		
		String aux = url.substring(7, url.length());
		if(aux.contains("/")) {
			String[] splitAux = aux.split("/");
			domain = splitAux[0];
		}else {
			domain = aux;
		}
		
		return domain;
	}

	/*
	 * Functia care scoate path-ul dintr-un URL (fara partea de fragment)
	 */
	public static String getPath(String url) {
		
		url = removeFragment(url);
		String aux = url.substring(7, url.length());
		String[] splitAux = aux.split("/");
		domain = splitAux[0];
		path = url.substring(7 + domain.length(), url.length());
		
		return path;
	}

	/*
	 * Stergerea partii de fragment din URL
	 */
	public static String removeFragment(String url) {
		
		if(url.contains("#")) {
			
			String[] splitURL = url.split("#");
			
			return splitURL[0];
		}else {
			
			return url;
		}
		
	}
	
	/*
	 * Verificam daca se foloseste protocolul HTTP
	 */
	public static boolean useHTTP(String url) {
		
		if(!url.startsWith("http://")) {
			
			return false;
		
		}else {
			
			return true;
		}
	}

	/*
	 * Returneaza URL-ul procesat pentru a fi pus in coada de explorare
	 */
	public static String getURLToPutInQueue(String siteAddress) {
		
		//Verificam daca se foloseste protocolul HTTP
		if(!useHTTP(siteAddress)) { 
			
			System.out.println("URL respins: Protocolul folosit nu este HTTP: " + siteAddress);
			return "";	
		}
		
		//Stergem fragmentul din URL
		siteAddress = removeFragment(siteAddress);
		
		return siteAddress;
	}
}
