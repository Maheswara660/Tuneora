package com.maheswara660.tuneora.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: TuneoraDispatchers)

enum class TuneoraDispatchers {
    IO,
    Default,
}
