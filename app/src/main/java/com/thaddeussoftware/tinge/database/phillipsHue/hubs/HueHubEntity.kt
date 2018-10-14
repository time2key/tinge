package com.thaddeussoftware.tinge.database.phillipsHue.hubs

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by thaddeusreason on 19/01/2018.
 */
@Entity(tableName = "HueHub")
class HueHubEntity(
        @PrimaryKey
        @ColumnInfo(name = "hub_id")
        var hubId: String,

        @ColumnInfo(name = "ip_address")
        var lastKnownIpAddress: String,

        @ColumnInfo(name = "hub_name")
        var lastKnownHubName: String?,

        @ColumnInfo(name = "username")
        var usernameCredentials: String
)