package com.nikhilnishad.househunt.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import com.nikhilnishad.househunt.model.LocationEmailMapping;
import com.nikhilnishad.househunt.model.PropertyDetails;
import com.nikhilnishad.househunt.repository.LocationEmailMappingDao;

import io.github.bonigarcia.wdm.WebDriverManager;

@Service
public class NotifierService implements TaskSchedulerCustomizer{

	Logger log = LoggerFactory.getLogger(NotifierService.class);

	//@Autowired
	WebDriver webDriverInstance;

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private LocationEmailMappingDao locationEmailMappingDao;
	
	public static boolean isDbUpdateFlag=true;
	
	private List<LocationEmailMapping> allLocationList= new ArrayList<>();
	
	private WebDriver webDriver() {
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

	 @Scheduled(fixedDelay = 30000)
	public void scheduleFixedDelayTask() {
		 if(isDbUpdateFlag) {
			 log.info("isDbUpdateFlag is :: ",true);
			 List<LocationEmailMapping> updatedAllLocationList = locationEmailMappingDao.findAll();
			 if(!updatedAllLocationList.isEmpty()) {
				 log.info("updatedAllLocationList is not empty:: ",updatedAllLocationList);
				 allLocationList=updatedAllLocationList;
				 isDbUpdateFlag=false;
			 }
		 }
		 log.info("reading allLocationList ::"+allLocationList);
		 allLocationList.forEach(location->checkNewProperty(location));
	}

	private void checkNewProperty(LocationEmailMapping location) {
		try {
			log.info("inside checkNewProperty");
			webDriverInstance=webDriver();
			webDriverInstance.navigate()
					.to("https://www.rightmove.co.uk/" + "property-to-rent/find.html?"
							+ "locationIdentifier="+location.getLocationPreference().getLocationIdentifier()
							+ "&minBedrooms="+location.getLocationPreference().getMinBedrooms() 
							+ "&maxPrice="+location.getLocationPreference().getMaxPrice() 
							+ "&radius="+location.getLocationPreference().getRadius()
							+ "&propertyTypes=" 
							+ "&maxDaysSinceAdded=3" 
							+ "&includeLetAgreed=false" 
							+ "&mustHave="
							+ "&dontShow=" 
							+ "&furnishTypes="+location.getLocationPreference().getFurnishTypes() 
							+ "&keywords=");
			log.info("web driver navigated for location");
			webDriverInstance.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
			Thread.sleep(1000);
			Map<String,List<PropertyDetails>> trackedVisitedList = getTrackedVisitedListFromFile();
			log.info("reading file stored list ::"+trackedVisitedList.size());
			Map<String, List<PropertyDetails>> propertyDetailsList = createPropertyList(trackedVisitedList,location.getUserEmailList());
			if (!propertyDetailsList.isEmpty()) {
				log.info("Found new properties at " + java.time.LocalTime.now());
				sendEmails(propertyDetailsList);
				log.info("email sent");
				propertyDetailsList.forEach((email,propertyDetails)->{
					if(trackedVisitedList.containsKey(email)) {
						List<PropertyDetails> copyPropertyListForEmail = trackedVisitedList.get(email).stream()
								  .collect(Collectors.toList());
						copyPropertyListForEmail.addAll(propertyDetails);
						propertyDetails=copyPropertyListForEmail;
					}
					trackedVisitedList.put(email, propertyDetails);
					
				});
				updateVisitedList(trackedVisitedList);
				log.info("visited properties added to file");
			} else {
				log.info("No new property at " + java.time.LocalTime.now());
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

	private Map<String,List<PropertyDetails>> getTrackedVisitedListFromFile() {
		Map<String,List<PropertyDetails>> trackedPropertyList;
		try {
			FileInputStream fileInputStream = new FileInputStream("visitedList.data");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			trackedPropertyList = (Map<String,List<PropertyDetails>>) objectInputStream.readObject();
			objectInputStream.close();
		} catch (FileNotFoundException e) {
			trackedPropertyList = new ConcurrentHashMap<String,List<PropertyDetails>>();
			updateVisitedList(trackedPropertyList);
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return trackedPropertyList;
	}

	private void updateVisitedList(Map<String,List<PropertyDetails>> visitedProperties) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("visitedList.data");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(visitedProperties);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Map<String, List<PropertyDetails>> createPropertyList(Map<String,List<PropertyDetails>> trackedVisitedList, List<String> emailList) {
		Map<String,List<PropertyDetails>> finalEmailPropertyListMap=new ConcurrentHashMap<>();
		try {
			WebElement propertyListParentDiv = webDriverInstance.findElement(By.className("l-searchResults"));
			if (propertyListParentDiv != null) {
				List<WebElement> propertyDivsList = propertyListParentDiv.findElements(By.className("is-list"));
				if (propertyDivsList.size() > 0) {
					propertyDivsList.forEach(propertyDiv -> {
						PropertyDetails propertyDetails = new PropertyDetails();
						propertyDetails
								.setId(propertyDiv.findElement(By.className("propertyCard-anchor")).getAttribute("id"));
						propertyDetails.setPropertyUrl(propertyDiv.findElement(By.className("propertyCard-details"))
								.findElement(By.tagName("a")).getAttribute("href"));
						propertyDetails
								.setPrice(propertyDiv.findElement(By.className("propertyCard-priceValue")).getText());
						propertyDetails.setAddress(propertyDiv.findElement(By.tagName("address")).getText());
						propertyDetails.setPhoneNumber(
								propertyDiv.findElement(By.className("propertyCard-contactsPhoneNumber"))
										.getAttribute("href").split(":")[1]);
						propertyDetails.setContactFormLink(
								propertyDiv.findElement(By.className("mail-icon")).getAttribute("href"));
						
						
						emailList.forEach(email->{
							if(finalEmailPropertyListMap.containsKey(email)) {
								List<PropertyDetails> newPropertyListForGivenEmail = 
										finalEmailPropertyListMap.get(email).stream()
										  .collect(Collectors.toList());
								if(trackedVisitedList.containsKey(email)) {
									List<PropertyDetails> trackedPropertyListForGivenEmail = trackedVisitedList.get(email);
									if(!trackedPropertyListForGivenEmail.contains(propertyDetails)) {
										newPropertyListForGivenEmail.add(propertyDetails);
										finalEmailPropertyListMap.put(email, newPropertyListForGivenEmail);
									}
								}
								else {
									newPropertyListForGivenEmail.add(propertyDetails);
									finalEmailPropertyListMap.put(email, newPropertyListForGivenEmail);
								}
							}
							else {
								if(trackedVisitedList.containsKey(email)) {
									List<PropertyDetails> trackedPropertyListForGivenEmail = trackedVisitedList.get(email);
									if(!trackedPropertyListForGivenEmail.contains(propertyDetails)) {
										finalEmailPropertyListMap.put(email, List.of(propertyDetails));
									}
								}
								else {
									finalEmailPropertyListMap.put(email, List.of(propertyDetails));
								}
							}
						});
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalEmailPropertyListMap;
	}

	private void sendEmails(Map<String, List<PropertyDetails>> propertyDetailsList) {
    	LocalDateTime currentDateAndTime = LocalDateTime.now(DateTimeZone.forID("Europe/London"));
		propertyDetailsList.keySet().forEach(email->{
			emailService.sendSimpleMail(new EmailDetails(email, propertyDetailsList.get(email).toString(),
					"New Properties found at "+currentDateAndTime.toString()+" || AutoPilot", null));
		});
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
