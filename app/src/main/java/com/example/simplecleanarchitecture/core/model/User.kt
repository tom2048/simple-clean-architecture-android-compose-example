package com.example.simplecleanarchitecture.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String?,
    val nickname: String,
    val email: String,
    val description: String = "",
    val avatarUri: String? = null,
    val idScanUri: String? = null
) : Parcelable