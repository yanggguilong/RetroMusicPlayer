package code.name.monkey.retromusic.fragments.jellyfin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinAlbum
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

class JellyfinLibraryViewModel : ViewModel() {

    private val _albums = MutableLiveData<List<JellyfinAlbum>>()
    val albums: LiveData<List<JellyfinAlbum>> = _albums

    private val _songs = MutableLiveData<List<JellyfinSong>>()
    val songs: LiveData<List<JellyfinSong>> = _songs

    private val _artists = MutableLiveData<List<JellyfinAlbum>>()
    val artists: LiveData<List<JellyfinAlbum>> = _artists

    private val _playlists = MutableLiveData<List<JellyfinAlbum>>()
    val playlists: LiveData<List<JellyfinAlbum>> = _playlists

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var serverUrl: String = ""
    private var accessToken: String = ""
    private var userId: String = ""

    fun setConnectionInfo(url: String, token: String, id: String) {
        serverUrl = url
        accessToken = token
        userId = id
    }

    fun loadAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$serverUrl/Users/$userId/Items?IncludeItemTypes=MusicAlbum&Recursive=true&api_key=$accessToken")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.source()?.readString(StandardCharsets.UTF_8) ?: ""
                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        val items = jsonObject.getJSONArray("Items")
                        val albumList = mutableListOf<JellyfinAlbum>()

                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            albumList.add(item.toJellyfinAlbum())
                        }

                        _albums.postValue(albumList)
                        _error.postValue(null)
                    }
                } else {
                    _error.postValue("获取专辑失败: ${response.code}")
                }
            } catch (e: Exception) {
                _error.postValue("网络错误: ${e.message}")
            }
        }
    }

    fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$serverUrl/Users/$userId/Items?IncludeItemTypes=Audio&Recursive=true&api_key=$accessToken")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.source()?.readString(StandardCharsets.UTF_8) ?: ""
                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        val items = jsonObject.getJSONArray("Items")
                        val songList = mutableListOf<JellyfinSong>()

                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            songList.add(item.toJellyfinSong(serverUrl, accessToken))
                        }

                        _songs.postValue(songList)
                        _error.postValue(null)
                    }
                } else {
                    _error.postValue("获取歌曲失败: ${response.code}")
                }
            } catch (e: Exception) {
                _error.postValue("网络错误: ${e.message}")
            }
        }
    }

    fun loadArtists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$serverUrl/Users/$userId/Items?IncludeItemTypes=MusicArtist&Recursive=true&api_key=$accessToken")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.source()?.readString(StandardCharsets.UTF_8) ?: ""
                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        val items = jsonObject.getJSONArray("Items")
                        val artistList = mutableListOf<JellyfinAlbum>()

                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            artistList.add(item.toJellyfinAlbum())
                        }

                        _artists.postValue(artistList)
                        _error.postValue(null)
                    }
                } else {
                    _error.postValue("获取艺术家失败: ${response.code}")
                }
            } catch (e: Exception) {
                _error.postValue("网络错误: ${e.message}")
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("$serverUrl/Users/$userId/Items?IncludeItemTypes=Playlist&Recursive=true&api_key=$accessToken")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.source()?.readString(StandardCharsets.UTF_8) ?: ""
                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        val items = jsonObject.getJSONArray("Items")
                        val playlistList = mutableListOf<JellyfinAlbum>()

                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            playlistList.add(item.toJellyfinAlbum())
                        }

                        _playlists.postValue(playlistList)
                        _error.postValue(null)
                    }
                } else {
                    _error.postValue("获取播放列表失败: ${response.code}")
                }
            } catch (e: Exception) {
                _error.postValue("网络错误: ${e.message}")
            }
        }
    }

    private fun JSONObject.toJellyfinAlbum(): JellyfinAlbum {
        val id = this.optString("Id", "")
        val name = this.optString("Name", "未知专辑")
        val artist = this.optString("AlbumArtist", "未知艺术家")
        val year = this.optInt("ProductionYear", 0)

        val imageTag = this.optJSONObject("ImageTags")?.optString("Primary") ?: ""
        val imageUrl = if (imageTag.isNotEmpty()) {
            "$serverUrl/Items/$id/Images/Primary?tag=$imageTag&api_key=$accessToken"
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
            "$serverUrl/Items/$id/Images/Primary?tag=$imageTag&api_key=$accessToken"
        } else {
            ""
        }
        
        // 为Jellyfin 10.10.0+版本添加音频编解码器参数
        val streamUrl = "$serverUrl/Audio/$id/stream?static=true&api_key=$accessToken&audioCodec=aac"

        return JellyfinSong(id, name, albumId, albumName, artist, duration, imageUrl, streamUrl)
    }
}