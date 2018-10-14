package com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment

/**
 * Each of these represents a state that the ConnectToHubFragment can be in. Each state consists of
 * a distinct UI display.
 * */
enum class ConnectToHubFragmentState {
    /**
     * The fragment is still performing its initial search for hubs, and has not found any yet
     * */
    SEARCHING_FOR_HUBS,
    /**
     * The fragment has found a single hub (it may also be searching for more)
     * */
    ONE_HUB_FOUND,
    /**
     * The fragment has found more than one hub (it may also be searching for more)
     * */
    MANY_HUBS_FOUND,
    /**
     * The fragment has completed its search, and has not found any hubs
     * */
    NO_HUBS_FOUND,
    /**
     * A hub has been successfully connected to.
     * */
    CONNECTED_TO_HUB
}