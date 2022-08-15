package com.nikhilnishad.househunt.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nikhilnishad.househunt.model.LocationEmailMapping;
import com.nikhilnishad.househunt.model.RequestAddUser;
import com.nikhilnishad.househunt.service.ConfigService;
import com.nikhilnishad.househunt.service.NotifierService;

import io.github.bonigarcia.wdm.WebDriverManager;

@RestController
public class ConfigController {
	
	Logger log = LoggerFactory.getLogger(ConfigController.class);

	@Autowired
	private ConfigService configService;
	
	WebDriver webDriverInstance;
	
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
	
	@PostMapping("/userWithLocationPref")
	public ResponseEntity<String> addUserToLocation(@Valid @RequestBody RequestAddUser inputRequest){
		String location=inputRequest.getLocationPref().getLocationIdentifier();
		try {
			webDriverInstance=webDriver();
			webDriverInstance.get("https://www.rightmove.co.uk/property-to-rent/find.html?locationIdentifier="+location);
			log.info("web driver navigated for location");
			webDriverInstance.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
			Thread.sleep(1000);
			WebElement error = webDriverInstance.findElement(By.className("l-errorCard"));
			if(error.isDisplayed()) {
				webDriverInstance.close();
				webDriverInstance.quit();
				return ResponseEntity.badRequest().body("Invalid LocationIdentifier");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			webDriverInstance.close();
			webDriverInstance.quit();
		}
		String response=configService.addUserToLocation(inputRequest);
		if(response!=null) {
			NotifierService.isDbUpdateFlag=true;
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.badRequest().body(null);
	}
	
	@GetMapping("/userLocationPrefs/{email}")
	public ResponseEntity<List<LocationEmailMapping>> getUserLocationPrefs(@PathVariable String email){
		List<LocationEmailMapping> response=configService.getUserLocationPrefs(email);
		if(response!=null) {
			return ResponseEntity.ok(response);
		}
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/userLocationMapping")
	public ResponseEntity<String> removeUserLocationMapping(@Valid @RequestBody RequestAddUser inputRequest){
		String response = configService.removeUserLocationMapping(inputRequest);
		NotifierService.isDbUpdateFlag=true;
		return ResponseEntity.ok(response);
	}
}
