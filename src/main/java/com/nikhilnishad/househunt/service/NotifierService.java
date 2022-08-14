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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nikhilnishad.househunt.model.EmailDetails;
import com.nikhilnishad.househunt.model.PropertyDetails;

@Service
public class NotifierService {

	Logger log = LoggerFactory.getLogger(NotifierService.class);

	@Autowired
	WebDriver webDriverInstance;
	
	@Autowired
    private EmailService emailService;

	@Scheduled(fixedDelay = 60000)
	public void scheduleFixedDelayTask() {
		checkNewProperty();
	}

	private void checkNewProperty() {
		try {
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
			System.exit(0);
		}
	}
	
    private Set<PropertyDetails> getTrackedVisitedListFromFile(){
        Set<PropertyDetails> trackedPropertyList;
		try{
            FileInputStream fileInputStream
                    = new FileInputStream("visitedList.data");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            trackedPropertyList = (Set<PropertyDetails>) objectInputStream.readObject();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
        	trackedPropertyList=new HashSet<PropertyDetails>();
            updateVisitedList(trackedPropertyList);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
    }
    
	private List<PropertyDetails> createPropertyList(Set<PropertyDetails> trackedVisitedList){
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
    	emailService.sendSimpleMail(
				new EmailDetails(
						"nikhilnishadatuk+autopilot@gmail.com",
						propertyLinks.toString(),
						"New Properties || AutoPilot",
						null)
				);
    }
}
