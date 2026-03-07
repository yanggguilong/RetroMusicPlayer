package code.name.monkey.retromusic.fragments.jellyfin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinAlbum
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinSong
import code.name.monkey.retromusic.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class JellyfinAlbumDetailsViewModel : ViewModel() {

    private val _album = MutableLiveData<JellyfinAlbum>()
    val album: LiveData<JellyfinAlbum> = _album

    private val _songs = MutableLiveData<List<JellyfinSong>>()
    val songs: LiveData<List<JellyfinSong>> = _songs

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var serverUrl: String? = null
    private var accessToken: String? = null
    private var userId: String? = null

    fun setConnectionInfo(url: String, token: String, uid: String) {
        serverUrl = url
        accessToken = token
        userId = uid
    }

    fun loadAlbumDetails(albumId: String) {
        viewModelScope.launch {
            try {
                val url = serverUrl ?: run {
                    _error.value = "未连接到服务器"
                    return@launch
                }

                val token = accessToken ?: run {
                    _error.value = "未连接到服务器"
                    return@launch
                }

                val uid = userId ?: run {
                    _error.value = "未获取到用户ID"
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val albumRequest = okhttp3.Request.Builder()
                        .url("$url/Items/$albumId?UserId=$uid")
                        .addHeader("X-MediaBrowser-Token", token)
                        .build()

                    val albumResponse = client.newCall(albumRequest).execute()
                    if (!albumResponse.isSuccessful) {
                        throw Exception("加载专辑失败: HTTP ${albumResponse.code}")
                    }

                    val albumJson = JSONObject(albumResponse.body?.string() ?: "")

                    val songsRequest = okhttp3.Request.Builder()
                        .url("$url/Users/$uid/Items?ParentId=$albumId&IncludeItemTypes=Audio&SortBy=SortName&SortOrder=Ascending")
                        .addHeader("X-MediaBrowser-Token", token)
                        .build()

                    val songsResponse = client.newCall(songsRequest).execute()
                    if (!songsResponse.isSuccessful) {
                        throw Exception("加载歌曲失败: HTTP ${songsResponse.code}")
                    }

                    val songsJson = JSONObject(songsResponse.body?.string() ?: "")
                    val items = songsJson.getJSONArray("Items")

                    val songList = mutableListOf<JellyfinSong>()
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        songList.add(item.toJellyfinSong(url, token))
                    }

                    Pair(albumJson.toJellyfinAlbum(url), songList)
                }

                _album.value = result.first
                _songs.value = result.second
                _error.value = null
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            }
        }
    }
}

private fun JSONObject.toJellyfinAlbum(serverUrl: String): JellyfinAlbum {
    val id = this.optString("Id", "")
    val name = this.optString("Name", "未知专辑")
    val artist = this.optJSONArray("AlbumArtists")?.optJSONObject(0)?.optString("Name") 
        ?: this.optString("AlbumArtist", "未知艺术家")
    val year = this.optString("PremiereDate", "").take(4).toIntOrNull() ?: 0
    val imageTag = this.optJSONObject("ImageTags")?.optString("Primary") ?: ""
    val imageUrl = if (imageTag.isNotEmpty()) {
        "$serverUrl/Items/$id/Images/Primary?tag=$imageTag"
    } else {
        ""
    }

    return JellyfinAlbum(id, name, artist, year, imageUrl)
}

private fun JSONObject.toJellyfinSong(serverUrl: String, accessToken: String): JellyfinSong {
    val id = this.optString("Id", "")
    val name = this.optString("Name", "未知歌曲")
    val albumId = this.optString("AlbumId", "")
    val albumName = this.optString("Album", "未知专辑")
    val artist = this.optJSONArray("Artists")?.optJSONObject(0)?.optString("Name") ?: "未知艺术家"
    val duration = this.optLong("RunTimeTicks", 0L).div(10000)
    
    val imageTag = this.optJSONObject("ImageTags")?.optString("Primary") ?: ""
    val imageUrl = if (imageTag.isNotEmpty()) {
        "$serverUrl/Items/$id/Images/Primary?tag=$imageTag"
    } else {
        ""
    }
    
    // 为Jellyfin 10.10.0+版本添加音频编解码器参数
    val streamUrl = "$serverUrl/Audio/$id/stream?static=true&api_key=$accessToken&audioCodec=aac"

    return JellyfinSong(id, name, albumId, albumName, artist, duration, imageUrl, streamUrl)
}