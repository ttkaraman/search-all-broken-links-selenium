package searchBrokenLinks;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SearchBrokenLinks {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        String homepage = "https://www.amazon.de";
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(homepage);


        String url;
        int respCode;
        HttpURLConnection huc = null;
        ArrayList<String> failedLinks = new ArrayList<>();
        ArrayList<String> ListXpathEmptyLinks = new ArrayList<>();
        ArrayList<String> incorrectLink = new ArrayList<>();
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));
        String xpathEmptyLink;


        for (WebElement each : allLinks) {
            url = each.getAttribute("href");
            if (url == null || url.isEmpty()) {
                xpathEmptyLink = generateXPATH(each, "");
                ListXpathEmptyLinks.add(xpathEmptyLink);
                continue;
            }

            if (!(url.startsWith("http://") || url.startsWith("https://"))) {
                incorrectLink.add(url);
                continue; // throws illegal argument exception if url is incorrect
            }

            try {
                //huc = (HttpURLConnection) new URL(null, url, new sun.net.www.protocol.https.Handler());

                huc = (HttpURLConnection)(new URL(url).openConnection());

                huc.setRequestMethod("HEAD");

                huc.connect();

                respCode = huc.getResponseCode();

                if (respCode >= 400) {
                    failedLinks.add(url);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        driver.quit();

        if (ListXpathEmptyLinks.isEmpty() && incorrectLink.isEmpty() && failedLinks.isEmpty()){
            System.out.println("All links are fine");
        } else {
            if (!ListXpathEmptyLinks.isEmpty()) {
                System.out.println("xpaths of empty 'a' tags: "+ListXpathEmptyLinks);
            }
            if (!incorrectLink.isEmpty()){
                System.out.println("incorrect links not tested: "+incorrectLink);
            }
            if(!failedLinks.isEmpty()){
                System.out.println("broken links: "+failedLinks);
            }
        }





    }
    public static String generateXPATH(WebElement childElement, String current) {
        String childTag = childElement.getTagName();
        if(childTag.equals("html")) {
            return "/html[1]"+current;
        }
        WebElement parentElement = childElement.findElement(By.xpath(".."));
        List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
        int count = 0;
        for (WebElement childrenElement : childrenElements) {
            String childrenElementTag = childrenElement.getTagName();
            if (childTag.equals(childrenElementTag)) {
                count++;
            }
            if (childElement.equals(childrenElement)) {
                return generateXPATH(parentElement, "/" + childTag + "[" + count + "]" + current);
            }
        }
        return null;
    }
}
