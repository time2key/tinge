package com.thaddeussoftware.tinge.tingeapi.generic.finder

/**
 * Represents an update to the state of a Hub Search Method.
 *
 * Created by thaddeusreason on 29/03/2018.
 */
class HubSearchMethodUpdate(state: HubSearchMethodState, type: HubSearchMethodType) {

    /**
     * Created by thaddeusreason on 29/03/2018.
     */
    enum class HubSearchMethodState {
        /**
         * Indicates that the search has not started yet.
         * */
        NOT_STARTED,
        /**
         * Some searches only run as last resorts if other searches do not complete successfully or
         * return results etc. In this case, the search will enter this state indicating it is
         * cancelled (has not run and will not run).
         * */
        CANCELLED,
        /**
         * Indicates that the Hub search is currently in progress and searching for Hubs.
         * */
        IN_PROGRESS,
        /**
         * Indicates that the Hub search completed without any serious issues.
         *
         * This does not neccesarily indicate that results were found.
         *
         * If the search completes but encounters minor issues that may affect the accuracy of
         * the results, it will still enter this state, but warnings will be logged.
         * */
        SUCCESS,
        /**
         * Indicates that a Hub search method completely failed / there was a serious
         * non-recoverable error performing the search meaning the search had to terminate
         * immediately and return no further results.
         * */
        FAILURE;
    }

    /**
     * Search method type used for scanning for hubs.
     *
     * @param priority
     * Each method type has a priority. If results come back for the same ip address/id, but different
     * names/other details, the priority will be used to determine which to display (higher is
     * preferred).
     *
     * Created by thaddeusreason on 29/03/2018.
     */
    enum class HubSearchMethodType(val priority: Int) {
        PHILLIPS_HUE_SSDP(0),
        PHILLIPS_HUE_CONTACT_SERVER(1),
        PHILLIPS_HUE_IP_LOOKUP(101),
        PHILLIPS_HUE_IP_SCAN(100)
    }
}