package com.musicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.activities.AlbumDetailActivity
import com.musicplayer.activities.PlayerActivity
import com.musicplayer.data.AudioModel
import java.util.concurrent.TimeUnit


class AlbumDetailAdapter : RecyclerView.Adapter<AlbumDetailAdapter.SongViewHolder> {

    private var mContext: Context
    private var mData: MutableList<AudioModel>

    constructor(mContext: Context, songList: MutableList<AudioModel>) : super() {
        this.mContext = mContext
        this.mData = songList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_album_detail, parent, false)

        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val realPos = position + 1
        val duration = mData[position].getDuration()
        val artistNameAndDuration = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )
        )

        holder.songNumber.text = realPos.toString()
        holder.songName.text = mData[position].getTitle()
        holder.duration.text = artistNameAndDuration

        holder.albumListItem.setOnClickListener {

            (mContext as AlbumDetailActivity).setPlaylist(mData)
            (mContext as AlbumDetailActivity).setSong(holder.adapterPosition)

            val intent = Intent(mContext, PlayerActivity::class.java)
            mContext.startActivity(intent)
        }

        holder.addBtn.setOnClickListener {
            (mContext as AlbumDetailActivity).addToPlaylist(mutableListOf<AudioModel>(mData[holder.adapterPosition]))
            val toast = Toast.makeText(mContext, "Song added to playlist", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 64)
            toast.show()
        }

    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class SongViewHolder : RecyclerView.ViewHolder {

        var albumListItem: LinearLayout
        var songNumber: TextView
        var songName: TextView
        var duration: TextView
        var addBtn: ImageButton

        constructor(itemView: View) : super(itemView) {
            super.itemView

            albumListItem = itemView.findViewById(R.id.row_album_detail_view)
            songNumber = itemView.findViewById(R.id.row_album_detail_tv_number)
            songName = itemView.findViewById(R.id.row_album_detail_tv_title)
            duration = itemView.findViewById(R.id.row_album_detail_tv_duration)
            addBtn = itemView.findViewById(R.id.row_album_detail_btn_add)
        }
    }
}