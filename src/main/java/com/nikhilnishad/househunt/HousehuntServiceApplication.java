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
    private Set<String> visitedLinks;
	
	public static void main(String[] args) {
		SpringApplication.run(HousehuntServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		setUp();
		log.debug("web driver setup-complete");
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
			log.debug("revisit list loaded from file");
			List<String> propertiesLinks=propertyList();
			log.debug("new property list ready");
			List<PropertyDetails> listOfpropertyDetails=createPropertiesDataToEmail(propertiesLinks);
			log.debug("property profiles ready");
			if(listOfpropertyDetails.size()>0) {
				sendEmails(listOfpropertyDetails);
				log.debug("email sent");
				visitedLinks.addAll(propertiesLinks);
			    updateVisitedList();
				log.debug("visited properties added to file");
			}
		} catch (Exception e) {
			driver.close();
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	public void setUp(){
        System.out.println("Web driver Getting Ready");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--headless");
        options.addArguments("start-maximized");
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");
        String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
        options.addArguments("user-agent="+userAgent);
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(120,TimeUnit.SECONDS);
        System.out.println("Web driver ready");
    }

    public void readVisitedList(){
        try{
            FileInputStream fileInputStream
                    = new FileInputStream("visitedList.data");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            visitedLinks = (Set<String>) objectInputStream.readObject();
            objectInputStream.close();
            driver.manage().timeouts().implicitlyWait(120,TimeUnit.SECONDS);
        } catch (FileNotFoundException e) {
            try {
                FileOutputStream fileOutputStream
                        = new FileOutputStream("visitedList.data");
                ObjectOutputStream objectOutputStream
                        = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(new HashSet<String>());
                objectOutputStream.flush();
                objectOutputStream.close();
                readVisitedList();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<String> propertyList(){
        List<WebElement> propertyLinks = driver.findElements(By.xpath("//a[@class='propertyCard-link property-card-updates']"));
        List<String> propertiesLinkToVisit=new ArrayList<>();
        propertyLinks.forEach( property->{
            if(visitedLinks==null || !visitedLinks.contains(property.getAttribute("href"))){
                propertiesLinkToVisit.add(property.getAttribute("href"));
            }

        });
        return propertiesLinkToVisit;
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

	private List<PropertyDetails> createPropertiesDataToEmail(List<String> propertiesLinks) {
		List<PropertyDetails> listOfpropertyDetails=new ArrayList<>();
		propertiesLinks.forEach(propertyUrl->{
			try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
			driver.get(propertyUrl);
			try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
			WebElement address = driver.findElement(By.xpath("/html/body/div[4]/main/div/div[2]/div/div[1]/div[1]/div/h1"));
			WebElement price=driver.findElement(By.xpath("/html/body/div[4]/main/div/div[2]/div/article[1]/div/div/div[1]/span"));
			WebElement phone=driver.findElement(By.xpath("/html/body/div[4]/main/div/div[2]/aside/div/div/div[1]/div[1]/div[2]/div/div/a"));
			WebElement contactForm=driver.findElement(By.xpath("/html/body/div[4]/main/div/div[2]/aside/div/div/div[1]/div[1]/div[2]/a"));
			
			PropertyDetails property=new PropertyDetails();
			property.setAddress(address.getText());
			property.setPrice(address.getText());
			property.setPhoneNumber(phone.getText().split(":")[1]);
			property.setContactFormLink(contactForm.getAttribute("href"));
			property.setPropertyUrl(propertyUrl);
			listOfpropertyDetails.add(property);
		});
		return listOfpropertyDetails;
	}

}
