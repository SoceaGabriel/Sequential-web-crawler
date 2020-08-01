package WebCrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.jws.soap.SOAPBinding.Use;

public class HTTPClient {

	//Variabila care retine domeniul unui URL
	private String domain;
	//Variabila care retine path-ul unui URL
	private String path;
	//Variabila care retine cererea
	private StringBuilder request;
	//Partul pe care ruleaza aplicatia
	private int port;
	//Directorul in care se va salva continutul paginilor
	private final String SAVE_DIRECTOR = "WebCrawlerResult";
	//Adresa IP
	private InetAddress addr;
	//Socketul pentru comunicare
	private Socket socket;
	//Codul cererii resursei robots.txt
	private String statusCodeRobot;
	//Lista de URL-uri respinse pentru robotul acesta web
	private ArrayList<String> rejectedURLCrawler;
	//Lista de URL-uri respinse toti robotii web in afara de acesta
	private ArrayList<String> rejectedURLStar;
	//Variabila care memoreaza daca s-a intalnit specificatii pentru acest robot in robots.txt
	boolean isRIWEB_CRAWLER = false;
	
	/*
	 * Constructorul clasei
	 */
	public HTTPClient() {
		
		domain = "";
		path = "";
		request = new StringBuilder();
		port = 80;
		statusCodeRobot ="";
		rejectedURLCrawler = new ArrayList<String>();
		rejectedURLStar = new ArrayList<String>();
	}
	
	public String getStatusCodeRobot() {
		return statusCodeRobot;
	}
	
	
	public ArrayList<String> getRejectedURLStar() {
		return rejectedURLStar;
	}

	public ArrayList<String> getRejectedURLCrawler() {
		return rejectedURLCrawler;
	}
	
	public boolean getIsCrawler(){
		return isRIWEB_CRAWLER;
	}

	/*
	 * Functia care creeaza cererea HTTP pentru un URL
	 */
	public void createRequest(String siteAddress) {
		
		//scoatem domeniu si path-ul din URL
		domain = URLAnalysis.getDomain(siteAddress);
		path = URLAnalysis.getPath(siteAddress);
		
		//Creem cererea
		request.append("GET " + path + " HTTP/1.1" + "\r\n");
		request.append("Host: " + domain + "\r\n");
		request.append("User-Agent: RIWEB_CRAWLER" + "\r\n");
		request.append("Connection: close" + "\r\n\r\n");
	}
	
	/*
	 * Functia care creeaza cererea HTTP pentru fisierul robots.txt al unui domeniu
	 */
	public void createRequestRobots(String dom) {
		
		//Creem cererea
		request.append("GET " + "/robots.txt" + " HTTP/1.1" + "\r\n");
		request.append("Host: " + dom + "\r\n");
		request.append("User-Agent: RIWEB_CRAWLER" + "\r\n");
		request.append("Connection: close" + "\r\n\r\n");
	}
	
