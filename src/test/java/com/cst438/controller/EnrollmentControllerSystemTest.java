package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentControllerSystemTest {

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
    public void systemTestAddFinalGrade() throws Exception {
        // views sections for spring 2025
        // views enrollments for section 10
        // Add a final grade for section 10, student 'thomas edison' of grade A
        //verify grade saved - 'grade saved' message displayed, grade text = A

        // enter search terms and click show sections link
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("showSections")).click();
        Thread.sleep(SLEEP_DURATION);

        // view enrollments for section 10
        WebElement section9 = driver.findElement(By.xpath("//tr[td='10']"));
        WebElement viewEnrollments = section9.findElement(By.id("viewEnrollments"));
        viewEnrollments.click();
        Thread.sleep(SLEEP_DURATION);

        // if any grade entered, clear it and verify cleared
        if(!driver.findElement(By.id("grade")).getAttribute("value").isEmpty()) {
            driver.findElement(By.id("grade")).clear();
            Thread.sleep(SLEEP_DURATION);
            WebElement grade = driver.findElement(By.id("grade"));
            assertEquals("", grade.getText());
            Thread.sleep(SLEEP_DURATION);
        }

        // enter new grade of A and verify saved
        driver.findElement(By.id("grade")).sendKeys("A");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);
        WebElement message = driver.findElement(By.id("message"));
        assertEquals("grade saved", message.getText());
        assertEquals("A", driver.findElement(By.id("grade")).getAttribute("value"));

        // clear grade entered & verify cleared
        driver.findElement(By.id("grade")).clear();
        Thread.sleep(SLEEP_DURATION);
        assertEquals("", driver.findElement(By.id("grade")).getAttribute("value"));
    }
}
