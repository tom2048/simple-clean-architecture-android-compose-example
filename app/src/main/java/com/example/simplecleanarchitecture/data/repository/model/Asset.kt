package com.example.simplecleanarchitecture.data.repository.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Asset(
    val id: String?,
    val type: Type,
    val userId: String,
    val uri: String
) : Parcelable {

    enum class Type {
        Avatar, IdScan
    }

}