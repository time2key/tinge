package com.thaddeussoftware.tinge.database.phillipsHue.lights

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

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