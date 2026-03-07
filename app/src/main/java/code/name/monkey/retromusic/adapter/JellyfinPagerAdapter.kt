package code.name.monkey.retromusic.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import code.name.monkey.retromusic.fragments.jellyfin.JellyfinAlbumsFragment
import code.name.monkey.retromusic.fragments.jellyfin.JellyfinArtistsFragment
import code.name.monkey.retromusic.fragments.jellyfin.JellyfinHomeFragment
import code.name.monkey.retromusic.fragments.jellyfin.JellyfinPlaylistsFragment
import code.name.monkey.retromusic.fragments.jellyfin.JellyfinSongsFragment

class JellyfinPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles = listOf("推荐", "歌曲", "专辑", "艺术家", "播放列表")

    override fun getItemCount(): Int = titles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> JellyfinHomeFragment()
            1 -> JellyfinSongsFragment()
            2 -> JellyfinAlbumsFragment()
            3 -> JellyfinArtistsFragment()
            4 -> JellyfinPlaylistsFragment()
            else -> JellyfinHomeFragment()
        }
    }

    fun getPageTitle(position: Int): String = titles[position]
}