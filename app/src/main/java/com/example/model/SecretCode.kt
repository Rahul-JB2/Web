package com.example.model

data class SecretDialCode(
    val title: String,
    val code: String,
    val oemBrand: String,
    val description: String,
    val instructions: String,
    val isPrimaryRadioInfo: Boolean = false
)

object SecretCodesDatabase {
    val codes = listOf(
        SecretDialCode(
            title = "Android RadioInfo (Testing Menu)",
            code = "*#*#4636#*#*",
            oemBrand = "Universal / Google / Motorola / Xiaomi",
            description = "The universal Android engineering menu to change 'Set Preferred Network Type' to 'NR only' or 'NR/LTE'.",
            instructions = "Dial in phone app -> Tap 'Phone information' -> Scroll to 'Set Preferred Network Type' -> Select 'NR only' or 'NR/LTE'.",
            isPrimaryRadioInfo = true
        ),
        SecretDialCode(
            title = "Samsung Band Selection Menu",
            code = "*#2263#",
            oemBrand = "Samsung Galaxy",
            description = "Opens Samsung's hidden Band Selection interface to lock specific NR 5G bands (n78, n28, n41, etc.).",
            instructions = "Dial in Samsung Phone keypad -> Select SIM 1 or 2 -> Tap '[1] NR' -> Select specific 5G bands -> Tap 'Apply band configuration'."
        ),
        SecretDialCode(
            title = "Samsung ServiceMode Diagnostics",
            code = "*#0011#",
            oemBrand = "Samsung Galaxy",
            description = "Detailed live radio telemetry, active NR band, RSRP/RSRQ/SINR and carrier aggregation status.",
            instructions = "Dial on Samsung Phone -> Select SIM -> View active NR-ARFCN, Band, PCI, and gNB/eNB tower ID."
        ),
        SecretDialCode(
            title = "Xiaomi 5G Carrier Check Bypass",
            code = "*#*#726633#*#*",
            oemBrand = "Xiaomi / Redmi / POCO",
            description = "Toggles 5G carrier check to force enable 5G Standalone (SA) toggle in MIUI / HyperOS SIM settings.",
            instructions = "Dial code -> Toast appears saying '5G carrier check was disabled' -> Go to Settings > SIM cards > Preferred network type > Select 5G."
        ),
        SecretDialCode(
            title = "Xiaomi VoNR (5G Voice) Enabler",
            code = "*#*#8667#*#*",
            oemBrand = "Xiaomi / Redmi / POCO",
            description = "Enables VoNR (Voice over New Radio) switch in SIM card settings for crystal clear 5G voice calls.",
            instructions = "Dial code -> Check SIM card settings -> Toggle on 'Make calls using VoNR'."
        ),
        SecretDialCode(
            title = "OnePlus / Oppo / Realme Engineer Mode",
            code = "*#800#",
            oemBrand = "OnePlus / Oppo / Realme",
            description = "Opens Feedback / Engineering Mode menu with cellular RF tests and 5G network lock switches.",
            instructions = "Dial code -> Select Telephony / Network -> Set 5G network mode preference to SA+NSA or Force SA."
        ),
        SecretDialCode(
            title = "Vivo / iQOO 5G Engineering Mode",
            code = "*#*#4838#*#*",
            oemBrand = "Vivo / iQOO",
            description = "Vivo's alternative testing menu for accessing phone radio info on FuntouchOS / OriginOS.",
            instructions = "Dial code -> Tap 'Phone information' -> Set preferred network type to 'NR only'."
        ),
        SecretDialCode(
            title = "Huawei / Honor ProjectMenu",
            code = "*#*#2846579#*#*",
            oemBrand = "Huawei / Honor",
            description = "Accesses ProjectMenu for network information queries and background settings.",
            instructions = "Dial in keypad -> Background Settings -> Network Information Query."
        ),
        SecretDialCode(
            title = "Qualcomm Field Test Mode",
            code = "*#*#46360000#*#*",
            oemBrand = "Qualcomm Snapdragon Devices",
            description = "Alternate RadioInfo trigger on certain Snapdragon powered devices with customized ROMs.",
            instructions = "Dial on keypad to invoke hidden Qualcomm diagnostic parameters."
        )
    )
}
