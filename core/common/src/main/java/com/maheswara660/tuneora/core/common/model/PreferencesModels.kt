package com.maheswara660.tuneora.core.common.model

enum class ThemeConfig {
    SYSTEM,
    LIGHT,
    DARK
}


object Sort {
    enum class By {
        TITLE,
        ARTIST,
        ALBUM,
        DATE,
        SIZE,
    }

    enum class Order {
        ASCENDING,
        DESCENDING,
    }
}
