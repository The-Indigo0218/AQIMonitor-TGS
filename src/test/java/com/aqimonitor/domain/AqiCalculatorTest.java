package com.aqimonitor.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AQI Calculator Tests")
class AqiCalculatorTest {

    @Nested
    @DisplayName("PM2.5 Breakpoint Tests")
    class Pm25Tests {

        @ParameterizedTest(name = "PM2.5 {0} µg/m³ -> AQI {1}")
        @CsvSource({
            "0.0, 0",
            "6.0, 25",
            "12.0, 50",
            "12.1, 51",
            "23.7, 75",
            "35.4, 100",
            "35.5, 101",
            "45.4, 125",
            "55.4, 150",
            "55.5, 151",
            "102.9, 175",
            "150.4, 200",
            "150.5, 201",
            "200.4, 250",
            "250.4, 300",
            "250.5, 301",
            "375.4, 400",
            "500.4, 500"
        })
        void testPm25Breakpoints(double concentration, int expectedAqi) {
            int aqi = AqiCalculator.calculateAqi(Pollutant.PM25, concentration);
            assertEquals(expectedAqi, aqi, "PM2.5 concentration " + concentration + " should yield AQI " + expectedAqi);
        }
    }

    @Nested
    @DisplayName("PM10 Breakpoint Tests")
    class Pm10Tests {

        @ParameterizedTest(name = "PM10 {0} µg/m³ -> AQI {1}")
        @CsvSource({
            "0, 0",
            "27, 25",
            "54, 50",
            "55, 51",
            "104, 75",
            "154, 100",
            "155, 101",
            "204, 125",
            "254, 150",
            "255, 151",
            "304, 175",
            "354, 200",
            "355, 201",
            "389, 250",
            "424, 300",
            "425, 301",
            "514, 400",
            "604, 500"
        })
        void testPm10Breakpoints(double concentration, int expectedAqi) {
            int aqi = AqiCalculator.calculateAqi(Pollutant.PM10, concentration);
            assertEquals(expectedAqi, aqi, "PM10 concentration " + concentration + " should yield AQI " + expectedAqi);
        }
    }

    @Nested
    @DisplayName("NO2 Breakpoint Tests")
    class No2Tests {

        @ParameterizedTest(name = "NO2 {0} ppb -> AQI {1}")
        @CsvSource({
            "0, 0",
            "26, 25",
            "53, 50",
            "54, 51",
            "77, 76",
            "100, 100",
            "101, 101",
            "230, 125",
            "360, 150",
            "361, 151",
            "505, 176",
            "649, 200",
            "650, 201",
            "949, 250",
            "1249, 300",
            "1250, 301",
            "1649, 400",
            "2049, 500"
        })
        void testNo2Breakpoints(double concentration, int expectedAqi) {
            int aqi = AqiCalculator.calculateAqi(Pollutant.NO2, concentration);
            assertEquals(expectedAqi, aqi, "NO2 concentration " + concentration + " should yield AQI " + expectedAqi);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCases {

        @Test
        @DisplayName("Negative concentration should throw exception")
        void testNegativeConcentration() {
            assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.calculateAqi(Pollutant.PM25, -1.0));
        }

        @Test
        @DisplayName("Concentration above max breakpoint should throw exception")
        void testAboveMaxBreakpoint() {
            assertThrows(IllegalArgumentException.class, () ->
                AqiCalculator.calculateAqi(Pollutant.PM25, 501.0));
        }

        @Test
        @DisplayName("Overall AQI should return maximum of individual AQIs")
        void testOverallAqiReturnsMax() {
            var concentrations = new java.util.EnumMap<Pollutant, Double>(Pollutant.class);
            concentrations.put(Pollutant.PM25, 35.4);  // AQI 100
            concentrations.put(Pollutant.PM10, 154.0);   // AQI 100
            concentrations.put(Pollutant.NO2, 360.0);    // AQI 150

            int overallAqi = AqiCalculator.calculateOverallAqi(concentrations);
            assertEquals(150, overallAqi);
        }

        @Test
        @DisplayName("Empty concentrations map should return 0")
        void testEmptyConcentrations() {
            var concentrations = new java.util.EnumMap<Pollutant, Double>(Pollutant.class);
            assertEquals(0, AqiCalculator.calculateOverallAqi(concentrations));
        }
    }

    @Nested
    @DisplayName("Category Classification Tests")
    class CategoryTests {

        @ParameterizedTest(name = "AQI {0} -> {1}")
        @CsvSource({
            "0, GOOD",
            "25, GOOD",
            "50, GOOD",
            "51, MODERATE",
            "75, MODERATE",
            "100, MODERATE",
            "101, UNHEALTHY_SENSITIVE",
            "125, UNHEALTHY_SENSITIVE",
            "150, UNHEALTHY_SENSITIVE",
            "151, UNHEALTHY",
            "175, UNHEALTHY",
            "200, UNHEALTHY",
            "201, VERY_UNHEALTHY",
            "250, VERY_UNHEALTHY",
            "300, VERY_UNHEALTHY",
            "301, HAZARDOUS",
            "400, HAZARDOUS",
            "500, HAZARDOUS"
        })
        void testCategorization(int aqi, String expectedCategory) {
            AirQualityCategory category = AqiCalculator.categorize(aqi);
            assertEquals(AirQualityCategory.valueOf(expectedCategory), category);
        }

        @Test
        @DisplayName("Categories UNHEALTHY and above should require urgent mitigation")
        void testUrgentMitigationFlag() {
            assertFalse(AirQualityCategory.GOOD.requiresUrgentMitigation());
            assertFalse(AirQualityCategory.MODERATE.requiresUrgentMitigation());
            assertFalse(AirQualityCategory.UNHEALTHY_SENSITIVE.requiresUrgentMitigation());
            assertTrue(AirQualityCategory.UNHEALTHY.requiresUrgentMitigation());
            assertTrue(AirQualityCategory.VERY_UNHEALTHY.requiresUrgentMitigation());
            assertTrue(AirQualityCategory.HAZARDOUS.requiresUrgentMitigation());
        }
    }

    @Nested
    @DisplayName("SensorReading Integration Tests")
    class SensorReadingTests {

        @Test
        @DisplayName("SensorReading should calculate correct overall AQI")
        void testSensorReadingOverallAqi() {
            var reading = SensorReading.builder("TEST-001")
                .concentration(Pollutant.PM25, 35.4)
                .concentration(Pollutant.PM10, 54)
                .concentration(Pollutant.NO2, 100)
                .build();

            assertEquals(100, reading.getOverallAqi());
            assertEquals(AirQualityCategory.MODERATE, reading.getCategory());
        }

        @Test
        @DisplayName("SensorReading should calculate individual pollutant AQI")
        void testSensorReadingIndividualAqi() {
            var reading = SensorReading.builder("TEST-001")
                .concentration(Pollutant.PM25, 55.4)
                .concentration(Pollutant.PM10, 154)
                .concentration(Pollutant.NO2, 360)
                .build();

            assertEquals(150, reading.getAqi(Pollutant.PM25));
            assertEquals(100, reading.getAqi(Pollutant.PM10));
            assertEquals(150, reading.getAqi(Pollutant.NO2));
        }

        @Test
        @DisplayName("Missing pollutant should return 0 AQI")
        void testMissingPollutant() {
            var reading = SensorReading.builder("TEST-001")
                .concentration(Pollutant.PM25, 35.4)
                .build();

            assertEquals(0, reading.getAqi(Pollutant.PM10));
            assertEquals(0, reading.getAqi(Pollutant.NO2));
        }
    }
}