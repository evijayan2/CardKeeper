package com.vijay.cardkeeper.util

object StateUtils {
    private val stateMap =
            mapOf(
                    "ALABAMA" to "AL",
                    "ALASKA" to "AK",
                    "ARIZONA" to "AZ",
                    "ARKANSAS" to "AR",
                    "CALIFORNIA" to "CA",
                    "COLORADO" to "CO",
                    "CONNECTICUT" to "CT",
                    "DELAWARE" to "DE",
                    "FLORIDA" to "FL",
                    "GEORGIA" to "GA",
                    "HAWAII" to "HI",
                    "IDAHO" to "ID",
                    "ILLINOIS" to "IL",
                    "INDIANA" to "IN",
                    "IOWA" to "IA",
                    "KANSAS" to "KS",
                    "KENTUCKY" to "KY",
                    "LOUISIANA" to "LA",
                    "MAINE" to "ME",
                    "MARYLAND" to "MD",
                    "MASSACHUSETTS" to "MA",
                    "MICHIGAN" to "MI",
                    "MINNESOTA" to "MN",
                    "MISSISSIPPI" to "MS",
                    "MISSOURI" to "MO",
                    "MONTANA" to "MT",
                    "NEBRASKA" to "NE",
                    "NEVADA" to "NV",
                    "NEW HAMPSHIRE" to "NH",
                    "NEW JERSEY" to "NJ",
                    "NEW MEXICO" to "NM",
                    "NEW YORK" to "NY",
                    "NORTH CAROLINA" to "NC",
                    "NORTH DAKOTA" to "ND",
                    "OHIO" to "OH",
                    "OKLAHOMA" to "OK",
                    "OREGON" to "OR",
                    "PENNSYLVANIA" to "PA",
                    "RHODE ISLAND" to "RI",
                    "SOUTH CAROLINA" to "SC",
                    "SOUTH DAKOTA" to "SD",
                    "TENNESSEE" to "TN",
                    "TEXAS" to "TX",
                    "UTAH" to "UT",
                    "VERMONT" to "VT",
                    "VIRGINIA" to "VA",
                    "WASHINGTON" to "WA",
                    "WEST VIRGINIA" to "WV",
                    "WISCONSIN" to "WI",
                    "WYOMING" to "WY",
                    "DISTRICT OF COLUMBIA" to "DC"
            )

    fun getStateCode(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim().uppercase()
        if (trimmed.length == 2) return trimmed // Already a code
        return stateMap[trimmed]
    }
}
