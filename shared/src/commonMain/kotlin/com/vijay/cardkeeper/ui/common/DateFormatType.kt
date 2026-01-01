package com.vijay.cardkeeper.ui.common

enum class DateFormatType(val formatPattern: String) {
    USA("MM/dd/yyyy"), // MM/DD/YYYY
    INDIA("dd/MM/yyyy"), // DD/MM/YYYY
    GENERIC("dd/MM/yyyy") // DD/MM/YYYY
}
