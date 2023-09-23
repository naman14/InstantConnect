package com.naman14.airstackandroid;

import com.apollographql.apollo3.ApolloClient
import com.example.SocialQuery

object AirStack {

    var apiClient: ApolloClient = ApolloClient.Builder()
        .serverUrl("https://api.airstack.xyz/gql")
        .addHttpHeader("authorization", "9860c28d23a24c2881a2d796d2f5fcc1")
        .build()

    suspend fun getSocialProfile(address: String): SocialQuery.Data? {
        val response = apiClient.query(SocialQuery(id = address)).execute()
        return response.data
    }
}
