package com.vijay.cardkeeper.util

object LogoUtils {


        /** Returns a Clearbit Logo URL for a bank institution. */
        fun getInstitutionLogoUrl(name: String?): String? {
                if (name == null) return null
                val domain =
                        when {
                                name.contains("Chase", ignoreCase = true) -> "chase.com"
                                name.contains("Citi", ignoreCase = true) -> "citi.com"
                                name.contains("Wells", ignoreCase = true) -> "wellsfargo.com"
                                name.contains("Bank of America", ignoreCase = true) ||
                                        name.contains("Boa", ignoreCase = true) ->
                                        "bankofamerica.com"
                                name.contains("Capital One", ignoreCase = true) -> "capitalone.com"
                                name.contains("American Express", ignoreCase = true) ->
                                        "americanexpress.com"
                                name.contains("Discover", ignoreCase = true) -> "discover.com"
                                name.contains("HDFC", ignoreCase = true) -> "hdfcbank.com"
                                name.contains("ICICI", ignoreCase = true) -> "icicibank.com"
                                name.contains("SBI", ignoreCase = true) ||
                                        name.contains("State Bank", ignoreCase = true) ->
                                        "sbi.co.in"
                                name.contains("Barclays", ignoreCase = true) -> "barclays.com"
                                name.contains("HSBC", ignoreCase = true) -> "hsbc.com"
                                else -> {
                                        // Heuristic: remove spaces and append .com (only for common
                                        // US/global
                                        // banks)
                                        // Risk: could be wrong. For now, let's just return null if
                                        // not matched for
                                        // safety
                                        // or try a fallback if name is just one word.
                                        null
                                }
                        }
                return domain?.let { "https://img.logo.dev/$it" }
        }

        /** Returns a primary brand color for a bank or card network. */
        fun getBrandColor(name: String?, network: String?): Long? {
                // 1. Check Bank Name first
                if (name != null) {
                        val bank = name.lowercase()
                        val color =
                                when {
                                        bank.contains("chase") -> 0xFF117ACA // Chase Blue
                                        bank.contains("citibank") || bank.contains("citi") ->
                                                0xFF003B70 // Citi Blue
                                        bank.contains("wells fargo") || bank.contains("wells") ->
                                                0xFFD41C2C // Wells Fargo Red
                                        bank.contains("bank of america") || bank.contains("boa") ->
                                                0xFF012169 // BoA Dark Blue
                                        bank.contains("capital one") ->
                                                0xFF004977 // Capital One Blue
                                        bank.contains("american express") ||
                                                bank.contains("amex") -> 0xFF006FCF // Amex Blue
                                        bank.contains("discover") -> 0xFFE55C20 // Discover Orange
                                        bank.contains("hsbc") -> 0xFFDB0011 // HSBC Red
                                        bank.contains("barclays") ->
                                                0xFF00AEEF // Barclays Light Blue
                                        bank.contains("standard chartered") ->
                                                0xFF0473EA // Standard Chartered Blue
                                        bank.contains("pnc") -> 0xFFF58025 // PNC Orange
                                        bank.contains("us bank") -> 0xFF001E79 // US Bank Blue
                                        bank.contains("td bank") -> 0xFF008A00 // TD Green
                                        bank.contains("truist") -> 0xFF2B0A3D // Truist Purple
                                        bank.contains("goldman sachs") || bank.contains("marcus") ->
                                                0xFF64A8F0 // Goldman Sachs Blue
                                        bank.contains("morgan stanley") -> 0xFF002855 // MS Navy
                                        bank.contains("ally") -> 0xFF500078 // Ally Purple
                                        bank.contains("hdfc") -> 0xFF004C8F // HDFC Blue
                                        bank.contains("icici") -> 0xFFF06321 // ICICI Orange
                                        bank.contains("sbi") || bank.contains("state bank") ->
                                                0xFF00B5EF // SBI Cyan
                                        bank.contains("axis") -> 0xFF861F41 // Axis Burgundy
                                        bank.contains("kotak") -> 0xFF003874 // Kotak Blue
                                        bank.contains("yes bank") -> 0xFF0054A6 // Yes Bank Blue
                                        bank.contains("indusind") -> 0xFF91191E // IndusInd Maroon
                                        bank.contains("bank of baroda") -> 0xFFF06522 // BoB Orange
                                        bank.contains("canara") -> 0xFF0091CF // Canara Blue
                                        bank.contains("union bank") -> 0xFFD71920 // Union Bank Red
                                        bank.contains("idfc") -> 0xFF91191E // IDFC Maroon
                                        else -> null
                                }
                        if (color != null) return color
                }

                // 2. Check Card Network
                if (network != null) {
                        val net = network.lowercase()
                        return when {
                                net.contains("visa") -> 0xFF1A1F71 // Visa Blue
                                net.contains("master") -> 0xFF222222 // Mastercard Black/Dark
                                net.contains("discover") -> 0xFFE55C20 // Discover Orange
                                net.contains("amex") -> 0xFF006FCF // Amex Blue
                                net.contains("rupay") -> 0xFF1B3F6B // Rupay Blue
                                else -> null
                        }
                }

                return null
        }

        fun getCardNetworkLogoUrl(network: String?): String? {
            // Currently returning null to fallback to local resources
            return null
        }
}
