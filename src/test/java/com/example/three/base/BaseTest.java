package com.example.three.base;


import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;

/**
 * Base test class for all API tests
 */
public abstract class BaseTest {

	private static final ThreadLocal<RequestSpecification> requestSpecThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<SoftAssert> softAssertThreadLocal = new ThreadLocal<>();

	protected SoftAssert getSoftAssert() {
		return softAssertThreadLocal.get();
	}

	protected void assertAll() {
		getSoftAssert().assertAll();
	}

	protected RequestSpecification getRequestSpec() {
		return requestSpecThreadLocal.get();
	}

	@BeforeMethod // Runs before EVERY test method
	public void setup() {
		softAssertThreadLocal.set(new SoftAssert());
		RequestSpecBuilder builder = new RequestSpecBuilder();
		requestSpecThreadLocal.set(builder.build());
	}

	@AfterMethod // Runs after EVERY test method (cleans up ThreadLocal)
	public void tearDown() {
		requestSpecThreadLocal.remove();
		softAssertThreadLocal.remove();
	}
}