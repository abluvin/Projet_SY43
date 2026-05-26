package com.example.projet.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromEventType(type: EventType?): String? = type?.name

    @TypeConverter
    fun toEventType(value: String?): EventType? = value?.let { EventType.valueOf(it) }

    @TypeConverter
    fun fromMessageType(type: MessageType?): String? = type?.name

    @TypeConverter
    fun toMessageType(value: String?): MessageType? = value?.let { MessageType.valueOf(it) }
}