import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RSSReader {

    //I preferred to declare these variables to be global(as fields of class)
    private static File file;
    private static int siteNum;
    private static ArrayList<String> siteURLs;

    RSSReader(){
        file = new File("data.txt");
        siteURLs = new ArrayList<String>();
        //initialize siteURLs:
        try {
            FileReader fReader = new FileReader(file);
            BufferedReader bfReader = new BufferedReader(fReader);

            int siteIndex = 0;
            String line;
            while((line = bfReader.readLine()) != null){
                String[] splittedLine = line.split(";");
                //lineSeprated[1] represents the site's URL in each line
                //saving site URLs in the array
                siteURLs.add(siteIndex, splittedLine[1]);
                siteIndex++;
            }

            bfReader.close();

            //updating number of sites
            siteNum = siteIndex;

        }
        catch (Exception e){
            System.out.println("FILE NOT FOUND");
        }
    }

    public static void main(String[] args){
        //In here I use "new" to call the constructor of class to read whatever is in the file from past
        new RSSReader();
        int command = 0;
        while(command != 4){
            command = commandGetter();
            switch (command){
                case 1:
                    showUpdates();
                    continue;
                case 2:
                    addURL();
                    continue;
                case 3:
                    removeURL();
                    continue;
                case 4:
                    return;
                default:
                    System.out.println("Please enter a valid input.");
            }

        }
    }


    public static int commandGetter(){
        Scanner scan = new Scanner(System.in);
        System.out.println("Type a valid number for your desired action:");
        System.out.println("[1] Show updates");
        System.out.println("[2] Add URL");
        System.out.println("[3] Remove URL");
        System.out.println("[4] Exit");
        if(scan.hasNextInt()){
            int command = scan.nextInt();
            return command;
        }
        else
            return -1;
    }

    public static void showUpdates(){
        try{
            FileReader fReader = new FileReader(file);
            BufferedReader bfReader = new BufferedReader(fReader);

            System.out.println("Show updates for:");
            System.out.println("[0] All websites");
            int siteIndex = 0;
            String line;
            while((line = bfReader.readLine()) != null){
                String[] splittedLine = line.split(";");
                //lineSeprated[0] represents the site's name in each line
                System.out.println("[" + (siteIndex + 1) + "] " + splittedLine[0]);
                siteIndex++;
            }
            System.out.println("Enter -1 to return");

            bfReader.close();

            //updating number of sites
            siteNum = siteIndex;

            boolean check = true; //as the defult case
            int inputNumber = 0;
            while (check){
                try{
                    Scanner scan = new Scanner(System.in);
                    inputNumber = scan.nextInt();
                    check = false;
                }
                catch (InputMismatchException e){
                    System.out.println("Please enter an integer.");
                }
            }

            if(inputNumber == 0){
                for(;siteIndex > 0; siteIndex--){
                    System.out.println(extractPageTitle(fetchPageSource(siteURLs.get(siteIndex - 1)))); //page title
                    retrieveRssContent(extractRssUrl(siteURLs.get(siteIndex - 1))); //the RSS
                }
            }
            else if(inputNumber == -1)
                return;
            else if(inputNumber >= 0 && inputNumber <= siteNum){
                System.out.println(extractPageTitle(fetchPageSource(siteURLs.get(inputNumber - 1)))); //page title
                retrieveRssContent(extractRssUrl(siteURLs.get(inputNumber - 1))); //the RSS
            }
            else
                System.out.println("Please enter a valid input.");
        }
        catch (Exception e){
            System.out.println("Error in retrieving RSS content");
        }

    }

    public static void addURL(){
        System.out.println("Please enter website URL to add:");
        Scanner scan = new Scanner(System.in);
        String URL = scan.nextLine();
        //check whether the URL exists or not
        boolean check = false; //defult status
        for(int i = 0; i < siteNum; i++){
            if(siteURLs.get(i).equals(URL)){
                check = true;
                break;
            }
        }
        if(check)
            System.out.println(URL + " already exists");
        else{
            try{
                FileWriter fWriter = new FileWriter(file, true);
                BufferedWriter bfWriter = new BufferedWriter(fWriter);
                bfWriter.write(extractPageTitle(fetchPageSource(URL)) + ";" + URL + ";" + extractRssUrl(URL) + "\n");

                bfWriter.close();

                //adding the new URL and updating the number of sites
                siteURLs.add(URL);
                siteNum++;

                System.out.println("Added " + URL + " successfully");
            }
            catch (Exception e){
                System.out.println("An error has occured.\nPlease try again.");
            }
        }
    }

    public static void removeURL(){
        System.out.println("Please enter website URL to remove:");
        Scanner scan = new Scanner(System.in);
        String URL = scan.nextLine();
        //check whether the URL exists or not
        boolean check = false; //defult status
        int siteIndex;
        for(siteIndex = 0; siteIndex < siteNum; siteIndex++){
            if(siteURLs.get(siteIndex).equals(URL)){
                check = true;
                break;
            }
        }
        if(!check)
            System.out.println("Couldn't find " + URL);
        else {
            try{
                //removing the selected URL and updating the number of sites
                siteURLs.remove(siteIndex);
                siteNum--;

                FileWriter fWriter = new FileWriter(file);
                BufferedWriter bfWriter = new BufferedWriter(fWriter);

                for(int i = 0; i < siteNum; i++)
                    bfWriter.write(extractPageTitle(fetchPageSource(siteURLs.get(i))) + ";" + siteURLs.get(i) +  ";" + extractRssUrl(siteURLs.get(i)) + "\n");


                bfWriter.close();


                System.out.println("Removed " + URL + "successfully.");
            }
            catch (Exception e){
                System.out.println("An error has occured.\nPlease try again.");
            }
        }
    }

    public static String extractPageTitle(String html) {
        try
        {
             org.jsoup.nodes.Document doc = Jsoup.parse(html);
             return doc.select("title").first().text();
        }
         catch (Exception e)
         {
             return "Error: no title tag found in page source!";
         }
    }
    public static String extractRssUrl(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < 5; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }

    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , " +
                "like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }

    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }

    //getter and setter methods for encapsulation
    public ArrayList<String> getSiteURLs() {
        return siteURLs;
    }

    public int getSiteNum() {
        return siteNum;
    }

    public void setSiteNum(int siteNums) {
        this.siteNum = siteNums;
    }

    public void setSiteURLs(ArrayList<String> siteURLs) {
        this.siteURLs = siteURLs;
    }

    public static File getFile() {
        return file;
    }

    public static void setFile(File file) {
        RSSReader.file = file;
    }
}
