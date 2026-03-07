package code.name.monkey.retromusic.fragments.jellyfin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.adapter.JellyfinAlbumCardAdapter
import code.name.monkey.retromusic.databinding.FragmentJellyfinLibraryNewBinding
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.util.PreferenceUtil
import com.google.android.material.transition.MaterialFadeThrough

class JellyfinPlaylistsFragment : Fragment(R.layout.fragment_jellyfin_library_new) {

    private var _binding: FragmentJellyfinLibraryNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinLibraryViewModel by viewModels()
    private lateinit var playlistAdapter: JellyfinAlbumCardAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinLibraryNewBinding.bind(view)

        enterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)
        reenterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)

        setupUI()
        observeViewModel()
        loadJellyfinPlaylists()
    }

    private fun setupUI() {
        // 设置随机播放按钮
        binding.shuffleButton.setOnClickListener {
            shuffleAll()
        }

        // 设置播放列表网格适配器
        playlistAdapter = JellyfinAlbumCardAdapter {
            navigateToPlaylistDetails(it.id)
        }

        binding.albumsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = playlistAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.submitList(playlists)
            
            // 显示或隐藏空状态视图
            if (playlists.isEmpty()) {
                binding.empty.visibility = View.VISIBLE
            } else {
                binding.empty.visibility = View.GONE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                requireContext().showToast(error)
            }
        }
    }

    private fun loadJellyfinPlaylists() {
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (serverUrl.isNullOrEmpty() || accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            requireContext().showToast("请先连接到 Jellyfin 服务器")
            findNavController().navigateUp()
            return
        }

        viewModel.setConnectionInfo(serverUrl, accessToken, userId)
        viewModel.loadPlaylists()
    }

    private fun shuffleAll() {
        val playlists = viewModel.playlists.value
        if (playlists.isNullOrEmpty()) {
            requireContext().showToast("没有可播放的播放列表")
            return
        }
        
        requireContext().showToast("随机播放所有播放列表")
    }

    private fun navigateToPlaylistDetails(playlistId: String) {
        requireContext().showToast("播放列表详情: $playlistId")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}