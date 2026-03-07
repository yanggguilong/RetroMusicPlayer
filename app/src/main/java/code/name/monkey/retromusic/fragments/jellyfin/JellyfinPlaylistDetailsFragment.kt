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
import code.name.monkey.retromusic.databinding.FragmentJellyfinPlaylistDetailsBinding
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.fragments.base.AbsMainActivityFragment
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinSong
import code.name.monkey.retromusic.util.PreferenceUtil
import com.google.android.material.transition.MaterialFadeThrough

class JellyfinPlaylistDetailsFragment : AbsMainActivityFragment(R.layout.fragment_jellyfin_playlist_details) {

    private var _binding: FragmentJellyfinPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinLibraryViewModel by viewModels()
    private lateinit var songAdapter: JellyfinSongAdapter

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinPlaylistDetailsBinding.bind(view)

        enterTransition = MaterialFadeThrough()
        reenterTransition = MaterialFadeThrough()

        setupUI()
        loadPlaylistDetails()
    }

    private fun setupUI() {
        mainActivity.setSupportActionBar(binding.appBarLayout.toolbar)
        mainActivity.supportActionBar?.title = "播放列表详情"
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.appBarLayout.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        songAdapter = JellyfinSongAdapter {
            playSong(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        binding.refreshLayout.setOnRefreshListener {
            loadPlaylistDetails()
        }
    }

    private fun loadPlaylistDetails() {
        val playlistId = arguments?.getString("playlist_id") ?: return
        
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (serverUrl.isNullOrEmpty() || accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            requireContext().showToast("请先连接到 Jellyfin 服务器")
            findNavController().navigateUp()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        viewModel.setConnectionInfo(serverUrl, accessToken, userId)
        viewModel.loadSongs() // 这里需要修改为加载播放列表的歌曲
    }

    private fun playSong(song: JellyfinSong) {
        requireContext().showToast("播放: ${song.name}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}