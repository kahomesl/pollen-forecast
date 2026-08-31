package com.kahomesl.allergenradar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivitySmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesWithHomeNavigation() {
        composeRule.onNodeWithText("首页").assertIsDisplayed()
        composeRule.onNodeWithText("过敏原雷达").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationOpensLocationSearch() {
        composeRule.onNodeWithText("位置").performClick()

        composeRule.onNodeWithText("搜索城市或北京区县").assertIsDisplayed()
    }

    @Test
    fun artemisiaNoDataUsesDedicatedEmptyState() {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("暂无蒿属独立数据").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("没有数据不代表风险为零。").assertIsDisplayed()
    }

    @Test
    fun dataExplanationReturnsToMyOnSystemBack() {
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("数据说明").performClick()
        composeRule.onNodeWithText("综合花粉不等于蒿属").assertIsDisplayed()

        composeRule.activity.onBackPressedDispatcher.onBackPressed()

        composeRule.onNodeWithText("我的").assertIsDisplayed()
    }
}
