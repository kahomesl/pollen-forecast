package com.kahomesl.allergenradar.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kahomesl.allergenradar.data.LocationDto
import com.kahomesl.allergenradar.data.TaxonAvailabilityDto
import com.kahomesl.allergenradar.data.TaxonAvailabilityStatusDto
import com.kahomesl.allergenradar.ui.theme.AllergenRadarTheme
import com.kahomesl.allergenradar.ui.viewmodel.HomeUiState

@Preview(name = "北京城市级蒿属状态", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun BeijingCityArtemisiaPreview() {
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

@Preview(name = "数据说明", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun DataInformationPreview() {
    AllergenRadarTheme { PremiumDataInfoScreen(onBack = {}) }
}
