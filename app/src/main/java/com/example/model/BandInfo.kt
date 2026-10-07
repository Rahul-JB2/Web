package com.example.model

enum class BandCategory(val label: String) {
    SUB_6_MID("Mid-Band (C-Band)"),
    SUB_6_LOW("Low-Band (Indoor Coverage)"),
    MMWAVE("mmWave (Extreme Speed)")
}

data class FiveGBand(
    val band: String, // e.g. "n78"
    val frequency: String, // e.g. "3.5 GHz (3300 - 3800 MHz)"
    val duplex: String, // "TDD" or "FDD"
    val category: BandCategory,
    val description: String,
    val primaryRegions: String,
    val typicalSpeeds: String,
    val commonCarriers: String,
    val indianAllocation: String = "",
    val indianCarriers: List<String> = emptyList() // ["Jio", "Airtel", "Vi", "BSNL"]
)

object BandDatabase {
    val global5gBands = listOf(
        FiveGBand(
            band = "n78",
            frequency = "3.5 GHz (3300 - 3800 MHz)",
            duplex = "TDD",
            category = BandCategory.SUB_6_MID,
            description = "India ka primary aur sabse fast 5G band. Sabhi 22 telecom circles me deployed hai for gigabit speeds.",
            primaryRegions = "Pan-India (All 22 Circles)",
            typicalSpeeds = "400 - 1200+ Mbps",
            commonCarriers = "Jio True 5G, Airtel 5G Plus, Vi 5G, BSNL",
            indianAllocation = "Jio: 130 MHz | Airtel: 100 MHz | Vi: 50 MHz",
            indianCarriers = listOf("Jio", "Airtel", "Vi", "BSNL")
        ),
        FiveGBand(
            band = "n28",
            frequency = "700 MHz (703 - 748 / 758 - 803 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_LOW,
            description = "Jio ka exclusive premium low-band spectrum. Best indoor penetration, thick walls aur rural areas ke liye.",
            primaryRegions = "Pan-India (Exclusive to Jio)",
            typicalSpeeds = "80 - 250 Mbps",
            commonCarriers = "Jio True 5G (Exclusive)",
            indianAllocation = "Jio: 10 MHz Paired (Pan-India)",
            indianCarriers = listOf("Jio")
        ),
        FiveGBand(
            band = "n258",
            frequency = "26 GHz (24.25 - 27.5 GHz)",
            duplex = "TDD",
            category = BandCategory.MMWAVE,
            description = "High-frequency mmWave band for stadiums, tech parks, airports aur dense urban hotspots.",
            primaryRegions = "Metro Cities & Tech Hubs (Delhi, Mumbai, Bengaluru, etc.)",
            typicalSpeeds = "1000 - 3500+ Mbps (Multi-Gigabit)",
            commonCarriers = "Jio True 5G, Airtel 5G Plus, Vi 5G",
            indianAllocation = "Jio: 1000 MHz | Airtel: 800 MHz | Vi: 200-800 MHz",
            indianCarriers = listOf("Jio", "Airtel", "Vi")
        ),
        FiveGBand(
            band = "n8",
            frequency = "900 MHz (880 - 960 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_LOW,
            description = "Airtel ka refarmed low-band spectrum for deep building penetration aur rural 5G coverage.",
            primaryRegions = "Pan-India (Airtel Circles)",
            typicalSpeeds = "50 - 180 Mbps",
            commonCarriers = "Airtel 5G Plus",
            indianAllocation = "Airtel: 900 MHz Refarmed",
            indianCarriers = listOf("Airtel")
        ),
        FiveGBand(
            band = "n3",
            frequency = "1800 MHz (1710 - 1880 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_MID,
            description = "Refarmed 1800 MHz DCS band used for Dynamic Spectrum Sharing (DSS) alongside 4G LTE.",
            primaryRegions = "Pan-India (Airtel & Jio Circles)",
            typicalSpeeds = "100 - 350 Mbps",
            commonCarriers = "Airtel 5G Plus, Jio",
            indianAllocation = "Airtel & Jio Refarmed Layer",
            indianCarriers = listOf("Airtel", "Jio")
        ),
        FiveGBand(
            band = "n1",
            frequency = "2100 MHz (1920 - 2170 MHz)",
            duplex = "FDD",
            category = BandCategory.SUB_6_MID,
            description = "2.1 GHz IMT spectrum refarmed for mid-band capacity boost in urban city centers.",
            primaryRegions = "Metro & Tier-1 Cities",
            typicalSpeeds = "120 - 380 Mbps",
            commonCarriers = "Airtel 5G Plus, Vi",
            indianAllocation = "Airtel & Vi: 2100 MHz Refarmed",
            indianCarriers = listOf("Airtel", "Vi")
        ),
        FiveGBand(
            band = "n40",
            frequency = "2300 MHz (2300 - 2400 MHz)",
            duplex = "TDD",
            category = BandCategory.SUB_6_MID,
            description = "High-capacity TDD band refarmed from TD-LTE for heavy city data traffic.",
            primaryRegions = "Pan-India",
            typicalSpeeds = "150 - 450 Mbps",
            commonCarriers = "Jio True 5G, Airtel 5G Plus",
            indianAllocation = "Jio & Airtel: 2300 MHz Layer",
            indianCarriers = listOf("Jio", "Airtel")
        ),
        FiveGBand(
            band = "n77",
            frequency = "3.7 GHz (3300 - 4200 MHz)",
            duplex = "TDD",
            category = BandCategory.SUB_6_MID,
            description = "Global C-Band compatible with n78 handsets for international roaming.",
            primaryRegions = "Global / India Roaming",
            typicalSpeeds = "350 - 1000+ Mbps",
            commonCarriers = "Jio / Airtel Handset Compatibility",
            indianAllocation = "Supports n78 hardware",
            indianCarriers = listOf("Jio", "Airtel")
        )
    )
}
