package com.nikhilnishad.househunt.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.nikhilnishad.househunt.model.EmailDetails;
import com.nikhilnishad.househunt.model.PropertyDetails;

import io.github.bonigarcia.wdm.WebDriverManager;

@Service
public class NotifierService implements TaskSchedulerCustomizer{

	Logger log = LoggerFactory.getLogger(NotifierService.class);

	//@Autowired
	WebDriver webDriverInstance;
	
	@Autowired
    private EmailService emailService;
	
    public WebDriver webDriver() {
		log.info("Getting WebDriver Ready");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
        options.addArguments("user-agent="+userAgent);
    	WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(150,TimeUnit.SECONDS);
        log.info("WebDriver Ready");
        return driver;
    }

	@Scheduled(fixedDelay = 60000)
	public void scheduleFixedDelayTask() {
		log.info("Triggered");
		checkNewProperty();
	}

	private void checkNewProperty() {
		try {
			webDriverInstance=webDriver();
			webDriverInstance.navigate().to(
					"https://www.rightmove.co.uk/"
					+ "property-to-rent/find.html?"
					+ "locationIdentifier=REGION%5E93616"
					+ "&minBedrooms=2"
					+ "&maxPrice=1200"
					+ "&radius=3.0"
					+ "&propertyTypes="
					+ "&maxDaysSinceAdded=1"
					+ "&includeLetAgreed=false"
					+ "&mustHave="
					+ "&dontShow="
					+ "&furnishTypes=furnished"
					+ "&keywords=");
			webDriverInstance.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
			Thread.sleep(1000);
			Set<PropertyDetails> trackedVisitedList=getTrackedVisitedListFromFile();
			List<PropertyDetails> propertyDetailsList = createPropertyList(trackedVisitedList);
			if (!propertyDetailsList.isEmpty()) {
				log.info("Found new properties at "+java.time.LocalTime.now());
				sendEmails(propertyDetailsList);
				log.info("email sent");
				trackedVisitedList.addAll(propertyDetailsList);
				updateVisitedList(trackedVisitedList);
				log.info("visited properties added to file");
			}
			else {
				log.info("No new property at "+java.time.LocalTime.now());
			}
		} catch (Exception e) {
			e.printStackTrace();
			webDriverInstance.close();
			webDriverInstance.quit();
			System.exit(0);
		}
		finally {
			webDriverInstance.close();
			webDriverInstance.quit();
			log.info("WebDriver closed. Waiting for next trigger");
		}
	}
	
    private Set<PropertyDetails> getTrackedVisitedListFromFile(){
		log.info("Trying to read datd from file.");
        Set<PropertyDetails> trackedPropertyList;
		try{
            FileInputStream fileInputStream
                    = new FileInputStream("visitedList.data");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            trackedPropertyList = (Set<PropertyDetails>) objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
    		log.info("File not found. Trying to create new file.");
        	trackedPropertyList=new HashSet<PropertyDetails>();
            updateVisitedList(trackedPropertyList);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
		log.info("Data Read complete.");
        return trackedPropertyList;
    }
	
    private void updateVisitedList(Set<PropertyDetails> visitedProperties){
        try {
            FileOutputStream fileOutputStream
                    = new FileOutputStream("visitedList.data");
            ObjectOutputStream objectOutputStream
                    = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(visitedProperties);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
		log.info("New file created.");
    }
    
	private List<PropertyDetails> createPropertyList(Set<PropertyDetails> trackedVisitedList){
		log.info("Creating new property list");
		List<PropertyDetails> finalPropertyDetailsList=new ArrayList<>();
		try {
			WebElement propertyListParentDiv = webDriverInstance.findElement(By.className("l-searchResults"));
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
						if(!trackedVisitedList.contains(propertyDetails)) {
							finalPropertyDetailsList.add(propertyDetails);
						}
					});	
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalPropertyDetailsList;
	}

    private void sendEmails(List<PropertyDetails> propertyLinks) {
    	LocalDateTime currentDateAndTime = LocalDateTime.now(DateTimeZone.forID("Europe/London"));
    	emailService.sendSimpleMail(
				new EmailDetails(
						"nikhilnishadatuk+autopilot@gmail.com",
						propertyLinks.toString(),
						"New Properties found at "+currentDateAndTime.toString()+" || AutoPilot",
						null)
				);
    }

	@Override
	public void customize(ThreadPoolTaskScheduler taskScheduler) {
		taskScheduler.setErrorHandler(t -> { 
			webDriverInstance.close();
			webDriverInstance.quit();
	        log.error("Scheduled task threw an exception: {} & webdriver force closed ::", t.getMessage(), t);

		});
	}
}
