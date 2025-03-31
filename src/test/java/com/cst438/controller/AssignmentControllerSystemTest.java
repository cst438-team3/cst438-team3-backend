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

public class AssignmentControllerSystemTest {

    // chrome driver
    public static final String CHROME_DRIVER_LOCATION =
            "C:/Users/taybe/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe";

    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second

    WebDriver driver;

    // the following tests assume:
    // 1.  There are course sections in Spring 2025
    // 2. There are no assignments for section 9 cst363

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set required Chrome Driver properties
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // adding short wait to allow page to load
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null){
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestAddAssignment() throws Exception {
        // views sections for spring 2025
        // views assignments for section 9
        // Add a new assignment for section 9
        // verify assigment added
        // delete the added assignment
        // verify assignment is gone

        // enter search terms and click show sections link
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("showSections")).click();
        Thread.sleep(SLEEP_DURATION);

        // view assignments for section 9
        WebElement section9 = driver.findElement(By.xpath("//tr[td='9']"));
        WebElement viewAssignments = section9.findElement(By.id("viewAssignments"));
        viewAssignments.click();
        Thread.sleep(SLEEP_DURATION);

        // if any assignments exists, delete it
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

        // verify assignment added
        // TODO: use unit test to verify assignment added
        WebElement assignment = driver.findElement(By.xpath("//tr[td='Test Assignment 1']"));
        assertNotNull(assignment);
        assertTrue(assignment.isDisplayed(), "Assignment not displayed");

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
