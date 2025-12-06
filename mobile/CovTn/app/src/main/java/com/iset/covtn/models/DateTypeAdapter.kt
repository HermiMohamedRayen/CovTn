package com.iset.covtn.models

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Gson TypeAdapter for java.util.Date to ensure it's serialized to the ISO 8601 format
 * required by the backend ("yyyy-MM-dd'T'HH:mm:ss.SSSX").
 */
class DateTypeAdapter : TypeAdapter<Date>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US).apply {
        // Always use UTC to ensure consistency across timezones
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Writes a Date object as a formatted string.
     */
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
        } else {
            val formattedDate = dateFormat.format(value)
            out.value(formattedDate)
        }
    }

    /**
     * Reads a formatted string and converts it back to a Date object.
     */
    @Throws(IOException::class)
    override fun read(input: JsonReader): Date? {
        return if (input.peek() == JsonToken.NULL) {
            input.nextNull()
            null
        } else {
            val dateString = input.nextString()
            try {
                dateFormat.parse(dateString)
            } catch (e: ParseException) {
                // Handle potential parsing errors if the format from the server is different
                throw IOException("Failed to parse date: $dateString", e)
            }
        }
    }
}