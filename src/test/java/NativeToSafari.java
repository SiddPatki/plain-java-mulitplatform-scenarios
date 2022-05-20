import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.TAB;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class NativeToSafari {

    private static final String USERNAME = "BROWSERSTACK_USERNAME";
    private static final String ACCESS_KEY = "BROWSERSTACK_ACCESS_KEY";
    private static final String URL = "https://hub-cloud.browserstack.com/wd/hub";


    public static void main(String[] args) throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();


        caps.setCapability("device", "iPhone 11 Pro Max");
        caps.setCapability("os_version", "13");
        caps.setCapability("name", "App To Safari");
        caps.setCapability("build", "Switching Scenario");
        caps.setCapability("project", "Java MultiPlatform");
        caps.setCapability("browserstack.debug", "true");
        caps.setCapability("browserstack.networkLogs", "true");
        caps.setCapability("includeSafariInWebviews", "true"); //Safari will not provide its WebView context in the set of context handles received by default. In order to get the WebView context, you would need to set the includeSafariInWebviews capability as true.
        caps.setCapability("fullContextList", "true"); //The WebView context provided for iOS apps are generic. An example of a WebView context would be WEBVIEW_76851.1 . To have the correct context, you would need to get additional details from every context that is available. To get these details, you would need to set the fullContextList capability as true.
/*      Command to upload the sample app used in this test.
        curl -u "YOUR_USERNAME:YOUR_ACCESS_KEY" \
        -X POST "https://api-cloud.browserstack.com/app-automate/upload" \
        -F "url=https://www.browserstack.com/app-automate/sample-apps/ios/BStackSampleApp.ipa" \
        -F "custom_id=iOSDemoApp"
*/

        caps.setCapability("app", "iOSDemoApp");


        IOSDriver<MobileElement> driver = new IOSDriver<MobileElement>(new URL("https://"+USERNAME+":"+ACCESS_KEY+"@hub-cloud.browserstack.com/wd/hub"), caps);

        Wait<IOSDriver<MobileElement>> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(10))
                .ignoring(NotFoundException.class);

        driver.findElementByAccessibilityId("Text Button").click();
        driver.findElementByAccessibilityId("Text Input").click();
        driver.findElementByAccessibilityId("Text Input").sendKeys("Welcome to BrowserStack" + ENTER);

//        You can open the Safari Mobile App by using the below code. The activateApp method just requires the Bundle ID of the App which needs to be launched.


        driver.activateApp("com.apple.mobilesafari");

//        After switching to the Safari Mobile App, you would at the moment only be able to interact only with the Native elements of the app. In order to use the web aspects of the mobile browser, you would need to change the context of the driver to WebView.
//        To have the correct context, you would need to poll for certain duration check if more than one contexts are available. Once that’s true, you can select the context which has the Safari’s bundle ID.
        wait.until(d -> d.getContextHandles().size() > 1);
        for (Object context : driver.getContextHandles()) {
            Map<String, String> contextMap = (Map<String, String>) context;
            if (contextMap.getOrDefault("bundleId", "").equals("com.apple.mobilesafari")) {
                driver.context(contextMap.get("id"));
            }
        }

        driver.get("https://bstackdemo.com");
        System.out.println(driver.getContextHandles());
        wait.until(elementToBeClickable(By.id("signin"))).click();
        wait.until(elementToBeClickable(By.cssSelector("#username input"))).sendKeys("fav_user" + TAB);
        driver.findElement(By.cssSelector("#password input")).sendKeys("testingisfun99" + TAB);
        driver.findElement(By.id("login-btn")).click();
        String username = wait.until(presenceOfElementLocated(By.className("username"))).getText();

//        To switch back to the iOS Sample App, you would need to use the activateApp method and provide the Bundle ID for the App. Now in the iOS Sample App, in order to use the App’s Native elements to perform actions, you would need to switch the context back to NATIVE_APP.
        driver.activateApp("com.browserstack.Sample-iOS");
        driver.context("NATIVE_APP");

        driver.findElementByAccessibilityId("UI Elements").click();
        wait.until(d -> d.findElementByAccessibilityId("Text Button").isEnabled());
        driver.findElementByAccessibilityId("Text Button").click();
        driver.findElementByAccessibilityId("Text Input").click();
        driver.findElementByAccessibilityId("Text Input").sendKeys("Welcome to App-Automate" + Keys.ENTER);

//        https://www.browserstack.com/docs/app-automate/appium/set-up-tests/mark-tests-as-pass-fail
        if (username.equalsIgnoreCase("fav_user")) {
            driver.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"passed\"}}");
        } else {
            driver.executeScript("browserstack_executor: {\"action\": \"setSessionStatus\", \"arguments\": {\"status\": \"failed\"}}");
        }
        driver.quit();
    }
}
