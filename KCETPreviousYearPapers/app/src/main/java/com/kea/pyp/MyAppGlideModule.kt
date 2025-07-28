package com.kea.pyp

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

@GlideModule
class MyAppGlideModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false 
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {

        builder.setDefaultRequestOptions(
            RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888) 
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE) 
                .signature(ObjectKey("3.8"))
                .dontAnimate() 
        )
    }
}