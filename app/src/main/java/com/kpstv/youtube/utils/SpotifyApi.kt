package com.kpstv.youtube.utils

import android.app.Activity
import android.content.Intent
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kpstv.youtube.models.spotify.Track
import com.kpstv.youtube.models.spotify.Tracks
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*


class SpotifyApi(
        private val activity: Activity
) {
    companion object {
        var accessToken = ""
        const val AUTHORIZATION_REQUEST_CODE = 616
        private const val REDIRECT_URI = "https://kaustubhpatange.github.io/YTPlayer"
        private const val CLIENT_ID = "ff0d06a6f7c943d9bb0a0e2167efaa1d"

        val gson: Gson = GsonBuilder()
                .serializeNulls()
                .create()
    }

    private var rememberToRunThisBlock: ((AuthResponse?, java.lang.Exception?) -> Unit)? = null

    fun invokeAuthorizationFlow() = with(activity) {
        val builder = AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.CODE,
                REDIRECT_URI)

        builder.setScopes(arrayOf("streaming"))
        val request = builder.build()

        AuthenticationClient.openLoginActivity(this, AUTHORIZATION_REQUEST_CODE, request)
    }

    fun processResponse(requestCode: Int, resultCode: Int, data: Intent?, responseAction: ResponseAction<AuthResponse>) = with(activity) {
        if (requestCode == AUTHORIZATION_REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthenticationResponse.Type.CODE -> {
                    val authToken = response.code

                    // Generate a refresh token...
                    val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
                    val body: RequestBody = "grant_type=authorization_code&code=${authToken}&redirect_uri=https://kaustubhpatange.github.io/YTPlayer".toRequestBody(mediaType)


                    val request = Request.Builder()
                            .url("https://accounts.spotify.com/api/token")
                            .method("POST", body)
                            .addHeader("Authorization", "Basic ZmYwZDA2YTZmN2M5NDNkOWJiMGEwZTIxNjdlZmFhMWQ6ZWE0MDI0MWY5MTg5NDQ5MjkyNTJhMWU2MWJkYTNkY2Y=")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .build()

                    val client = OkHttpClient().newBuilder().build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread { responseAction.onError(e) }
                            rememberToRunThisBlock = null
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val json = response.body?.string()
                                if (!json.isNullOrBlank()) {
                                    val obj = JSONObject(json)
                                    accessToken = obj.getString("access_token")
                                    val refreshToken = obj.getString("refresh_token")

                                    val date = Calendar.getInstance().apply {
                                        add(Calendar.MINUTE, 50)
                                    }.time

                                    PreferenceManager.getDefaultSharedPreferences(this@with)
                                            .edit().apply {
                                                putString("accessToken", accessToken)
                                                putLong("expiresIn", date.getFormattedDate().toLong())
                                                putString("refreshToken", refreshToken)
                                            }.apply()

                                    val auth = AuthResponse(
                                            accessToken = accessToken,
                                            refreshToken = refreshToken
                                    )
                                    rememberToRunThisBlock?.invoke(auth, null)

                                    runOnUiThread { responseAction.onComplete(auth) }

                                } else
                                    runOnUiThread { responseAction.onError(Exception("Response body is null or empty")) }
                            } else
                                runOnUiThread {
                                    responseAction.onError(Exception("Response not successful"))
                                }
                            rememberToRunThisBlock = null
                        }
                    })
                }
                AuthenticationResponse.Type.ERROR -> {
                    runOnUiThread {
                        responseAction.onError(Exception("Authentication is denied"))
                    }
                }
                else -> {
                    runOnUiThread {
                        responseAction.onError(Exception("Authentication unknown error"))
                    }
                }
            }
        }
    }

    fun createAccessTokenFromRefreshToken(responseListener: ResponseListener<AuthResponse>) = with(activity) {
        val refreshToken = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("refreshToken", "")

        if (!refreshToken.isNullOrBlank()) {
            val client = OkHttpClient().newBuilder().build()
            val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
            val body = "grant_type=refresh_token&refresh_token=${refreshToken}&redirect_uri=https://kaustubhpatange.github.io/YTPlayer".toRequestBody(mediaType)

            val request = Request.Builder()
                    .url("https://accounts.spotify.com/api/token")
                    .method("POST", body)
                    .addHeader("Authorization", "Basic ZmYwZDA2YTZmN2M5NDNkOWJiMGEwZTIxNjdlZmFhMWQ6ZWE0MDI0MWY5MTg5NDQ5MjkyNTJhMWU2MWJkYTNkY2Y=")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    responseListener.error(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val json = response.body?.string()
                        if (!json.isNullOrBlank()) {
                            accessToken = JSONObject(json).getString("access_token")

                            val date = Calendar.getInstance().apply {
                                add(Calendar.MINUTE, 50)
                            }.time

                            PreferenceManager.getDefaultSharedPreferences(this@with)
                                    .edit().apply {
                                        putString("accessToken", accessToken)
                                        putLong("expiresIn", date.getFormattedDate().toLong())
                                    }.apply()
                            responseListener.complete(
                                    AuthResponse(
                                            accessToken = accessToken,
                                            refreshToken = refreshToken
                                    )
                            )
                        } else
                            responseListener.error(java.lang.Exception("Response body is null"))
                    } else
                        responseListener.error(java.lang.Exception("Response is null"))
                }
            })
        } else responseListener.error(java.lang.Exception("Refresh Token is null"))
    }


    // 7ytR5pFWmSjzHJIeQkgog4
    fun getTrackDetail(id: String, responseAction: ResponseAction<Track>) = with(activity) {

        commonWorkFlow { auth, e ->
            if (auth != null) {
                val client = OkHttpClient().newBuilder().build()

                val request = Request.Builder()
                        .url("https://api.spotify.com/v1/tracks?ids=$id")
                        .method("GET", null)
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread { responseAction.onError(e) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val json = response.body?.string()
                            if (!json.isNullOrBlank()) {
                                runOnUiThread { responseAction.onComplete(
                                        gson.fromJson(json, Track::class.java)
                                ) }
                            } else
                                runOnUiThread { responseAction.onError(java.lang.Exception("Response body is null")) }
                        } else
                            runOnUiThread { responseAction.onError(java.lang.Exception("Response is null")) }
                    }
                })
            } else {
                runOnUiThread { responseAction.onError(java.lang.Exception(e)) }
            }
        }
    }

    fun commonWorkFlow(block: (AuthResponse?, java.lang.Exception?) -> Unit) = with(activity) {
        accessToken = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("accessToken", "") ?: ""
        val refreshToken = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("refreshToken", "") ?: ""
        val expiresIn = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong("expiresIn", 0)
        val todayTime = Calendar.getInstance().time.getFormattedDate().toLong()

        val response = AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
        )

        if (todayTime > expiresIn) {
            if (refreshToken.isBlank()) {
                rememberToRunThisBlock = block
                invokeAuthorizationFlow()
            } else {
                createAccessTokenFromRefreshToken(ResponseListener(
                        complete = {
                            block.invoke(it, null)
                        },
                        error = {
                            block.invoke(null, it)
                        }
                ))
            }
        } else block.invoke(response, null)
    }

    data class AuthResponse(val accessToken: String, val refreshToken: String?)

    interface ResponseAction<T> {
        fun onComplete(t: T)
        fun onError(e: Exception)
    }

    open class ResponseListener<T>(
            val complete: (T) -> Unit,
            val error: (Exception) -> Unit
    ) : ResponseAction<T> {
        override fun onComplete(t: T) {
            complete.invoke(t)
        }

        override fun onError(e: Exception) {
            error.invoke(e)
        }
    }
}
