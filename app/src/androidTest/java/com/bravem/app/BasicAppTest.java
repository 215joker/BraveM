package com.bravem.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bravem.app.ui.auth.SplashActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Basic instrumentation test to verify the app launches.
 */
@RunWith(AndroidJUnit4.class)
public class BasicAppTest {

    @Rule
    public ActivityScenarioRule<SplashActivity> activityRule =
            new ActivityScenarioRule<>(SplashActivity.class);

    @Test
    public void testAppLaunch() {
        // Verify that some view in the SplashActivity is displayed
        // Since SplashActivity might finish quickly, we just check for its existence
        // Or check for a view in the next activity if it routes immediately
        onView(withId(android.R.id.content)).check(matches(isDisplayed()));
    }
}
