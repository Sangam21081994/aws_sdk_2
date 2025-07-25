package com.example.runners;

import cucumber.api.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.steps"},
        plugin = {"pretty", "html:target/cucumber-reports"},
        monochrome = true
)
public class CucumberTestRunner {
    // This class should be empty
    // It's just used to run the Cucumber tests
}