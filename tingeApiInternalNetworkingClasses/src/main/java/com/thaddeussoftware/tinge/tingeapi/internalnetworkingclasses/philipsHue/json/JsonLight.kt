package com.thaddeussoftware.tinge.tingeapi.internalnetworkingclasses.philipsHue.json

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Since

/**
 * Created by thaddeusreason on 07/01/2018.
 */

class JsonLight {

    companion object {
        fun getEmptyJsonLightWithId(id: String): JsonLight {
            val returnValue = JsonLight()
            returnValue.state = JsonLight.JsonState()
            returnValue.uniqueId = id
            return returnValue
        }
    }

    /**The current state that the light is in (color, brightness, on/off etc) */
    var state: JsonState? = null
        private set

    /**Fixed name describing the type of light, e.g. "Extended color light" */
    var type: String? = null
        private set

    /**Name that the user has given the light, e.g. "Hue lamp 1". Between 0 and 32 characters in
     * length */
    var name: String? = null
        //private set

    /**Model id identifying the type of light, e.g. "LCT001". Always 6 characters long */
    @SerializedName("modelid")
    var modelId: String? = null
        private set

    /**The MAC address of the device with a unique endpoint id in the form
     * AA:BB:CC:DD:EE:FF:00:11-XX. Between 6 and 32 characters in length. Only returned in hue
     * api versions >= 1.4*/
    @Since(1.4)
    @SerializedName("uniqueid")
    var uniqueId: String? = null
        private set

    /**Fixed manufacturer name of the light. Between 6 and 32 characters in length. Only returned
     * in hue api versions >= 1.7*/
    @Since(1.7)
    @SerializedName("manufacturername")
    var manufacturerName: String? = null
        private set

    /**String describing the current software version of the light, e.g. "66009461" */
    @SerializedName("swversion")
    var softwareVersion: String? = null
        private set

    class JsonState {

        /**Whether the light is currently switched on or not */
        var on: Boolean? = null
            //private set

        /**Current brightness of the light between 1 (minimum) to 254 (maximum)*/
        @SerializedName("bri")
        var brightness: Int? = null
            //private set

        /**
         * Current hue value of the light between 0 (red) and 65535 (also red) - e.g. 25500 is green
         * and 46920 is blue.
         * */
        var hue: Int? = null
            //private set

        /**
         * Current saturation of the light - 0 is the least saturated/white, 254 is the most
         * saturated/colored
         * */
        var sat: Int? = null
            //private set

        /**
         * The x and y coordinates of the light color in
         * <a href="https://www.developers.meethue.com/documentation/core-concepts#color_gets_more_complicated">CIE color space</a>.
         * Both x and y values are between 0 and 1
         * */
        @SerializedName("xy")
        var cieColorSpaceXY: Array<Float>? = null
            //private set

        /**
         * The mired color temperature of the light. 2012 connected lights are capable of
         * 153 (6500K) to 500 (2000K).
         * @see <a>http://en.wikipedia.org/wiki/Mired</a>
         * */
        @SerializedName("ct")
        var miredColorTemperature: Int? = null
            //private set

        /**
         * The last alert affect which was sent to the light. This doesn't neccesarily correspond
         * with the current alert effect displaying on the light, as this doesn't reset to NONE
         * when the alert effect stops on the light.
         * */
        @SerializedName("alert")
        var lastAlertAffectSet: JsonLightAlertEffect? = null

        /**
         * The current ongoing effect on the light
         * */
        @SerializedName("effect")
        var ongoingEffect: JsonLightOngoingEffect? = null

        /**
         * Whether the light is currently in full color (hue and sat) mode, or white tint mode.
         * Only set when the light supports at least one of the modes
         * */
        @SerializedName("colormode")
        var colorMode: JsonLightColorMode? = null

        /**
         * Whether the light is currently reachable by the bridge (within range/turned on)
         */
        @SerializedName("reachable")
        var reachable: Boolean? = null


        enum class JsonLightAlertEffect {
            /**
             * No alert effect
             * */
            @SerializedName("none")
            NONE,

            /**
             * Alert effect of performing one breathe cycle.
             *
             * In a breathe cycle, the light(s) transition to having a higher brightness than
             * current, to a lower brightness then current, then back to the original brightness
             * (without changing the color)
             * */
            @SerializedName("select")
            BREATHE_CYCLE,

            /**
             * Alert effect of performing breathe cycles repeatedly for a 15 second period (or
             * until [NONE] is sent to the light
             *
             * @see BREATHE_CYCLE
             * */
            @SerializedName("lselect")
            MULTIPLE_BREATHE_CYCLES
        }

        enum class JsonLightOngoingEffect {

            /**
             * No ongoing effect
             * */
            @SerializedName("none")
            NONE,

            /**
             * Color loop effect - causes the light to cycle through all hues with the current
             * set brightness and saturation
             * */
            @SerializedName("colorloop")
            COLOR_LOOP
        }

        enum class JsonLightColorMode {

            /**
             * Hue and saturation mode, where the light is set to any color on the color wheel
             * */
            @SerializedName("hs")
            HUE_AND_SATURATION,

            /**
             * Color temperature mode, where the light is set to a tint from orangey-white to
             * blueish-white
             * */
            @SerializedName("ct")
            COLOR_TEMPERATURE
        }
    }
}
