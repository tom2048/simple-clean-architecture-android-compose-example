package com.example.simplecleanarchitecture.core.mock

import com.example.simplecleanarchitecture.domain.model.User
import com.example.simplecleanarchitecture.data.repository.model.UserDetails

@Suppress("unused")
object Mocks {

    object UserDetails {

        val listDefault = listOf(
            UserDetails(
                "a312b3ee-84c2-11eb-8dcd-0242ac130003",
                "Nickname1",
                "nickname1@test.com",
                "Test description 1"
            ),
            UserDetails(
                "3b04aacf-4320-48bb-8171-af512aae0894",
                "Nickname2",
                "nickname2@test.com",
                "Test description 1"
            ),
            UserDetails(
                "52408bc4-4cdf-49ef-ac54-364bfde3fbf0",
                "Nickname3",
                "nickname3@test.com",
                "Test description 1"
            )
        )

        val detailsDefault = listDefault.first()

    }

    object User {

        val detailsDefault = Mocks.UserDetails.detailsDefault.run {
            User(
                id = id,
                nickname = nickname,
                email = email,
                description = description,
                avatarUri = "",
                idScanUri = ""
            )
        }

    }

    object Asset {

        val avatar = com.example.simplecleanarchitecture.data.repository.model.Asset(
            id = "assetId1",
            type = com.example.simplecleanarchitecture.data.repository.model.Asset.Type.Avatar,
            userId = Mocks.UserDetails.detailsDefault.id!!,
            uri = ""
        )

        val idScan = com.example.simplecleanarchitecture.data.repository.model.Asset(
            id = "assetId2",
            type = com.example.simplecleanarchitecture.data.repository.model.Asset.Type.IdScan,
            userId = Mocks.UserDetails.detailsDefault.id!!,
            uri = ""
        )

        val default = avatar

    }

    object Storage {

        val defaultAssetContents = "test".toByteArray()

    }

}