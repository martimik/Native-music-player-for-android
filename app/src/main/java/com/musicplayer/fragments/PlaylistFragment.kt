package com.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.adapters.PlaylistAdapter
import com.musicplayer.services.MusicService
import com.musicplayer.utility.ItemMoveCallback
import com.musicplayer.utility.StartDragListener

class PlaylistFragment : Fragment, StartDragListener {

    private var mContext: Context
    private val musicSrv: MusicService?
    private val fm: FragmentManager
    private lateinit var v: View
    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var recyclerView: RecyclerView

    constructor(mContext: Context, musicService: MusicService?, fm: FragmentManager) : super() {
        this.mContext = mContext
        this.musicSrv = musicService
        this.fm = fm
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        this.v = inflater.inflate(R.layout.fragment_playlist, container, false)

        recyclerView = v.findViewById(R.id.fragment_playlist_recyclerview)
        val playlistAdapter = PlaylistAdapter(mContext, musicSrv, this)
        recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = playlistAdapter

        val callback: ItemTouchHelper.Callback = ItemMoveCallback(playlistAdapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        v.findViewById<ImageButton>(R.id.fragment_playlist_btn_back).setOnClickListener {
            fm.popBackStack()
        }

        return v

    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }
}