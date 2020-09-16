package com.musicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.activities.MainActivity
import com.musicplayer.activities.PlayerActivity
import com.musicplayer.data.AudioModel
import com.musicplayer.data.DataManager
import java.util.concurrent.TimeUnit

class SongListAdapter : RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    var mContext: Context
    var mData: MutableList<AudioModel> = DataManager.returnInstance().getAllSongs()

    constructor(mContext: Context) : super() {
        this.mContext = mContext
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_song_list, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val duration = mData[position].getDuration()

        val artistNameAndDuration = String.format(
            mData[position].getArtist() + " - %02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )
        )

        holder.title.text = mData[position].getTitle()
        holder.artist.text = artistNameAndDuration

        holder.addBtn.setOnClickListener {
            if (mContext is MainActivity) {
                (mContext as MainActivity).addToPlaylist(mutableListOf<AudioModel>(mData[holder.adapterPosition]))
                val toast = Toast.makeText(mContext, "Song added to playlist", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP, 0, 64)
                toast.show()
            }
        }

        holder.songListItem.setOnClickListener {

            if (mContext is MainActivity) {
                (mContext as MainActivity).setPlaylist(mData)
                (mContext as MainActivity).setSong(holder.adapterPosition)
            }

            val intent = Intent(mContext, PlayerActivity::class.java)
            mContext.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class SongViewHolder : RecyclerView.ViewHolder {

        var songListItem: ConstraintLayout

        //var albumCover : ImageView
        var title: TextView
        var artist: TextView
        var addBtn: ImageButton

        constructor(itemView: View) : super(itemView) {
            super.itemView

            songListItem = itemView.findViewById(R.id.row_song_list_container)
            //albumCover = itemView.findViewById(R.id.songListCoverImageView)
            title = itemView.findViewById(R.id.row_song_list_tv_title)
            artist = itemView.findViewById(R.id.row_song_list_tv_artist)
            addBtn = itemView.findViewById(R.id.row_song_list_btn_add)
        }
    }
}