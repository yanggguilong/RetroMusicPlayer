package code.name.monkey.retromusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.ItemAlbumCardBinding
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinAlbum
import code.name.monkey.retromusic.glide.RetroGlideExtension
import com.bumptech.glide.Glide

class JellyfinAlbumCardAdapter(
    private val onAlbumClick: (JellyfinAlbum) -> Unit
) : ListAdapter<JellyfinAlbum, JellyfinAlbumCardAdapter.ViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumCardBinding.inflate(
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
        private val binding: ItemAlbumCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: JellyfinAlbum) {
            binding.title.text = album.name

            Glide.with(binding.root)
                .load(album.imageUrl)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(binding.image)

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