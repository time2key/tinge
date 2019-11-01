package com.thaddeussoftware.tinge.tingeapi.philipsHue.controller.json

import com.google.gson.Gson
import com.thaddeussoftware.tinge.tingeapi.philipsHue.json.JsonUsernameResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class JsonUsernameResponseTests {

    @Test
    fun correctData_correctResponse() {
        //Arrange:
        val json = "[{'success':{'username': 'AAA'}}]"

        //Act:
        val gson = Gson()
        val parsedResponse = gson.fromJson(json, JsonUsernameResponse::class.java)

        //Assert:
        assertEquals("AAA", parsedResponse.username)
    }
}