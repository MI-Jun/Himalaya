package com.example.himalaya.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.DetailActivity;
import com.example.himalaya.R;
import com.example.himalaya.adapters.AlbumListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.base.BaseFragment;
import com.example.himalaya.interfaces.ISubscriptionCallback;
import com.example.himalaya.interfaces.ISubscriptionPresenter;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.SubscriptionPresenter;
import com.example.himalaya.utils.Constants;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionCallback, AlbumListAdapter.OnRecommendItemClickListen, AlbumListAdapter.OnAlbumItemLongClickListener {
    private ISubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubListView;
    private AlbumListAdapter mAlbumListAdapter;


    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        View rootView = layoutInflater.inflate(R.layout.fragment_subscription,container,false);
        TwinklingRefreshLayout refreshLayout = rootView.findViewById(R.id.over_scroll_view);
        refreshLayout.setEnableLoadmore(false);
        refreshLayout.setEnableRefresh(false);
        mSubListView = rootView.findViewById(R.id.sub_list);
        mSubListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mSubListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(),3);
                outRect.bottom = UIUtil.dip2px(view.getContext(),3);
                outRect.right = UIUtil.dip2px(view.getContext(),5);
                outRect.left = UIUtil.dip2px(view.getContext(),5);

            }
        });
        //
        mAlbumListAdapter = new AlbumListAdapter();
        mAlbumListAdapter.setAlbumItemClickListener(this);
        mAlbumListAdapter.setOnAlbumItemLongClickListener(this);
        mSubListView.setAdapter(mAlbumListAdapter);

        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
        mSubscriptionPresenter.getSubscriptionList();
        return rootView;
    }

    @Override
    public void onAddResult(boolean isSuccess) {

    }

    @Override
    public void onDeleteResult(boolean isSuccess) {

    }

    @Override
    public void onSubscriptionsLoaded(List<Album> albums) {
        //更新UI
        if (mAlbumListAdapter != null) {
            mAlbumListAdapter.setData(albums);
        }
    }

    @Override
    public void onSubFull() {
        Toast.makeText(getActivity(), "订阅数量不得超过" + Constants.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口的注册
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
        mAlbumListAdapter.setAlbumItemClickListener(null);
    }

    @Override
    public void onItemClick(int position, Album album) {
        AlbumDetailPresenter.getInstance().setTargetAlbum(album);
        //item被点击了，跳转到详情界面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Album album) {
//        this.mCurrentClickAlbum = album;
        //订阅的item被长按了.
         Toast.makeText(BaseApplication.getAppContext(), "订阅被长按.", Toast.LENGTH_SHORT).show();
//        ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
//        confirmDialog.setOnDialogActionClickListener(this);
//        confirmDialog.show();
    }
}
