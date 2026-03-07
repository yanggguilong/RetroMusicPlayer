package code.name.monkey.retromusic.fragments.jellyfin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

sealed class JellyfinConnectionState {
    object Idle : JellyfinConnectionState()
    object Loading : JellyfinConnectionState()
    data class Success(val serverUrl: String, val userId: String, val accessToken: String) : JellyfinConnectionState()
    data class Error(val message: String) : JellyfinConnectionState()
}

class JellyfinViewModel : ViewModel() {

    private val _connectionState = MutableLiveData<JellyfinConnectionState>(JellyfinConnectionState.Idle)
    val connectionState: LiveData<JellyfinConnectionState> = _connectionState

    fun connectToServer(serverUrl: String, username: String, password: String) {
        _connectionState.value = JellyfinConnectionState.Loading

        viewModelScope.launch {
            try {
                Log.d("Jellyfin", "开始连接服务器: $serverUrl, 用户: $username")
                
                val result = withContext(Dispatchers.IO) {
                    val normalizedUrl = normalizeServerUrl(serverUrl)
                    Log.d("Jellyfin", "标准化URL: $normalizedUrl")
                    
                    val publicInfoUrl = "$normalizedUrl/System/Info/Public"
                    val authUrl = "$normalizedUrl/Users/authenticatebyname"
                    
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    // 测试服务器连接
                    Log.d("Jellyfin", "测试服务器连接: $publicInfoUrl")
                    val request = okhttp3.Request.Builder()
                        .url(publicInfoUrl)
                        .build()

                    val response = client.newCall(request).execute()
                    Log.d("Jellyfin", "服务器响应码: ${response.code}")
                    
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: ""
                        Log.e("Jellyfin", "服务器连接失败: HTTP ${response.code}, $errorBody")
                        throw Exception("无法连接到服务器 (HTTP ${response.code})")
                    }

                    // Jellyfin 认证
                    val authBody = JSONObject().apply {
                        put("Username", username)
                        put("Pw", password)
                    }.toString()
                    
                    Log.d("Jellyfin", "认证请求体: $authBody")

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = authBody.toRequestBody(mediaType)
                    
                    val authRequest = okhttp3.Request.Builder()
                        .url(authUrl)
                        .post(requestBody)
                        .header("X-Emby-Authorization", "MediaBrowser Client=\"RetroMusic\", Device=\"Android\", DeviceId=\"${System.currentTimeMillis()}\", Version=\"1.0.0\"")
                        .build()

                    Log.d("Jellyfin", "发送认证请求到: $authUrl")
                    val authResponse = client.newCall(authRequest).execute()
                    Log.d("Jellyfin", "认证响应码: ${authResponse.code}")
                    
                    if (!authResponse.isSuccessful) {
                        val errorBody = authResponse.body?.string() ?: ""
                        Log.e("Jellyfin", "认证失败: HTTP ${authResponse.code}, $errorBody")
                        throw Exception("认证失败: HTTP ${authResponse.code}, $errorBody")
                    }

                    val authData = authResponse.body?.string() ?: ""
                    Log.d("Jellyfin", "认证响应: $authData")
                    
                    val authJson = JSONObject(authData)
                    val userId = authJson.getJSONObject("User").getString("Id")
                    val accessToken = authJson.getString("AccessToken")

                    Triple(normalizedUrl, userId, accessToken)
                }

                saveCredentials(result.first, username, password, result.second, result.third)

                _connectionState.value = JellyfinConnectionState.Success(result.first, result.second, result.third)
                Log.d("Jellyfin", "连接成功!")
            } catch (e: Exception) {
                Log.e("Jellyfin", "连接异常", e)
                _connectionState.value = JellyfinConnectionState.Error(e.message ?: "连接失败")
            }
        }
    }

    private fun normalizeServerUrl(url: String): String {
        var normalized = url.trim()
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://$normalized"
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.dropLast(1)
        }
        return normalized
    }

    private fun saveCredentials(serverUrl: String, username: String, password: String, userId: String, accessToken: String) {
        PreferenceUtil.jellyfinServerUrl = serverUrl
        PreferenceUtil.jellyfinUsername = username
        PreferenceUtil.jellyfinPassword = password
        PreferenceUtil.jellyfinUserId = userId
        PreferenceUtil.jellyfinAccessToken = accessToken
    }

    fun getServerUrl(): String {
        return PreferenceUtil.jellyfinServerUrl ?: ""
    }

    fun getUsername(): String {
        return PreferenceUtil.jellyfinUsername ?: ""
    }
}