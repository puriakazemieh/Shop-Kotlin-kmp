package com.kazemieh.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiClient(
    val httpClient: HttpClient,
    val baseUrl: String
) {

    suspend inline fun <reified T> get(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        query: Map<String, String> = emptyMap()
    ): T {
        return httpClient.get("$baseUrl/$endpoint") {
            headers.forEach { (k, v) -> header(k, v) }

            url {
                query.forEach { (k, v) ->
                    parameters.append(k, v)
                }
            }
        }.body()
    }

    suspend inline fun <reified B, reified R> post(
        endpoint: String,
        body: B,
        headers: Map<String, String> = emptyMap()
    ): R {
        return httpClient.post("$baseUrl/$endpoint") {
            headers.forEach { (k, v) -> header(k, v) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend inline fun <reified T> delete(
        endpoint: String
    ): T {
        return httpClient.delete("$baseUrl/$endpoint").body()
    }
}