package com.example.simplecleanarchitecture.core.lib.resources

import android.app.Application
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes

interface AppResources {

    fun getStringResource(@StringRes id: Int, vararg params: Any): String

    fun getIntResource(@IntegerRes id: Int): Int
}

class AppResourcesDefault(private val application: Application) : AppResources {

    override fun getStringResource(@StringRes id: Int, vararg params: Any): String = application.getString(id)

    override fun getIntResource(@IntegerRes id: Int): Int = application.resources.getInteger(id)

}