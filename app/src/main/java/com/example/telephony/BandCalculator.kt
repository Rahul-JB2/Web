package com.example.telephony

object BandCalculator {

    /**
     * Estimates 5G NR band name from NR-ARFCN (3GPP TS 38.104).
     */
    fun getNrBandFromArfcn(arfcn: Int): Pair<String, String> {
        return when (arfcn) {
            in 620000..653333 -> Pair("n78", "3.5 GHz (3300 - 3800 MHz)")
            in 620000..680000 -> Pair("n77", "3.7 GHz (3300 - 4200 MHz)")
            in 499200..537999 -> Pair("n41", "2.5 GHz (2496 - 2690 MHz)")
            in 422000..434000 -> Pair("n1", "2.1 GHz (1920 - 2170 MHz)")
            in 361000..376000 -> Pair("n3", "1.8 GHz (1710 - 1880 MHz)")
            in 173800..178800 -> Pair("n5", "850 MHz (824 - 894 MHz)")
            in 151600..160600 -> Pair("n28", "700 MHz (703 - 803 MHz)")
            in 123400..130400 -> Pair("n71", "600 MHz (617 - 698 MHz)")
            in 158200..164200 -> Pair("n8", "900 MHz (880 - 960 MHz)")
            in 145800..149200 -> Pair("n20", "800 MHz (791 - 862 MHz)")
            in 500000..538000 -> Pair("n38", "2.6 GHz (2570 - 2620 MHz)")
            in 2054166..2104165 -> Pair("n258", "26 GHz mmWave")
            in 2229166..2279165 -> Pair("n260", "39 GHz mmWave")
            in 2070833..2084999 -> Pair("n261", "28 GHz mmWave")
            else -> Pair("NR (Unknown Band)", "ARFCN: $arfcn")
        }
    }

    /**
     * Estimates LTE band name from EARFCN (3GPP TS 36.101).
     */
    fun getLteBandFromEarfcn(earfcn: Int): Pair<String, String> {
        return when (earfcn) {
            in 0..599 -> Pair("Band 1 (B1)", "2100 MHz")
            in 600..1199 -> Pair("Band 2 (B2)", "1900 MHz PCS")
            in 1200..1949 -> Pair("Band 3 (B3)", "1800 MHz DCS")
            in 1950..2399 -> Pair("Band 4 (B4)", "1700/2100 MHz AWS")
            in 2400..2649 -> Pair("Band 5 (B5)", "850 MHz Cellular")
            in 2750..3449 -> Pair("Band 7 (B7)", "2600 MHz IMT-E")
            in 3450..3799 -> Pair("Band 8 (B8)", "900 MHz")
            in 6150..6449 -> Pair("Band 20 (B20)", "800 MHz Digital Dividend")
            in 9210..9659 -> Pair("Band 28 (B28)", "700 MHz APT")
            in 37750..38249 -> Pair("Band 38 (B38)", "2600 MHz TDD")
            in 38650..39649 -> Pair("Band 40 (B40)", "2300 MHz TDD")
            in 39650..41589 -> Pair("Band 41 (B41)", "2500 MHz BRS/EBS")
            in 66436..67335 -> Pair("Band 71 (B71)", "600 MHz US")
            else -> Pair("LTE Band", "EARFCN: $earfcn")
        }
    }
}
