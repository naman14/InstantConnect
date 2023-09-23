/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.naman14.instantconnect.carousel

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.naman14.instantconnect.R
import com.naman14.instantconnect.loadUrl

/** An [RecyclerView.ViewHolder] that displays an item inside a Carousel.  */
class CarouselItemViewHolder(itemView: View, listener: CarouselItemListener) :
    RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView
    private val listener: CarouselItemListener

    init {
        imageView = itemView.findViewById(R.id.carousel_image_view)
        this.listener = listener
    }

    fun bind(item: CarouselItem) {
        imageView.loadUrl(item.uri)
        imageView.contentDescription = item.text
        itemView.setOnClickListener { v: View? ->
            listener.onItemClicked(
                item,
                bindingAdapterPosition
            )
        }
    }
}