	/*
	 * Functia care trimite cererea HTTP la server si salveaza continutul paginii
	 */
	public void communication(String siteAddress) {
		
		//Creem cererea
		createRequest(siteAddress);
		
        addr = null;
        socket = null;
        FileWriter fw = null;
        File fl;
        
		try {
			
			//Trimitem cererea la server
			addr = InetAddress.getByName(domain);
			socket = new Socket(addr, port);
			socket.setSoTimeout(3000);
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
			wr.write(request.toString());
			wr.flush();
			
			//Primim raspunsul de la server
			BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
             
            //Creem structura de foldere si fisiereul in care va fi scris continutul
            String fullPath = SAVE_DIRECTOR;
            String link = domain + path;
            if(link.endsWith("/")) {
            	
            	String[] directories = link.split("/");
            	for(int i=0; i<directories.length; i++) {
            		
            		fullPath += ("/" + directories[i]);
            		File dir = new File(fullPath);
            		if (!dir.exists()) {
            			dir.mkdir();
            		}
            	}
            	fullPath += "/index.html";
            }else {
            	
            	String[] directories = link.split("/");
            	for(int i=0; i<directories.length - 1; i++) {
            		
            		fullPath += ("/" + directories[i]);
            		File dir = new File(fullPath);
            		if (!dir.exists()) {
            			dir.mkdir();
            		}
            	}
            	fullPath += "/" + (directories[directories.length-1]);
            }
            
            fl = new File(fullPath);
			fw = new FileWriter(fl, true);
            int flag = 0;
            int isContent = 0;
            
            //Verificam codul de raspuns si daca este 200 scriem rezultatul intr-un fisier HTML
            while ((line = rd.readLine()) != null) {
                
            	if(line.startsWith("HTTP") && (flag==0)) {
            	
            		flag = 1;
            		String[] splitLine = line.split(" ");
            		if(!splitLine[1].contentEquals("200")) {
            			
            			System.out.println("Error: " + splitLine[1]);
            			return;
            		}
            	}
            	
            	if(line.startsWith("<") && (isContent == 0)) {
            		
            		isContent = 1;
            	}
            	
            	if(isContent == 1) {
            		fw.write(line);
            	}
            }
            
            fw.flush();
            fw.close();
            wr.close();
            rd.close();

            socket=null;
            
		} catch (UnknownHostException e) {
			System.out.println("Eroare: Host necunoscut! Nu s-a putut realiza cererea HTTP deoarece nu s-a indentificat corect host-ul");
		}
		catch (IOException e) {
			System.out.println("Eroare: Nu s-a putut crea socket-ul! Request timeout");
		}
	}
	
	/*
	 * Functia care cere preia resursa robots.txt de la un domeniu daca aceasta exista
	 */
	public void requestRobotsTxt(String dom) {
		
		//Creem cererea
		createRequestRobots(dom);
		
        addr = null;
        socket = null;
        FileWriter fw = null;
        File fl;
        
		try {
			
			//Trimitem cererea la server
			addr = InetAddress.getByName(dom);
			socket = new Socket(addr, port);
			socket.setSoTimeout(3000);
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
			wr.write(request.toString());
			wr.flush();
			
			//Primim raspunsul de la server
			BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            
            int flag = 0;
            int isContent = 0;
            
            //Verificam codul de raspuns daca este 200 sau 404 sau 
            line = rd.readLine();
            if(line.contains("200 OK") && line != null) {
            	this.statusCodeRobot = "200 OK";
            }else if(line.contains("404 Not Found") && line != null) {
            	this.statusCodeRobot = "404 Not Found";
            	System.out.println("Fisierul robots.txt corespunzator domeniului " + dom + " nu exista!");
            }
            //Daca exista fisierul robots.txt
            if(this.statusCodeRobot.contentEquals("200 OK")) {
            	boolean isMesage = false;
            	String UserAgent = "";
                while ((line = rd.readLine()) != null) {

                	if(isMesage == true) {
                		
                		if(line.contains("User-agent:")) {
                			UserAgent = line.split(" ")[1];
                		}
                		if(line.contains("Disallow:")) {
                			if(UserAgent.contains("RIWEB_CRAWLER")){
                				String path = line.substring(9, line.length());
                				if(path.length() > 0) {
                					path = path.substring(1, path.length());
                					if(path.endsWith("*")) {
                    					path = line.substring(0, path.length()-1);
                    				}
                					rejectedURLCrawler.add(dom + path);
                				}
                				isRIWEB_CRAWLER = true;
                				
                			}else if(UserAgent.contains("*") && (isRIWEB_CRAWLER == false)) {
                				
                				String path = line.substring(9, line.length());
                				if(path.length() > 0) {
                					path = path.substring(1, path.length());
                					if(path.endsWith("*")) {
                    					path = line.substring(0, path.length()-1);
                    				}
                    				rejectedURLStar.add(dom + path);
                				}
                			}
                		}
                	}
                	if(line.contentEquals("") && (isMesage == false)) {
                		isMesage = true;
                	}
                }
            }
            wr.close();
            rd.close();

            socket=null;
            
		} catch (UnknownHostException e) {
			System.out.println("Eroare: host necunoscut!");
		}
		catch (IOException e) {
			System.out.println("Eroare: socket-ul nu poate fi creat! Request timeout");
		}
	}
}
