package com.kahomesl.allergenradar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.TaxonAvailabilityDto
import com.kahomesl.allergenradar.data.TaxonAvailabilityStatusDto
import com.kahomesl.allergenradar.ui.screens.PremiumHomeScreen
import com.kahomesl.allergenradar.ui.theme.AllergenRadarTheme
import com.kahomesl.allergenradar.ui.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun beijingCityRequiresDistrictSelectionForArtemisia() {
        composeRule.setContent {
            AllergenRadarTheme {
                PremiumHomeScreen(
                    state = HomeUiState(
                        locationName = "北京",
                        location = LocationDto(
                            id = "cn-city-beijing",
                            nameCn = "北京",
                            scope = "CITY",
                            taxonAvailability = listOf(
                                TaxonAvailabilityDto(
                                    taxonCode = "ARTEMISIA",
                                    status = TaxonAvailabilityStatusDto.CHILD_LOCATION_REQUIRED,
                                    childScope = "DISTRICT",
                                    childLocationLabel = "北京区县",
                                ),
                            ),
                        ),
                        isLoading = false,
                    ),
                    onRefresh = {},
                    onOpenLocations = {},
                    onOpenDataInfo = {},
                )
            }
        }

        composeRule.onNodeWithText("蒿属数据按北京区县提供").assertIsDisplayed()
        composeRule.onNodeWithText("选择北京区县").assertIsDisplayed()
    }
}
