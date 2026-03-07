package code.name.monkey.retromusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.ItemJellyfinAlbumBinding
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinAlbum
import com.bumptech.glide.Glide

class JellyfinAlbumAdapter(
    private val onAlbumClick: (JellyfinAlbum) -> Unit
) : ListAdapter<JellyfinAlbum, JellyfinAlbumAdapter.ViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJellyfinAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemJellyfinAlbumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: JellyfinAlbum) {
            binding.albumName.text = album.name
            binding.albumArtist.text = album.artist
            binding.albumYear.text = album.year.toString()

            Glide.with(binding.root)
                .load(album.imageUrl)
                .placeholder(R.drawable.ic_album)
                .error(R.drawable.ic_album)
                .into(binding.albumCover)

            binding.root.setOnClickListener {
                onAlbumClick(album)
            }
        }
    }

    private class AlbumDiffCallback : DiffUtil.ItemCallback<JellyfinAlbum>() {
        override fun areItemsTheSame(oldItem: JellyfinAlbum, newItem: JellyfinAlbum): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JellyfinAlbum, newItem: JellyfinAlbum): Boolean {
            return oldItem == newItem
        }
    }
}