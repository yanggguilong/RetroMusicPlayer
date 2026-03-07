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

class JellyfinArtistsFragment : Fragment(R.layout.fragment_jellyfin_library_new) {

    private var _binding: FragmentJellyfinLibraryNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinLibraryViewModel by viewModels()
    private lateinit var artistAdapter: JellyfinAlbumCardAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinLibraryNewBinding.bind(view)

        enterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)
        reenterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)

        setupUI()
        observeViewModel()
        loadJellyfinArtists()
    }

    private fun setupUI() {
        // 设置随机播放按钮
        binding.shuffleButton.setOnClickListener {
            shuffleAll()
        }

        // 设置艺术家网格适配器
        artistAdapter = JellyfinAlbumCardAdapter {
            navigateToArtistDetails(it.id)
        }

        binding.albumsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = artistAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.artists.observe(viewLifecycleOwner) { artists ->
            artistAdapter.submitList(artists)
            
            // 显示或隐藏空状态视图
            if (artists.isEmpty()) {
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

    private fun loadJellyfinArtists() {
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (serverUrl.isNullOrEmpty() || accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            requireContext().showToast("请先连接到 Jellyfin 服务器")
            findNavController().navigateUp()
            return
        }

        viewModel.setConnectionInfo(serverUrl, accessToken, userId)
        viewModel.loadArtists()
    }

    private fun shuffleAll() {
        val artists = viewModel.artists.value
        if (artists.isNullOrEmpty()) {
            requireContext().showToast("没有可播放的艺术家")
            return
        }
        
        requireContext().showToast("随机播放所有艺术家")
    }

    private fun navigateToArtistDetails(artistId: String) {
        requireContext().showToast("艺术家详情: $artistId")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}