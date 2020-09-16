package com.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.R
import com.musicplayer.adapters.AlbumDetailAdapter
import com.musicplayer.data.AlbumModel
import com.musicplayer.data.AudioModel
import com.musicplayer.data.DataManager

class AlbumDetailFragment : Fragment {

    private var mContext: Context
    private var mData: AlbumModel
    private lateinit var v: View
    private lateinit var recyclerView: RecyclerView

    constructor(mContext: Context, position: Int) : super() {
        this.mContext = mContext
        this.mData = DataManager.returnInstance().getAlbum(position)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        this.v = inflater.inflate(R.layout.recycler_view, container, false)

        recyclerView = v.findViewById(R.id.recyclerView)
        val albumDetailAdapter = AlbumDetailAdapter(mContext, mData.getSongs() as MutableList<AudioModel>)
        recyclerView.adapter = albumDetailAdapter
        return v
    }
}