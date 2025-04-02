package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StudentScheduleControllerSystemTest {

    // chrome driver
    public static final String CHROME_DRIVER_LOCATION =
            "C:/Users/taybe/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestEnrollSection () throws Exception {
        // view available sections to enroll in
        // enroll in section 10 software engineering
        // view student schedule and verify section 10 is in the schedule
        // disenroll from added section and verify disenrollment

        // check if enrolled in section 10 for 2024 Fall
        driver.findElement(By.id("schedule")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("load")).click();
        Thread.sleep(SLEEP_DURATION);

        // if enrolled in section 10, disenroll
        try{
            WebElement sec10 = driver.findElement(By.xpath("//tr[td='10']"));
            sec10.findElement(By.id("drop")).click();
            Thread.sleep(SLEEP_DURATION);
        } catch (NoSuchElementException e){
            // proceed
        }

        // view available sections to enroll in
        driver.findElement(By.id("enroll")).click();
        Thread.sleep(SLEEP_DURATION);

        // enroll in section 10 software engineering
        WebElement section5 = driver.findElement(By.xpath("//tr[td='10']"));
        section5.findElement(By.id("enroll")).click();
        Thread.sleep(SLEEP_DURATION);

        //verify enrollment message
        WebElement message = driver.findElement(By.id("message"));
        assertEquals("Successfully enrolled in section 10", message.getText());

        // view student schedule and verify section 10 is in the schedule
        driver.findElement(By.id("schedule")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("load")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement section5Schedule = driver.findElement(By.xpath("//tr[td='10']"));
        assertEquals("Software Engineering", section5Schedule.findElement(By.id("title")).getText());
        assertEquals("10", section5Schedule.findElement(By.id("secNo")).getText());

        // disenroll from added section and verify disenrollment
        WebElement sec10 = driver.findElement(By.xpath("//tr[td='10']"));
        sec10.findElement(By.id("drop")).click();
        Thread.sleep(SLEEP_DURATION);

        // verify disenrollment
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.xpath("//tr[td='10']"));
        });
    }
}
