package com.nikhilnishad.househunt;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.bonigarcia.wdm.WebDriverManager;

@EnableScheduling
@SpringBootApplication
public class HousehuntServiceApplication{

    Logger log = LoggerFactory.getLogger(HousehuntServiceApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(HousehuntServiceApplication.class, args);
	}
	
	@Bean
    public WebDriver webDriver() {
		log.info("Getting WebDriver Ready");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        //options.addArguments("--headless");
        String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
        options.addArguments("user-agent="+userAgent);
    	WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(150,TimeUnit.SECONDS);
        log.info("WebDriver Ready");
        return driver;
    }
}
