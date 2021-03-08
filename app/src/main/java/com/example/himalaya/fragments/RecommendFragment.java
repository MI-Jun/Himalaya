package com.example.himalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.DetailActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.RecommendListAdapter;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.IRecommendViewCallback;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallback, UILoader.OnRetryClickListener, RecommendListAdapter.OnRecommendItemClickListen {
    private static final String TAG = "";
    private View mRootView;
    private RecyclerView mRecommendRv;
    private RecommendListAdapter mRecommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {

        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater, container);
            }
        };





        //获取到逻辑层的对象
        mRecommendPresenter = RecommendPresenter.getInstance();
        //先要设置通知接口的注册
        mRecommendPresenter.registerViewCallback(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();

        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(this);

        //返回view，给界面显示
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater,ViewGroup container) {
        //view加载完成
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend,container,false);

        //RecycleView的使用
        //1.找到控件
        mRecommendRv = mRootView.findViewById(R.id.recommend_list);
        //2.设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendRv.setLayoutManager(linearLayoutManager);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(),3);
                outRect.bottom = UIUtil.dip2px(view.getContext(),3);
                outRect.right = UIUtil.dip2px(view.getContext(),5);
                outRect.left = UIUtil.dip2px(view.getContext(),5);

            }
        });
        //3.设置适配器
        mRecommendListAdapter = new RecommendListAdapter();
        mRecommendRv.setAdapter(mRecommendListAdapter);
        mRecommendListAdapter.setOnRecommendItemClickListen(this);
        return mRootView;
    }


    @Override
    public void onRecommendListLoaded(List<Album> result) {
        LogUtil.d(TAG,"onRecommendListLoaded");


        //当我们获取到推荐内容的时候，这个方法就会被调用（成功了）
        //数据回来以后，就是更新UI了
        //把数据设置给适配器，并且更新UI
        mRecommendListAdapter.setData(result);
        mUiLoader.updataStatus(UILoader.UIStatus.SUCCESS);

    }

    @Override
    public void onNetWorkError() {
        LogUtil.d(TAG,"onNetWorkError");
        mUiLoader.updataStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        LogUtil.d(TAG,"onEmpty");
        mUiLoader.updataStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onLoading() {
        LogUtil.d(TAG,"onLoading");
        mUiLoader.updataStatus(UILoader.UIStatus.LOADING);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口的注册，避免内存泄漏
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unregisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        //表示网络不佳的时候，用户点击了重试
        //重新获取数据即可
        if (mRecommendPresenter != null) {
            mRecommendPresenter.getRecommendList();
        }
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击了,跳转到详情界面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);


    }
}
