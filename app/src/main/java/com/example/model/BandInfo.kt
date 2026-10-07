package com.example.model

enum class BandCategory(val label: String) {
    SUB_6_MID("Mid-Band Sub-6 GHz"),
    SUB_6_LOW("Low-Band Sub-1 GHz"),
    MMWAVE("mmWave High-Band")
}

data class FiveGBand(
    val band: String, // e.g. "n78"
    val frequency: String, // e.g. "3.5 GHz (3300 - 3800 MHz)"
    val duplex: String, // "TDD" or "FDD"
    val category: BandCategory,
    val description: String,
    val primaryRegions: String,
    val typicalSpeeds: String,
    val commonCarriers: String
)

object BandDatabase {
    val global5gBands = listOf(
        FiveGBand(
            band = "n78",
            frequency = "3500 MHz (3300 - 3800 MHz)",
            duplex = "TDD",
            category = BandCategory.SUB_6_MID,
            description = "The global gold standard 5G band. Offers incredible capacity, gigabit speeds, and wide city coverage.",
            primaryRegions = "Global (Europe, Asia, India, Australia, LatAm)",
            typicalSpeeds = "300 - 1200+ Mbps",
            commonCarriers = "Jio, Airtel, Vodafone, Orange, Telstra, EE, TIM, Docomo"
        ),
        FiveGBand(
            band = "n77",
            frequency = "3700 MHz (3300 - 4200 MHz)",
            duplex = "TDD",
            category = BandCategory.SUB_6_MID,
            description = "C-Band powerhouse widely deployed in the Americas and Japan. Key for high-speed urban and suburban coverage.",
            primaryRegions = "North America, Japan, Europe",
            typicalSpeeds = "250 - 900+ Mbps",
            commonCarriers = "Verizon 5G Ultra Wideband, AT&T 5G+, NTT Docomo, KDDI"
        ),
        FiveGBand(
            band = "n41",
            frequency = "2500 MHz (2496 - 2690 MHz)",
            duplex = "TDD",
            category = BandCategory.SUB_6_MID,
            description = "Ultra Capacity mid-band deployed extensively. Excellent penetration with sustained 300+ Mbps throughput.",
            primaryRegions = "USA, China, India, Philippines",
            typicalSpeeds = "200 - 800 Mbps",
            commonCarriers = "T-Mobile Ultra Capacity (UC), China Mobile, Smart, Globe"
        ),
        FiveGBand(
            band = "n28",
            frequency = "700 MHz (703 - 748 / 758 - 803 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_LOW,
            description = "Long-range low-band providing deep indoor penetration and vast rural highway coverage.",
            primaryRegions = "Europe, Asia-Pacific, Latin America, India",
            typicalSpeeds = "30 - 150 Mbps",
            commonCarriers = "Jio True 5G (Low-Band), Vodafone Ziggo, Telstra, Movistar"
        ),
        FiveGBand(
            band = "n71",
            frequency = "600 MHz (663 - 698 / 617 - 652 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_LOW,
            description = "Extended Range 5G foundation in the US. Pierces thick building walls and covers vast rural areas.",
            primaryRegions = "USA, Canada",
            typicalSpeeds = "35 - 180 Mbps",
            commonCarriers = "T-Mobile Extended Range 5G, Rogers"
        ),
        FiveGBand(
            band = "n5",
            frequency = "850 MHz (824 - 849 / 869 - 894 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_LOW,
            description = "Sub-1 GHz band repurposed from 3G/LTE for nationwide 5G reach.",
            primaryRegions = "North America, Korea, Australia, India",
            typicalSpeeds = "40 - 160 Mbps",
            commonCarriers = "AT&T 5G, Verizon, SK Telecom, Telstra"
        ),
        FiveGBand(
            band = "n1",
            frequency = "2100 MHz (1920 - 1980 / 2110 - 2170 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_MID,
            description = "Refarmed AWS/IMT band widely used for Dynamic Spectrum Sharing (DSS) alongside 4G.",
            primaryRegions = "Europe, Asia, Latin America",
            typicalSpeeds = "100 - 350 Mbps",
            commonCarriers = "Vodafone, Deutsche Telekom, Swisscom, Airtel"
        ),
        FiveGBand(
            band = "n3",
            frequency = "1800 MHz (1710 - 1785 / 1805 - 1880 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_MID,
            description = "Classic 1800 MHz DCS band refarmed for 5G NSA/SA high-capacity layer.",
            primaryRegions = "Europe, Asia, Middle East, Australia",
            typicalSpeeds = "120 - 400 Mbps",
            commonCarriers = "EE, Optus, Singtel, Vodafone UK, Jio"
        ),
        FiveGBand(
            band = "n258",
            frequency = "26 GHz (24.25 - 27.5 GHz)",
            duplex = "TDD",
            category = BandCategory.MMWAVE,
            description = "High-frequency mmWave band for dense stadiums, airports, and urban hubs. Multi-gigabit speeds.",
            primaryRegions = "Europe, Asia, Australia, India",
            typicalSpeeds = "1000 - 3500+ Mbps",
            commonCarriers = "Telecom Italia, Telstra, Optus, Jio mmWave"
        ),
        FiveGBand(
            band = "n260",
            frequency = "39 GHz (37.0 - 40.0 GHz)",
            duplex = "TDD",
            category = BandCategory.MMWAVE,
            description = "High-band mmWave spectrum in the USA delivering extreme throughput in crowded arenas.",
            primaryRegions = "USA, Japan",
            typicalSpeeds = "1200 - 4000+ Mbps",
            commonCarriers = "Verizon 5G Ultra Wideband mmWave, AT&T 5G+"
        ),
        FiveGBand(
            band = "n261",
            frequency = "28 GHz (27.5 - 28.35 GHz)",
            duplex = "TDD",
            category = BandCategory.MMWAVE,
            description = "Original 28 GHz mmWave band used for blistering gigabit line-of-sight connections.",
            primaryRegions = "USA, Korea, Japan",
            typicalSpeeds = "1500 - 4200+ Mbps",
            commonCarriers = "Verizon Ultra Wideband, KT, NTT Docomo, T-Mobile mmWave"
        )
    )
}
