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

import static org.junit.jupiter.api.Assertions.*;


public class GradeControllerSystemTest {

    // chrome driver
    public static final String CHROME_DRIVER_LOCATION =
            "C:/Users/taybe/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second

    WebDriver driver;

    // the following tests assume:
    // 1.  There are course sections in Spring 2025
    // 2. There are no assignments for section 10 cst363

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
    public void systemTestGradeAssignment() throws Exception {
        // views sections for spring 2025
        // views assignments for section 10
        // Add a new assignment for section 10
        // Add a grade for assignment for student 'thomas edison' of 90
        // verify score for assignment is saved - "scored saved" message displayed, score text = 90

        // enter search terms and click show sections link
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("showSections")).click();
        Thread.sleep(SLEEP_DURATION);

        // view assignments for section 10
        WebElement section9 = driver.findElement(By.xpath("//tr[td='10']"));
        WebElement viewAssignments = section9.findElement(By.id("viewAssignments"));
        viewAssignments.click();
        Thread.sleep(SLEEP_DURATION);

        // if any assignments exists, delete them
        try {
            while(true){
                WebElement deleteButton = driver.findElement(By.id("delete"));
                deleteButton.click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e){
            // continue with test
        }

        // add a new assignment
        driver.findElement(By.id("addAssignment")).click();
        Thread.sleep(SLEEP_DURATION);

        // enter assignment data
        driver.findElement(By.id("title")).sendKeys("Test Assignment 1");
        driver.findElement(By.id("dueDate")).sendKeys("2025-04-01");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        // add a grade for assignment for student Thomas Edison
        driver.findElement(By.id("grade")).click();
        Thread.sleep(SLEEP_DURATION);

        // enter assignment score
        driver.findElement(By.id("score")).sendKeys("90");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        // verify score for assignment is saved
        WebElement message = driver.findElement(By.id("message"));
        assertEquals("score saved", message.getText());
        assertEquals("90", driver.findElement(By.id("score")).getAttribute("value"));

        // navigate back to assignment view
        driver.navigate().back();
        Thread.sleep(SLEEP_DURATION);

        // delete the added assignment
        WebElement deleteButton = driver.findElement(By.id("delete"));
        deleteButton.click();
        Thread.sleep(SLEEP_DURATION);

        // verify assignment is gone
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.xpath("//tr[td='Test Assignment 1']"));
        });
    }
}
