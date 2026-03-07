package code.name.monkey.retromusic.fragments.jellyfin

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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

class JellyfinLibraryFragment : Fragment(R.layout.fragment_jellyfin_library_new) {

    private var _binding: FragmentJellyfinLibraryNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinLibraryViewModel by viewModels()
    private lateinit var albumCardAdapter: JellyfinAlbumCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinLibraryNewBinding.bind(view)

        enterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)
        reenterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)

        setupUI()
        observeViewModel()
        loadJellyfinLibrary()
    }

    private fun setupUI() {
        // 设置随机播放按钮
        binding.shuffleButton.setOnClickListener {
            shuffleAll()
        }

        // 设置专辑网格适配器
        albumCardAdapter = JellyfinAlbumCardAdapter { album ->
            navigateToAlbumDetails(album.id)
        }

        binding.albumsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = albumCardAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            albumCardAdapter.submitList(albums)
            
            // 显示或隐藏空状态视图
            if (albums.isEmpty()) {
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

    private fun loadJellyfinLibrary() {
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (serverUrl.isNullOrEmpty() || accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            requireContext().showToast("请先连接到 Jellyfin 服务器")
            findNavController().navigateUp()
            return
        }

        viewModel.setConnectionInfo(serverUrl, accessToken, userId)
        viewModel.loadAlbums()
    }

    private fun shuffleAll() {
        val albums = viewModel.albums.value
        if (albums.isNullOrEmpty()) {
            requireContext().showToast("没有可播放的专辑")
            return
        }
        
        requireContext().showToast("随机播放所有专辑")
    }

    private fun navigateToAlbumDetails(albumId: String) {
        findNavController().navigate(
            R.id.action_jellyfin_library_to_album_details,
            JellyfinAlbumDetailsFragment.createArgs(albumId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}