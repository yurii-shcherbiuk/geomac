package io.silentsea.geomac.ui.model

import androidx.annotation.StringRes
import io.silentsea.geomac.R
import io.silentsea.geomac.domain.model.Service

@get:StringRes
val Service.labelRes: Int
    get() = when (this) {
        Service.APPLE -> R.string.apple
        Service.GOOGLE -> R.string.google
        Service.MICROSOFT -> R.string.microsoft
        Service.MYLNIKOV -> R.string.mylnikov
        Service.YANDEX -> R.string.yandex
    }
