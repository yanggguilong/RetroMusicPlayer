package code.name.monkey.retromusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.databinding.ItemJellyfinSongBinding
import code.name.monkey.retromusic.fragments.jellyfin.model.JellyfinSong
import com.bumptech.glide.Glide

class JellyfinSongAdapter(
    private val onSongClick: (JellyfinSong) -> Unit
) : ListAdapter<JellyfinSong, JellyfinSongAdapter.ViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJellyfinSongBinding.inflate(
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
        private val binding: ItemJellyfinSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: JellyfinSong) {
            binding.songTitle.text = song.name
            binding.songArtist.text = song.artist
            binding.songDuration.text = formatDuration(song.duration)

            Glide.with(binding.root)
                .load(song.imageUrl)
                .placeholder(R.drawable.ic_audio_file)
                .error(R.drawable.ic_audio_file)
                .into(binding.songCover)

            binding.root.setOnClickListener {
                onSongClick(song)
            }
        }

        private fun formatDuration(duration: Long): String {
            val minutes = duration / 60
            val seconds = duration % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    private class SongDiffCallback : DiffUtil.ItemCallback<JellyfinSong>() {
        override fun areItemsTheSame(oldItem: JellyfinSong, newItem: JellyfinSong): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JellyfinSong, newItem: JellyfinSong): Boolean {
            return oldItem == newItem
        }
    }
}