package com.naman14.instantconnect

import android.widget.ImageView
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest

fun ImageView.loadUrl(url: String) {

    val imageLoader = ImageLoader.Builder(this.context)
        .components(fun ComponentRegistry.Builder.() {
            add(SvgDecoder.Factory())
            add(ImageDecoderDecoder.Factory())
        })
        .build()

    val request = ImageRequest.Builder(this.context)
        .crossfade(true)
        .crossfade(500)
        .placeholder(R.color.image_bg)
        .error(R.color.image_bg)
        .data(url)
        .target(this)
        .build()

    imageLoader.enqueue(request)
}