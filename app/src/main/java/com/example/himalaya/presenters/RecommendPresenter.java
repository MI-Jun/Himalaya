package com.example.himalaya.presenters;

import com.example.himalaya.interfaces.IRecommendPresenter;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendPresenter implements IRecommendPresenter {

    private static final String TAG = "RecommendPresenter";
    private RecommendPresenter(){}

    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private static RecommendPresenter sInstance = null;

    /**
     * 获取单例对象
     * @return
     */
    public static RecommendPresenter getInstance() {
        if (sInstance == null){
           synchronized (RecommendPresenter.class){
               if (sInstance == null) {
                   sInstance = new RecommendPresenter();
               }
           }
        }
       return sInstance;
    }

    /**
     * 获取推荐内容，其实就是猜你喜欢
     * 接口：喜马拉雅SDK，3.10.6 获取猜你喜欢专辑
     * */
    @Override
    public void getRecommendList() {
        //获取推荐内容
        //封装参数
        Map<String, String> map = new HashMap<>();
        //这个参数表示一页数据返回多少页（Constants.RECOMMEND_COUNT + ""）
        map.put(DTransferConstants.LIKE_COUNT, Constants.COUNT_rECOMMEND + "");
        CommonRequest.getGuessLikeAlbum(map, new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                LogUtil.d(TAG,"thread name -- >" + Thread.currentThread().getName());
                //数据获取成功
                if (gussLikeAlbumList != null){
                    List<Album> albumList = gussLikeAlbumList.getAlbumList();
                    //数据回来后，我们去更新UI
                    //upRecommendUI(albumList);
                    handlerRecommendResult(albumList);
                }

            }

            @Override
            public void onError(int i, String s) {
                //数据获取失败
                LogUtil.d(TAG,"error -- >" + i);
                LogUtil.d(TAG,"errorMsg -- >" + s);

                handlerError();
            }
        });

    }

    private void handlerError() {
        if (mCallbacks != null) {
            for (IRecommendViewCallback callback : mCallbacks) {
                callback.onNetWorkError();
            }
        }
    }


    private void handlerRecommendResult(List<Album> albumList) {
        //通知UI更新
        if (albumList != null) {
            //测试，清空一下，让界面显示空
            //albumList.clear();
            if (albumList.size() == 0) {
                for (IRecommendViewCallback callback : mCallbacks) {
                    callback.onEmpty();
                }
            }else{
                for (IRecommendViewCallback callback : mCallbacks) {

                    callback.onRecommendListLoaded(albumList);
                }
            }
        }
    }
    private void updataLoading(){
        for (IRecommendViewCallback callback : mCallbacks) {
            callback.onLoading();
        }
    }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void registerViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unregisterViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null) {
            mCallbacks.remove(mCallbacks);
        }
    }
}
