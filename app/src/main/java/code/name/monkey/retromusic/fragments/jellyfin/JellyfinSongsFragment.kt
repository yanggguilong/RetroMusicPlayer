package code.name.monkey.retromusic.fragments.jellyfin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.adapter.JellyfinSongAdapter
import code.name.monkey.retromusic.databinding.FragmentJellyfinLibraryNewBinding
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinSong
import code.name.monkey.retromusic.util.PreferenceUtil
import com.google.android.material.transition.MaterialFadeThrough

class JellyfinSongsFragment : Fragment(R.layout.fragment_jellyfin_library_new) {

    private var _binding: FragmentJellyfinLibraryNewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinLibraryViewModel by viewModels()
    private lateinit var songAdapter: JellyfinSongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinLibraryNewBinding.bind(view)

        enterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)
        reenterTransition = MaterialFadeThrough().addTarget(binding.albumsRecyclerView)

        setupUI()
        observeViewModel()
        loadJellyfinSongs()
    }

    private fun setupUI() {
        // 设置随机播放按钮
        binding.shuffleButton.setOnClickListener {
            shuffleAll()
        }

        // 设置歌曲列表适配器
        songAdapter = JellyfinSongAdapter {
            playSong(it)
        }

        binding.albumsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.songs.observe(viewLifecycleOwner) { songs ->
            songAdapter.submitList(songs)
            
            // 显示或隐藏空状态视图
            if (songs.isEmpty()) {
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

    private fun loadJellyfinSongs() {
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (serverUrl.isNullOrEmpty() || accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            requireContext().showToast("请先连接到 Jellyfin 服务器")
            findNavController().navigateUp()
            return
        }

        viewModel.setConnectionInfo(serverUrl, accessToken, userId)
        viewModel.loadSongs()
    }

    private fun shuffleAll() {
        val songs = viewModel.songs.value
        if (songs.isNullOrEmpty()) {
            requireContext().showToast("没有可播放的歌曲")
            return
        }
        
        requireContext().showToast("随机播放所有歌曲")
    }

    private fun playSong(song: JellyfinSong) {
        requireContext().showToast("播放: ${song.name}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}