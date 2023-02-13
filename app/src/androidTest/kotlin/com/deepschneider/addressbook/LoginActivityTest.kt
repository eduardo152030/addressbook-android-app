package com.deepschneider.addressbook

import android.widget.Button
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.deepschneider.addressbook.activities.LoginActivity
import com.deepschneider.addressbook.activities.OrganizationsActivity
import com.deepschneider.addressbook.utils.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {
    @get:Rule
    public var loginActivityTestRule: ActivityTestRule<LoginActivity> = ActivityTestRule(
        LoginActivity::class.java
    )
    private var loginActivity: LoginActivity? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        loginActivity = loginActivityTestRule.activity
        PreferenceManager.getDefaultSharedPreferences(loginActivity).edit().putBoolean(Constants.SETTINGS_SHOULD_USE_HTTP, true).commit()
        PreferenceManager.getDefaultSharedPreferences(loginActivity).edit().putString(Constants.SETTINGS_SERVER_URL, "192.168.1.210:9000").commit()
        PreferenceManager.getDefaultSharedPreferences(loginActivity).edit().putBoolean(Constants.SETTINGS_SHOW_LOCK_NOTIFICATIONS, false).commit()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        loginActivity = null
    }

    @Test
    fun test() {
        val loginButton: Button = loginActivity!!.findViewById(R.id.login_button)
        assertThat(loginButton.text.toString()).isEqualTo("LOGIN")
        runOnUiThread {
            loginActivity!!.findViewById<TextInputEditText>(R.id.edit_text_login).setText("admin")
            loginActivity!!.findViewById<TextInputEditText>(R.id.edit_text_password).setText("adminPass")
            loginButton.performClick()
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(OrganizationsActivity::class.java.name, null, false)
        val currentActivity = instrumentation.waitForMonitor(monitor)
        assertNotNull(currentActivity)
        openActionBarOverflowOrOptionsMenu(instrumentation.targetContext);
        onView(withText(R.string.action_logout)).perform(click())
    }
}