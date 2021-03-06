package com.example.himalaya;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.adapters.TrackListAdapter;
import com.example.himalaya.base.BaseActivity;
import com.example.himalaya.base.BaseApplication;
import com.example.himalaya.interfaces.IAlbumDetailViewCallback;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.interfaces.ISubscriptionCallback;
import com.example.himalaya.interfaces.ISubscriptionPresenter;
import com.example.himalaya.presenters.AlbumDetailPresenter;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.presenters.SubscriptionPresenter;
import com.example.himalaya.utils.Constants;
import com.example.himalaya.utils.ImageBlur;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.RoundRectImageView;
import com.example.himalaya.views.UILoader;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.header.bezierlayout.BezierLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class DetailActivity extends BaseActivity implements IAlbumDetailViewCallback, UILoader.OnRetryClickListener, TrackListAdapter.ItemClickListener, IPlayerCallback, ISubscriptionCallback {

    private static final String TAG = "DetailActivity";
    private ImageView mLargeCover;
    private RoundRectImageView mSmallCover;
    private TextView mAlbumTitle;
    private TextView mAlbumAuthor;
    private AlbumDetailPresenter mAlbumDetailPresenter;
    private int mCurrentPage = 1;
    private RecyclerView mDetailList;
    private TrackListAdapter mDetailListAdapter;
    private FrameLayout mDetailListContainer;
    private UILoader mUiLoader;
    private  long mCurremtId = -1;
    private ImageView mPlayControlBtn;
    private TextView mPlayControlTips;
    private PlayerPresenter mPlayerPresenter;
    private List<Track> mCurrentTracks = null;
    private final static int DEFAULT_PLAY_INDEX = 0;
    private TwinklingRefreshLayout mRefreshLayout;
    private String mTrackTitle;
    private String mCurrentTrackTitle;
    private TextView mSubBtn;
    private ISubscriptionPresenter mSubscriptionPresenter;
    private Album mCurrentAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initView();
        initPresenter();
        //???????????????????????????
        updateSubState();
        updatePlayState(mPlayerPresenter.isPlaying());
        initListener();

    }

    private void updateSubState() {
        if (mSubscriptionPresenter != null) {
            boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
            mSubBtn.setText(isSub ? R.string.cancel_sub_tips_text : R.string.sub_tips_text);
        }
    }

    private void initPresenter() {
        //????????????????????????presenter
        mAlbumDetailPresenter = AlbumDetailPresenter.getInstance();
        mAlbumDetailPresenter.registerViewCallback(this);

        //????????????Presenter
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);
        //???????????????presenter
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.getSubscriptionList();
        mSubscriptionPresenter.registerViewCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.unRegisterViewCallback(this);
        }
        if (mPlayerPresenter != null) {
            mPlayerPresenter.unRegisterViewCallback(this);
        }
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
    }

    private void initListener() {
            mPlayControlBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //????????????????????????????????????
                    //TODO:
                    boolean has = mPlayerPresenter.hasPlayList();
                    if (has) {
                        handlePlayControl();

                    }else{
                        handleNoPlayList();

                    }
                }
            });

        mSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSubscriptionPresenter != null) {
                    boolean isSub = mSubscriptionPresenter.isSub(mCurrentAlbum);
                    //?????????????????????????????????????????????????????????????????????????????????
                    if (isSub) {
                        mSubscriptionPresenter.deleteSubscription(mCurrentAlbum);
                    } else {
                        mSubscriptionPresenter.addSubscription(mCurrentAlbum);
                    }
                }
            }
        });

    }

    /**
     *??????????????????????????????????????????????????????????????????
     */
    private void handleNoPlayList() {
        mPlayerPresenter.setPlayList(mCurrentTracks,DEFAULT_PLAY_INDEX);

    }

    private void handlePlayControl() {
        if (mPlayerPresenter != null) {
            //????????????????????????
            if (mPlayerPresenter.isPlaying()) {
                //??????????????????????????????
                mPlayerPresenter.pause();
            }else{
                mPlayerPresenter.play();
            }
        }
    }

    private void initView() {
        mDetailListContainer = this.findViewById(R.id.detail_list_container);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(this) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }
            };
            mDetailListContainer.removeAllViews();
            mDetailListContainer.addView(mUiLoader);
            mUiLoader.setOnRetryClickListener(DetailActivity.this);
        }

        mLargeCover = this.findViewById(R.id.iv_large_cover);
        mSmallCover = this.findViewById(R.id.viv_small_cover);
        mAlbumTitle = this.findViewById(R.id.tv_album_title);
        mAlbumTitle.setSelected(true);
        mAlbumAuthor = this.findViewById(R.id.tv_album_author);
        //?????????????????????
        mPlayControlBtn = findViewById(R.id.detail_play_control);
        mPlayControlTips = findViewById(R.id.play_control_tv);
        mPlayControlTips.setSelected(true);
        //
        mSubBtn = this.findViewById(R.id.detail_sub_btn);

    }

    private View createSuccessView(ViewGroup container) {
        View detailListView = LayoutInflater.from(this).inflate(R.layout.item_detail_list, container, false);
        mDetailList = detailListView.findViewById(R.id.album_detail_list);
        mRefreshLayout = detailListView.findViewById(R.id.refresh_layout);
        //RecyclerView???????????????
        //?????????????????????????????????
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDetailList.setLayoutManager(layoutManager);
        //???????????????????????????
        mDetailListAdapter = new TrackListAdapter();
        mDetailList.setAdapter(mDetailListAdapter);
        //??????item???????????????
        mDetailList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(),3);
                outRect.bottom = UIUtil.dip2px(view.getContext(),3);
                outRect.right = UIUtil.dip2px(view.getContext(),3);
                outRect.left = UIUtil.dip2px(view.getContext(),3);

            }
        });

        mDetailListAdapter.setItemClickListener(this);
        BezierLayout headerView = new BezierLayout(this);

        mRefreshLayout.setHeaderView(headerView);
        mRefreshLayout.setMaxHeadHeight(140);
        mRefreshLayout.setOverScrollBottomShow(false);
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                BaseApplication.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.finishRefreshing();
                    }
                },1000);
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                //?????????????????????
                if (mAlbumDetailPresenter != null) {
                    mAlbumDetailPresenter.loadMore();
                    mIsLoaderMore = true;
                }
            }
        });
        return detailListView;
    }


    private boolean mIsLoaderMore = false;

    @Override
    public void onDetailListLoaded(List<Track> tracks) {

        if (mIsLoaderMore && mRefreshLayout != null) {
            mRefreshLayout.finishLoadmore();
            mIsLoaderMore = false;
        }

        this.mCurrentTracks = tracks;
        //???????????????????????????????????????UI??????
        if (tracks == null || tracks.size() == 0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }

        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
        //??????/??????UI??????
        mDetailListAdapter.setData(tracks);

    }

    @Override
    public void onNetworkError(int errorCode, String errorMsg) {
        //?????????????????????????????????????????????
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onAlbumLoaded(Album album) {
        this.mCurrentAlbum = album;
        long id = album.getId();
        LogUtil.d(TAG,"album-->" + id);
        mCurremtId = id;

        //???????????????????????????
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) id,mCurrentPage);
        }


        //??????????????????Loading??????
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        if (mAlbumTitle != null) {
            mAlbumTitle.setText(album.getAlbumTitle());
        }

        if (mAlbumAuthor != null) {
            mAlbumAuthor.setText(album.getAnnouncer().getNickname());
        }

        //??????????????????
        if (mLargeCover != null && null != mLargeCover){
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mLargeCover, new Callback() {
                @Override
                public void onSuccess() {
                    Drawable drawable = mLargeCover.getDrawable();
                    if (drawable != null) {
                        //?????????????????????????????????
                        ImageBlur.makeBlur(mLargeCover,DetailActivity.this);
                    }
                }

                @Override
                public void onError() {

                    LogUtil.d(TAG,"onError");
                }
            });
        }


        if (mSmallCover != null) {
            Picasso.with(this).load(album.getCoverUrlLarge()).into(mSmallCover);

        }
    }

    @Override
    public void onLoaderMoreFinish(int size) {
        if (size > 0) {
            Toast.makeText(this, "????????????" + size + "?????????", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRefreshFinisned(int size) {

    }

    @Override
    public void onRetryClick() {
        //??????????????????????????????????????????????????????????????????
        if (mAlbumDetailPresenter != null) {
            mAlbumDetailPresenter.getAlbumDetail((int) mCurremtId,mCurrentPage);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        //????????????????????????
        PlayerPresenter playerPresenter = PlayerPresenter.getPlayerPresenter();
        playerPresenter.setPlayList(detailData,position);
        //?????????????????????
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    /**
     * ???????????????????????????????????????
     * @param playing
     */
    private void updatePlayState(boolean playing) {
        if (mPlayControlBtn != null && mPlayControlTips != null) {
            mPlayControlBtn.setImageResource(playing ? R.drawable.select_play_control_pause : R.drawable.select_play_control_play);
            if (!playing){
                mPlayControlTips.setText(R.string.click_play_tips_text);
            }else {
                if (!TextUtils.isEmpty(mCurrentTrackTitle)){
                    mPlayControlTips.setText(mCurrentTrackTitle);
                }
            }
        }
    }

    @Override
    public void onPlayStart() {
        //??????????????????????????????????????????
        updatePlayState(true);
    }

    @Override
    public void onPlayPause() {
        //???????????????????????????????????????????????????
        updatePlayState(false);
    }



    @Override
    public void onPlayStop() {
        //???????????????????????????????????????????????????
        updatePlayState(false);

    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay(Track track) {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode playMode) {

    }

    @Override
    public void onProgressChange(int currentDuration, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        if (track != null) {
            mCurrentTrackTitle = track.getTrackTitle();
            if (!TextUtils.isEmpty(mCurrentTrackTitle) && mPlayControlTips != null) {
                mPlayControlTips.setText(mCurrentTrackTitle);
            }
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }

    @Override
    public void onAddResult(boolean isSuccess) {
        if (isSuccess) {
            //??????????????????????????????UI???????????????
            mSubBtn.setText(R.string.cancel_sub_tips_text);
        }
        //??????toast
        String tipsText = isSuccess ? "????????????" : "????????????";
        Toast.makeText(this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        if (isSuccess) {
            //??????????????????????????????UI?????????
            mSubBtn.setText(R.string.sub_tips_text);
        }
        //??????toast
        String tipsText = isSuccess ? "????????????" : "????????????";
        Toast.makeText(this, tipsText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionsLoaded(List<Album> albums) {
        //??????????????? ???????????????
    }

    @Override
    public void onSubFull() {
        //?????????????????????toast
        Toast.makeText(this, "????????????????????????" + Constants.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();
    }
}
