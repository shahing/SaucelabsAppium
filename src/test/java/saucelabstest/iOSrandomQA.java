package saucelabstest;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import com.saucelabs.saucerest.SauceREST;
import io.appium.java_client.AppiumDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AppiumTest implements SauceOnDemandSessionIdProvider {

    private AppiumDriver driver;


    public static WebElement wait(By locator) {
        return Helpers.wait(locator);
    }

    private boolean runOnSauce = System.getProperty("sauce") != null;

    private SauceOnDemandAuthentication auth = new SauceOnDemandAuthentication();

    public
    @Rule
    SauceOnDemandTestWatcher reportToSauce = new SauceOnDemandTestWatcher(this, auth, false);

    private String sessionId;

    private static final Date date = new Date();

    @Rule
    public TestRule printTests = new TestWatcher() {
        protected void starting(Description description) {
            System.out.print("  test: " + description.getMethodName());
        }

        protected void finished(Description description) {
            final String session = getSessionId();

            if (session != null) {
                System.out.println(" " + "https://saucelabs.com/tests/" + session);
            } else {
                System.out.println();
            }
        }
    };

    /**
     * Run before each test *
     */
    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appium-version", "1.1.0");
        capabilities.setCapability("platformVersion", "7.1");
        capabilities.setCapability("platformName", "ios");
        capabilities.setCapability("deviceName", "iPhone Simulator");

        // Set job name on Sauce Labs
        capabilities.setCapability("name", "Java iOS " + date);
        String userDir = System.getProperty("user.dir");
        String localApp = "Random_v8.0_apkpure.com.ipa";
        if (runOnSauce) {
            String user = auth.getUsername();
            String key = auth.getAccessKey();

            // Upload app to Sauce Labs
            SauceREST rest = new SauceREST(user, key);

            rest.uploadFile(new File(userDir, localApp), localApp);

            capabilities.setCapability("app", "sauce-storage:" + localApp);
            URL sauceURL = new URL("http://" + user + ":" + key + "@ondemand.saucelabs.com:80/wd/hub");
            driver = new AppiumDriver(sauceURL, capabilities);
        } else {
            String appPath = Paths.get(userDir, localApp).toAbsolutePath().toString();
            capabilities.setCapability("app", appPath);
            driver = new AppiumDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
        }

        sessionId = driver.getSessionId().toString();

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        Helpers.init(driver);
    }


    @After
    public void tearDown() throws Exception {
        if (driver != null) driver.quit();
    }

    public String getSessionId() {
        return runOnSauce ? sessionId : null;
    }
}
