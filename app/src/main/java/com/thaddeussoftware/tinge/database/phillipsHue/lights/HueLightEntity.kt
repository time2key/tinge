package com.thaddeussoftware.tinge.database.phillipsHue.lights

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HueLight")
class HueLightEntity(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: String,

        @ColumnInfo(name = "name")
        var name: String,

        @ColumnInfo(name = "hubid")
        var hubId: String
)