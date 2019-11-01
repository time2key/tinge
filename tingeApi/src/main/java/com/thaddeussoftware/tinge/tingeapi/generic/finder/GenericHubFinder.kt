package com.thaddeussoftware.tinge.tingeapi.generic.finder

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

/**
 * Represents a hub finder. Hub finders extending this class will exist for every type of supported
 * hub.
 *
 * Each finder will contain all of the logic required to find hubs of their type, which should
 * be usable for both scanning for new devices to add, and searching for devices the user has
 * previously added (i.e. during the app startup).
 *
 * **How to use**
 * * Subscribe to any/all of [loggedEventObservable], [foundResultObservable] and
 * [methodUpdateObservable], depending on the data interested in.
 * * Call [startFindingHubs] to begin the scan.
 *
 * Created by thaddeusreason on 08/04/2018.
 */
abstract class GenericHubFinder {

    /**
     * All of the most recent [HubSearchFoundResult] instances for each hub, referenced by
     * [HubSearchFoundResult.HubSearchIndividualResult.primaryId].
     * */
    val currentlyFoundResults = HashMap<String, HubSearchFoundResult>()

    /**
     * Observable for [HubSearchMethodUpdate] instances - a new instance will get emitted each time
     * the state of one of the Hub search methods changes, such as when a search method starts
     * or completes etc.
     *
     * This is a hot observable - events will only be emitted when [startFindingHubs] is called.
     *
     * Each [HubSearchMethodUpdate] instance emitted will be a new instance; previously emitted
     * instances will **NOT** be updated to reflect the new data.
     * */
    val methodUpdateObservable: Observable<HubSearchMethodUpdate>
            = Observable.create(object: ObservableOnSubscribe<HubSearchMethodUpdate> {
        override fun subscribe(e: ObservableEmitter<HubSearchMethodUpdate>) {
            methodUpdateEmitter = e
        }
    }).share()

    /**
     * Observable for [HubSearchFoundResult] instances - a new instance will get emitted each time
     * a hub is found by a search method.
     *
     * This is a hot observable - events will only be emitted when [startFindingHubs] is called.
     *
     * If a hub that was previously found by a different search method is found by a new search
     * method, an instance will be emitted. This new instance will contain data about all the
     * previously searches that found this hub however.
     *
     * Each [HubSearchFoundResult] instance emitted will be a new instance; previously emitted
     * instances will **NOT** be updated to reflect the new data (so you can keep references to
     * previously emitted instances and be confident that their values will not change later)
     *
     * */
    val foundResultObservable: Observable<HubSearchFoundResult>
            = Observable.create(object: ObservableOnSubscribe<HubSearchFoundResult> {
        override fun subscribe(e: ObservableEmitter<HubSearchFoundResult>) {
            foundResultEmitter = e
        }
    })

    /**
     * Observable for [HubSearchLoggedEvent] instances - a new instance will get emitted each time
     * info/warnings/errors occur in a hub search - see [HubSearchLoggedEvent] for more info.
     *
     * This is a hot observable - events will only be emitted when [startFindingHubs] is called.
     * */
    val loggedEventObservable: Observable<HubSearchLoggedEvent>
            = Observable.create(object: ObservableOnSubscribe<HubSearchLoggedEvent> {
        override fun subscribe(e: ObservableEmitter<HubSearchLoggedEvent>) {
            loggedEventEmitter = e
        }
    }).share()


    /**
     * Emitter for [methodUpdateObservable]
     * */
    private var methodUpdateEmitter: ObservableEmitter<HubSearchMethodUpdate>? = null

    /**
     * Emitter for [foundResultObservable]
     * */
    private var foundResultEmitter: ObservableEmitter<HubSearchFoundResult>? = null

    /**
     * Emitter for [loggedEventObservable]
     * */
    private var loggedEventEmitter: ObservableEmitter<HubSearchLoggedEvent>? = null



    /**
     * Starts searching for hubs. To receive updates related to this, subscribe to
     * [loggedEventObservable] / [methodUpdateObservable] /
     * [foundResultObservable].
     * */
    abstract fun startFindingHubs()

    /**
     * Stops finding hubs and resets this instance to a blank state so that it can be used again.
     * */
    abstract fun reset()

    /**
     * Subclasses should call this when a search method is updated (started, finished, etc).
     *
     * This will cause a new [HubSearchMethodUpdate] to be emitted by the
     * [methodUpdateObservable].
     * */
    protected fun hubSearchMethodUpdateHasHappened(
            hubSearchMethodType: HubSearchMethodUpdate.HubSearchMethodType,
            hubSearchMethodState: HubSearchMethodUpdate.HubSearchMethodState) {
        methodUpdateEmitter?.onNext(
                HubSearchMethodUpdate(hubSearchMethodState, hubSearchMethodType))
    }

    /**
     * Subclasses should call this when a new result is found for a device.
     *
     * This will attach the result to the correct [HubSearchFoundResult] based on the primary
     * id, update [currentlyFoundResults], and emit the result to [foundResultObservable].
     * */
    protected fun hubSearchMethodIndividualResultFound(hubSearchIndividualResult: HubSearchFoundResult.HubSearchIndividualResult) {

        val primaryId = hubSearchIndividualResult.primaryId

        val hubSearchFoundResult
                = currentlyFoundResults[primaryId]?.clone() as? HubSearchFoundResult
                ?: HubSearchFoundResult()

        hubSearchFoundResult.addIndividualResult(hubSearchIndividualResult)

        currentlyFoundResults.put(hubSearchFoundResult.highestPriorityIndividualResult!!.primaryId,
                hubSearchFoundResult)

        foundResultEmitter?.onNext(hubSearchFoundResult)
    }

    /**
     * Subclasses should call this to emit a new [HubSearchLoggedEvent] each time they want to
     * log an event to the user.
     * */
    protected fun hubSearchLoggedHasHappened(hubSearchLoggedEvent: HubSearchLoggedEvent) {
        loggedEventEmitter?.onNext(hubSearchLoggedEvent)
    }
}