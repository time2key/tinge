package com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder

/**
 * Represents one or more result from different searches for the same hub (as multiple searches
 * can return the same hub).
 *
 * Use [highestPriorityIndividualResult] to get the search result with the highest priority.
 *
 * Created by thaddeusreason on 29/03/2018.
 */
class HubSearchFoundResult(): Cloneable {

    /**
     * All of the individual results corresponding with this found hub.
     * */
    val individualResults = ArrayList<HubSearchIndividualResult>()

    /**
     * The individual result corresponding with this found hub that has the highest priority.
     * */
    var highestPriorityIndividualResult: HubSearchIndividualResult? = null
        private set

    fun addIndividualResult(hubSearchIndividualResult: HubSearchIndividualResult) {
        individualResults.add(hubSearchIndividualResult)
        calculateHighestPriorityResult()
    }

    protected fun calculateHighestPriorityResult() {
        var tempHighestPriorityIndividualResult: HubSearchIndividualResult? = null

        individualResults.forEach { item ->
            if (item.hubSearchMethodType.priority >
                    tempHighestPriorityIndividualResult?.hubSearchMethodType?.priority ?: Int.MIN_VALUE) {
                tempHighestPriorityIndividualResult = item
            }
        }

        highestPriorityIndividualResult = tempHighestPriorityIndividualResult
    }

    public override fun clone(): Any {
        val newInstance = HubSearchFoundResult()
        newInstance.individualResults.addAll(individualResults)
        newInstance.calculateHighestPriorityResult()
        return newInstance
    }

    /**
     * An individual result for a hub found from a search
     * */
    data class HubSearchIndividualResult(
            val name:String?,
            val primaryId:String,
            val secondaryId:String,
            val hubSearchMethodType: HubSearchMethodUpdate.HubSearchMethodType)
}