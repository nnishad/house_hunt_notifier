package com.nikhilnishad.househunt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nikhilnishad.househunt.model.EmailDetails;
import com.nikhilnishad.househunt.model.PropertyDetails;
import com.nikhilnishad.househunt.service.EmailService;

import ch.qos.logback.core.spi.PropertyDefiner;
import io.github.bonigarcia.wdm.WebDriverManager;

@SpringBootApplication
public class HousehuntServiceApplication implements CommandLineRunner{

    Logger log = LoggerFactory.getLogger(HousehuntServiceApplication.class);

	@Autowired
    private EmailService emailService;
	
	private WebDriver driver;
    private Set<PropertyDetails> visitedLinks;
	
	public static void main(String[] args) {
		SpringApplication.run(HousehuntServiceApplication.class, args);
	}

	public void setUp(){
		System.out.println("Getting Ready");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
        options.addArguments("user-agent="+userAgent);
        driver = new ChromeDriver(options);
        driver.navigate()
        .to("https://www.rightmove.co.uk/property-to-rent/find.html?locationIdentifier=REGION%5E93616&minBedrooms=2&maxPrice=1200&radius=3.0&propertyTypes=&maxDaysSinceAdded=1&includeLetAgreed=false&mustHave=&dontShow=&furnishTypes=furnished&keywords=");
        driver.manage().timeouts().implicitlyWait(150,TimeUnit.SECONDS);
    }
	
	@Override
	public void run(String... args) throws Exception {
		setUp();
		log.info("web driver setup-complete");
		new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
            	checkNewProperty();
            }
        },100,60000);
	}
	
	private void checkNewProperty() {
		try {
			driver.navigate()
            .to("https://www.rightmove.co.uk/property-to-rent/find.html?locationIdentifier=REGION%5E93616&minBedrooms=2&maxPrice=1200&radius=3.0&propertyTypes=&maxDaysSinceAdded=1&includeLetAgreed=false&mustHave=&dontShow=&furnishTypes=furnished&keywords=");
			driver.manage().timeouts().implicitlyWait(120,TimeUnit.SECONDS);
			Thread.sleep(1000);
			readVisitedList();
			List<PropertyDetails> propertyDetailsList = createPropertyList();
			log.info("property profiles ready");
			if(!propertyDetailsList.isEmpty()) {
				sendEmails(propertyDetailsList);
				log.info("email sent");
				visitedLinks.addAll(propertyDetailsList);
			    updateVisitedList();
				log.info("visited properties added to file");
			}
		} catch (Exception e) {
			e.printStackTrace();
			driver.close();
			System.exit(0);
		}
	}

    public void readVisitedList(){
        try{
            FileInputStream fileInputStream
                    = new FileInputStream("visitedList.data");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            visitedLinks = (Set<PropertyDetails>) objectInputStream.readObject();
            objectInputStream.close();
            driver.manage().timeouts().implicitlyWait(120,TimeUnit.SECONDS);
        } catch (FileNotFoundException e) {
            visitedLinks=new HashSet<PropertyDetails>();
            updateVisitedList();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void sendEmails(List<PropertyDetails> propertyLinks) {
    	emailService.sendSimpleMail(
				new EmailDetails(
						"nikhilnishaduk+autopilot@gmail.com",
						propertyLinks.toString(),
						"New Properties || AutoPilot",
						null)
				);
    }
    
    public void updateVisitedList(){
        try {
            FileOutputStream fileOutputStream
                    = new FileOutputStream("visitedList.data");
            ObjectOutputStream objectOutputStream
                    = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(visitedLinks);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

	private List<PropertyDetails> createPropertyList(){
		List<PropertyDetails> finalPropertyDetailsList=new ArrayList<>();
		try {
			WebElement propertyListParentDiv = driver.findElement(By.className("l-searchResults"));
			if(propertyListParentDiv!=null) {
				List<WebElement> propertyDivsList = propertyListParentDiv.findElements(By.className("is-list"));
				if(propertyDivsList.size()>0) {
					propertyDivsList.forEach(propertyDiv->{
						PropertyDetails propertyDetails=new PropertyDetails();
						propertyDetails.setId(propertyDiv.findElement(By.className("propertyCard-anchor")).getAttribute("id"));
						propertyDetails.setPropertyUrl(propertyDiv.findElement(By.className("propertyCard-details")).findElement(By.tagName("a")).getAttribute("href"));
						propertyDetails.setPrice(propertyDiv.findElement(By.className("propertyCard-priceValue")).getText());
						propertyDetails.setAddress(propertyDiv.findElement(By.tagName("address")).getText());
						propertyDetails.setPhoneNumber(propertyDiv.findElement(By.className("propertyCard-contactsPhoneNumber")).getAttribute("href").split(":")[1]);
						propertyDetails.setContactFormLink(propertyDiv.findElement(By.className("mail-icon")).getAttribute("href"));
						if(!visitedLinks.contains(propertyDetails)) {
							finalPropertyDetailsList.add(propertyDetails);
						}
					});	
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		visitedLinks.addAll(finalPropertyDetailsList);
		return finalPropertyDetailsList;
	}
}
