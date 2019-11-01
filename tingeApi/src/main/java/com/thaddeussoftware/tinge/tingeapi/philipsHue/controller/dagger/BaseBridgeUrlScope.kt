package com.thaddeussoftware.tinge.tingeapi.philipsHue.dagger

import javax.inject.Scope

/**
 * Dagger 2 scope - lasts the duration of a base bridge url / ip address
 *
 * Created by thaddeusreason on 14/01/2018.
 */

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseBridgeUrlScope