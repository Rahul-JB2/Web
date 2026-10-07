package com.example.launcher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

sealed class LaunchResult {
    data class Success(val methodUsed: String) : LaunchResult()
    data class Failure(val message: String, val suggestedFallbackCode: String = "*#*#4636#*#*") : LaunchResult()
}

object Force5gLauncher {

    /**
     * Directly launches the "Phone info" (RadioInfo) system screen
     * WITHOUT opening the dialer keypad.
     * Iterates through multiple direct activity & component configurations.
     */
    fun openRadioInfo(context: Context): LaunchResult {
        // List of all direct intents to open the "Phone info" screen directly without dialer
        val directIntents = listOf(
            // 1. Direct Component: com.android.settings/.RadioInfo (No action)
            Intent().apply {
                component = ComponentName("com.android.settings", "com.android.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },

            // 2. Action MAIN with RadioInfo
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.android.settings", "com.android.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 3. ClassName direct invocation
            Intent().apply {
                setClassName("com.android.settings", "com.android.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 4. Action VIEW with RadioInfo
            Intent(Intent.ACTION_VIEW).apply {
                component = ComponentName("com.android.settings", "com.android.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 5. Inner RadioInfoActivity alias (MIUI / ColorOS / Realme)
            Intent().apply {
                setClassName("com.android.settings", "com.android.settings.Settings\$RadioInfoActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 6. Direct TestingSettings (Displays Phone info as item 1)
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.android.settings", "com.android.settings.TestingSettings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 7. TestingSettings alias
            Intent().apply {
                setClassName("com.android.settings", "com.android.settings.Settings\$TestingSettingsActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 8. com.android.phone package RadioInfo
            Intent().apply {
                setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 9. com.android.phone RadioInfo direct
            Intent().apply {
                setClassName("com.android.phone", "com.android.phone.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 10. Subpackage radio.RadioInfo (AOSP & Motorola)
            Intent().apply {
                setClassName("com.android.settings", "com.android.settings.radio.RadioInfo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 11. Realme / Oppo / OnePlus Telephony Test in EngineerMode
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.oplus.engineermode", "com.oplus.engineermode.manualtest.TelephonyTest")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 12. Realme / Oppo / OnePlus Manual Test
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.oplus.engineermode", "com.oplus.engineermode.manualtest.ManualTest")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 13. ColorOS EngineerMode
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.coloros.engineermode", "com.coloros.engineermode.EngineerMode")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },

            // 14. Samsung Service Mode Band Selection
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.samsung.android.app.telephonyui", "com.samsung.android.app.telephonyui.hiddennetworksetting.MainActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )

        for (intent in directIntents) {
            if (safeStart(context, intent)) {
                return LaunchResult.Success("Phone Info Direct Settings")
            }
        }

        // Method 15: Direct Telephony Secret Code Broadcast (Doesn't open keypad!)
        try {
            val secretBroadcast = Intent("android.provider.Telephony.SECRET_CODE").apply {
                data = Uri.parse("android_secret_code://4636")
                flags = Intent.FLAG_RECEIVER_FOREGROUND
            }
            context.sendBroadcast(secretBroadcast)
        } catch (_: Exception) {}

        // Fallback: Open system mobile network settings directly
        val settingsIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (safeStart(context, settingsIntent)) {
            return LaunchResult.Success("Mobile Network Settings")
        }

        val roamingSettings = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (safeStart(context, roamingSettings)) {
            return LaunchResult.Success("Network Operator Settings")
        }

        return LaunchResult.Failure(
            message = "Settings locked by system security.",
            suggestedFallbackCode = "*#*#4636#*#*"
        )
    }

    /**
     * Samsung Band Selection tool.
     */
    fun openSamsungBandSelection(context: Context): LaunchResult {
        val samsungIntents = listOf(
            ComponentName("com.samsung.android.app.telephonyui", "com.samsung.android.app.telephonyui.hiddennetworksetting.MainActivity"),
            ComponentName("com.samsung.android.app.telephonyui", "com.samsung.android.app.telephonyui.HiddenNetworkSettingActivity"),
            ComponentName("com.sec.android.app.servicemodeapp", "com.sec.android.app.servicemodeapp.SecServiceModeApp")
        )

        for (comp in samsungIntents) {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                component = comp
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (safeStart(context, intent)) {
                return LaunchResult.Success("Samsung Band Selection")
            }
        }

        return LaunchResult.Failure("Samsung menu restricted.")
    }

    /**
     * Opens official Network & Mobile Internet Settings.
     */
    fun openMobileNetworkSettings(context: Context): LaunchResult {
        val intents = listOf(
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
        )

        for (intent in intents) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (safeStart(context, intent)) {
                return LaunchResult.Success("Mobile Network Settings")
            }
        }

        return LaunchResult.Failure("Could not open system settings.")
    }

    /**
     * Optional dialer trigger only when requested explicitly.
     */
    fun openDialerWithCode(context: Context, code: String): Boolean {
        return try {
            val encodedUri = Uri.parse("tel:" + Uri.encode(code))
            val dialIntent = Intent(Intent.ACTION_DIAL, encodedUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
            true
        } catch (_: Exception) {
            copyToClipboard(context, code)
            false
        }
    }

    /**
     * Copies code to clipboard and shows toast.
     */
    fun copyToClipboard(context: Context, code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("5G Secret Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $code!", Toast.LENGTH_SHORT).show()
    }

    private fun safeStart(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
