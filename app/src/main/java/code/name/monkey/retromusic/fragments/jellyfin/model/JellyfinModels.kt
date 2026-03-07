package code.name.monkey.retromusic.fragments.jellyfin.model

data class JellyfinAlbum(
    val id: String,
    val name: String,
    val artist: String,
    val year: Int,
    val imageUrl: String
)

data class JellyfinSong(
    val id: String,
    val name: String,
    val albumId: String,
    val albumName: String,
    val artist: String,
    val duration: Long,
    val imageUrl: String,
    val streamUrl: String
)