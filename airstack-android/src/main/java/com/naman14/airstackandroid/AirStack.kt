package com.naman14.airstackandroid;

import com.apollographql.apollo3.ApolloClient
import com.example.GetAllSocialsAndNftsQuery
import com.example.SocialQuery

object AirStack {

    var apiClient: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://api.airstack.xyz/gql")
        .addHttpHeader("authorization", "9860c28d23a24c2881a2d796d2f5fcc1")
        .build()

    suspend fun getSocialProfile(address: String): GetAllSocialsAndNftsQuery.Data? {
        val response = apiClient.query(GetAllSocialsAndNftsQuery(address = address, identity = address)).execute()
        return response.data
    }

    suspend fun getCommonPoaps(address: String, address2: String): GetAllSocialsAndNftsQuery.Data? {
        val response = apiClient.query(GetAllSocialsAndNftsQuery(address = address, identity = address)).execute()
        return response.data
    }
}
