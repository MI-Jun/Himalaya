package com.example.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback {
    /**
     * 专辑详情内容加载出来了
     * @param tracks
     */
    void onDetailListLoaded(List<Track> tracks);

    /**
     * 网络错误
     */
    void onNetworkError(int errorCode, String errorMsg);

    /**
     * 把album传给UI
     * @param album
     */
    void onAlbumLoaded(Album album);

    /**
     * 加载更多的接口
     * @param size size > 0表示加载成功，否则表示加载失败
     */
    void onLoaderMoreFinish(int size);

    /**
     * 下拉加载更多的结果
     * @param size size > 0表示加载成功，否则表示加载失败
     */
    void onRefreshFinisned(int size);
}
