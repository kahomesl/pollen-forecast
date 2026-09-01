package com.kahomesl.allergenradar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
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

        composeRule.onNodeWithText("搜索城市或区县").assertIsDisplayed()
    }

    @Test
    fun historyNavigationShowsAllTaxaFilterWithoutNetworkData() {
        composeRule.onNodeWithText("历史").performClick()
        composeRule.onAllNodesWithText("全部").assertCountEquals(2)
        composeRule.onNodeWithText("蒿属").assertIsDisplayed()
    }

    @Test
    fun dataExplanationReturnsToMyOnSystemBack() {
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("数据说明").performClick()
        composeRule.onNodeWithText("综合花粉 ≠ 蒿属").assertIsDisplayed()

        composeRule.activity.onBackPressedDispatcher.onBackPressed()

        composeRule.onAllNodesWithText("综合花粉 ≠ 蒿属").assertCountEquals(0)
        composeRule.onNodeWithText("最近同步").assertIsDisplayed()
    }
}
