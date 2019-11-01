package com.thaddeussoftware.tinge.tingeapi.generic.finder

/**
 * An event of the type Info, Warning or Error that occurs as a result of a Hub search. These
 * are designed to communicate additional details to the user about searches, that the user can
 * access if they wish to attempt to diagnose why their hub is not listed.
 *
 * An event can be associated with zero or more found hubs - see [affectedHubsPrimaryIds] for more
 * info.
 *
 * Also, an event will be associated with one or more search types - see [affectedSearchMethodTypes]
 * for more info.
 *
 * Created by thaddeusreason on 31/03/2018.
 */
class HubSearchLoggedEvent {

    /**
     * An event can be associated with zero or more found hubs, if the event relates specifically
     * to those found hubs.
     *
     * E.g. if a hub responded to a search in a way that indicated there may be connection issues
     * with it, a warning may be raised associated with that hub.
     * */
    val affectedHubsPrimaryIds = ArrayList<String>()

    /**
     * An event must be associated with one or more search types.
     *
     * Generally speaking, an event will be associated with only one search type, however in some
     * cases it may be possible for an event to be associated with more than one, e.g. if the
     * results of two separate searches are compared against each other and there are
     * inconsistencies.
     * */
    val affectedSearchMethodTypes = ArrayList<HubSearchMethodUpdate.HubSearchMethodType>()

    enum class HubSearchLoggedEventType {
        /**
         * Indicates something that could be of interest to the user if they wish to view
         * additional details, but does not have the potential to affect the accuracy of the results
         * returned.
         *
         * For example, an info event could be logged explaining why a hub search entered the
         * [HubSearchMethodUpdate.HubSearchMethodState.CANCELLED] state.
         * */
        INFO,
        /**
         * Logged when an issue occurs during a hub search that may affect the accuracy of results
         * returned, but does not result in a hub search entering the
         * [HubSearchMethodUpdate.HubSearchMethodState.FAILURE] state.
         *
         * Examples of such issues are:
         * * Two hub searches that are meant to provide consistent results having inconsistencies
         * (as this means that technical issues are occuring with at least one of the scans,
         * so there may be missing or erroneous additional hubs).
         * * A subnet mask being too large to perform a full ip scan on every device on the network
         * (as this means hubs may be missed out).
         *
         * Most [HubSearchLoggedEvent] events will be of this type.
         * */
        WARNING,
        /**
         * Should only be logged when a hub search enters the
         * [HubSearchMethodUpdate.HubSearchMethodState.FAILURE] state.
         *
         * Explains why the hub search failed.
         * */
        ERROR
    }
}