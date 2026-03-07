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
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.FragmentJellyfinBinding
import code.name.monkey.retromusic.extensions.showToast
import code.name.monkey.retromusic.fragments.base.AbsMainActivityFragment
import code.name.monkey.retromusic.util.PreferenceUtil

class JellyfinFragment : AbsMainActivityFragment(R.layout.fragment_jellyfin) {

    private var _binding: FragmentJellyfinBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JellyfinViewModel by viewModels()

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJellyfinBinding.bind(view)

        setupUI()
        checkLoginStatus()
        observeViewModel()
    }

    private fun setupUI() {
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.title = "Jellyfin"
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.connectButton.setOnClickListener {
            connectToJellyfin()
        }

        binding.serverUrl.setText(viewModel.getServerUrl())
        binding.username.setText(viewModel.getUsername())
    }

    private fun checkLoginStatus() {
        // 检查是否已经有保存的登录状态
        val serverUrl = PreferenceUtil.jellyfinServerUrl
        val accessToken = PreferenceUtil.jellyfinAccessToken
        val userId = PreferenceUtil.jellyfinUserId
        
        if (!serverUrl.isNullOrEmpty() && !accessToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            // 已经登录，直接导航到Jellyfin主界面
            navigateToJellyfinLibrary()
        }
    }

    private fun observeViewModel() {
        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is JellyfinConnectionState.Loading -> {
                    showLoading(true)
                }
                is JellyfinConnectionState.Success -> {
                    showLoading(false)
                    requireContext().showToast("连接成功")
                    navigateToJellyfinLibrary()
                }
                is JellyfinConnectionState.Error -> {
                    showLoading(false)
                    requireContext().showToast("连接失败: ${state.message}")
                }
                is JellyfinConnectionState.Idle -> {
                }
            }
        }
    }

    private fun connectToJellyfin() {
        val serverUrl = binding.serverUrl.text.toString().trim()
        val username = binding.username.text.toString().trim()
        val password = binding.password.text.toString()

        if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            requireContext().showToast("请填写所有字段")
            return
        }

        viewModel.connectToServer(serverUrl, username, password)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.connectButton.isEnabled = !show
    }

    private fun navigateToJellyfinLibrary() {
        findNavController().navigate(R.id.action_jellyfin_to_main)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}