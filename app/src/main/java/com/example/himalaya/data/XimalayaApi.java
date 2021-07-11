package com.example.himalaya.data;

import com.example.himalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.HashMap;
import java.util.Map;

public class XimalayaApi {

    private XimalayaApi(){
    }

    public static XimalayaApi sXimalayaApi;

    public static XimalayaApi getXimalayaApi(){
        if (sXimalayaApi == null){
            synchronized (XimalayaApi.class){
                if (sXimalayaApi == null) {
                    sXimalayaApi = new XimalayaApi();
                }
            }
        }
        return sXimalayaApi;
    }

    /**
     * 获取推荐内容
     * @param callBack 请求结果的回调接口
     */
    public void getRecommendList(IDataCallBack<GussLikeAlbumList> callBack){
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少页（Constants.RECOMMEND_COUNT + ""）
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_rECOMMEND + "");
        CommonRequest.getGuessLikeAlbum(map,callBack);
    }

    /**
     * 根据专辑的id获取到专辑的内容
     *
     * @param callback 获取专辑详情的回调接口
     * @param mCurrentAlbumId 专辑的ID
     * @param mCurrentPageIndex 第几页
     */
    public void getAlbumDetail(IDataCallBack<TrackList> callback, long mCurrentAlbumId,int mCurrentPageIndex){
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.ALBUM_ID, mCurrentAlbumId + "");
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, mCurrentPageIndex + "");
        map.put(DTransferConstants.PAGE_SIZE, Constants.COUNT_DEFAULT + "");
        CommonRequest.getTracks(map,callback);
    }

    /**
     * 根据关键字，进行搜索
     * @param keyword
     */
    public void searchByKeyword(String keyword, int page,IDataCallBack<SearchAlbumList> callBack) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        map.put(DTransferConstants.PAGE_SIZE,Constants.COUNT_DEFAULT + "");
        map.put(DTransferConstants.PAGE, page + "");
        CommonRequest.getSearchedAlbums(map,callBack);

    }

    /**
     * 获取推荐的热词
     * @param callBack
     */
    public void getHotWords(IDataCallBack<HotWordList> callBack){
        Map<String,String> map = new HashMap<>();
        map.put(DTransferConstants.TOP, String.valueOf(Constants.COUNT_HOT_WORD));
        CommonRequest.getHotWords(map,callBack);
    }

    /**
     * 根据关键字获取联想词
     * @param keyword 关键字
     * @param callback 回调
     */
    public void getSuggestWord(String keyword,IDataCallBack<SuggestWords> callback){
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, keyword);
        CommonRequest.getSuggestWord(map, callback);

    }
}
