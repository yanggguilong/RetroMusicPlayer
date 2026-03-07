package code.name.monkey.retromusic.fragments.jellyfin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.adapter.JellyfinSongAdapter
import code.name.monkey.retromusic.databinding.FragmentJellyfinAlbumDetailsBinding
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.fragments.base.AbsMainActivityFragment
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinSong
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.model.Song
import code.name.monkey.retromusic.util.PreferenceUtil
import com.bumptech.glide.Glide

class JellyfinAlbumDetailsFragment : AbsMainActivityFragment(R.layout.fragment_jellyfin_album_details) {

    private var _binding: FragmentJellyfinAlbumDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinAlbumDetailsViewModel by viewModels()
    private lateinit var songAdapter: JellyfinSongAdapter

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return false
    }

    companion object {
        private const val ARG_ALBUM_ID = "album_id"

        fun createArgs(albumId: String): Bundle {
            return Bundle().apply {
                putString(ARG_ALBUM_ID, albumId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinAlbumDetailsBinding.bind(view)

        val albumId = arguments?.getString(ARG_ALBUM_ID) ?: return

        setupUI()
        observeViewModel()
        loadAlbumDetails(albumId)
    }

    private fun setupUI() {
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        songAdapter = JellyfinSongAdapter { song ->
            playSong(song)
        }

        binding.songsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        binding.playAllButton.setOnClickListener {
            viewModel.album.value?.let { album ->
                viewModel.songs.value?.let { songs ->
                    if (songs.isNotEmpty()) {
                        playSong(songs.first())
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.album.observe(viewLifecycleOwner) { album ->
            mainActivity.supportActionBar?.title = album.name
            binding.albumName.text = album.name
            binding.albumArtist.text = album.artist
            binding.albumYear.text = album.year.toString()

            Glide.with(this)
                .load(album.imageUrl)
                .placeholder(R.drawable.ic_album)
                .error(R.drawable.ic_album)
                .into(binding.albumCover)
        }

        viewModel.songs.observe(viewLifecycleOwner) { songs ->
            songAdapter.submitList(songs)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                requireContext().showToast(error)
            }
        }
    }

    private fun loadAlbumDetails(albumId: String) {
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (serverUrl.isNullOrEmpty() || accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            requireContext().showToast("请先连接到 Jellyfin 服务器")
            findNavController().navigateUp()
            return
        }

        viewModel.setConnectionInfo(serverUrl, accessToken, userId)
        viewModel.loadAlbumDetails(albumId)
    }

    private fun playSong(song: JellyfinSong) {
        // 将 JellyfinSong 转换为 Song 对象
        val convertedSong = Song(
            id = song.id.hashCode().toLong(),
            title = song.name,
            trackNumber = 0,
            year = 0,
            duration = song.duration * 1000, // 转换为毫秒
            data = song.streamUrl,
            dateModified = System.currentTimeMillis(),
            albumId = song.albumId.hashCode().toLong(),
            albumName = song.albumName,
            artistId = song.artist.hashCode().toLong(),
            artistName = song.artist,
            composer = null,
            albumArtist = song.artist
        )
        
        // 构建播放队列
        val queue = mutableListOf<Song>()
        viewModel.songs.value?.forEach { jellyfinSong ->
            val queueSong = Song(
                id = jellyfinSong.id.hashCode().toLong(),
                title = jellyfinSong.name,
                trackNumber = 0,
                year = 0,
                duration = jellyfinSong.duration * 1000,
                data = jellyfinSong.streamUrl,
                dateModified = System.currentTimeMillis(),
                albumId = jellyfinSong.albumId.hashCode().toLong(),
                albumName = jellyfinSong.albumName,
                artistId = jellyfinSong.artist.hashCode().toLong(),
                artistName = jellyfinSong.artist,
                composer = null,
                albumArtist = jellyfinSong.artist
            )
            queue.add(queueSong)
        }
        
        // 播放歌曲
        MusicPlayerRemote.openQueue(queue, queue.indexOf(convertedSong), true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}