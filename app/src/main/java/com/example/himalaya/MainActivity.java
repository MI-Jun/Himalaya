package com.example.himalaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.himalaya.adapters.IndicatorAdapter;
import com.example.himalaya.adapters.MainContentAdapter;
import com.example.himalaya.interfaces.IPlayerCallback;
import com.example.himalaya.presenters.PlayerPresenter;
import com.example.himalaya.presenters.RecommendPresenter;
import com.example.himalaya.utils.LogUtil;
import com.example.himalaya.views.RoundRectImageView;
import com.squareup.picasso.Picasso;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;

public class MainActivity extends FragmentActivity implements IPlayerCallback {

    private static final String TAG = "MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private RoundRectImageView mRoundRectImageView;
    private TextView mHeaderTitle;
    private TextView mSubTitle;
    private ImageView mPlayControl;
    private IndicatorAdapter mIndicatorAdapter;
    private PlayerPresenter mPlayerPresenter;
    private View mPlayControlItem;
    private View mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        initPresenter();


    }

    private void initPresenter() {
        mPlayerPresenter = PlayerPresenter.getPlayerPresenter();
        mPlayerPresenter.registerViewCallback(this);
    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTapClickListener(new IndicatorAdapter.OnIndicatorTapClickListener() {
            @Override
            public void onTabClick(int index) {
                if (mContentPager != null){
                    mContentPager.setCurrentItem(index);
                }
            }
        });

        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerPresenter != null) {
                    boolean hasPlayList = mPlayerPresenter.hasPlayList();
                    if (!hasPlayList) {
                        //没有设置过播放列表，我们就播放默认的第一个推荐专辑
                        //第一个推荐专辑，每天都会变
                        playFirstRecommend();
                    } else{
                        if (mPlayerPresenter.isPlaying()) {
                            mPlayerPresenter.pause();
                        }else{
                            mPlayerPresenter.play();
                        }
                    }


                }
            }
        });

        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasPlayList = mPlayerPresenter.hasPlayList();
                if (!hasPlayList) {
                    playFirstRecommend();
                }
                //跳转到播放器界面
                startActivity(new Intent(MainActivity.this,PlayerActivity.class));
            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 播放第一个推荐的内容
     */
    private void playFirstRecommend() {
        List<Album> currentRecommend = RecommendPresenter.getInstance().getCurrentRecommend();
        if (currentRecommend != null && currentRecommend.size() > 0) {
            Album album = currentRecommend.get(0);
            long albumId = album.getId();
            mPlayerPresenter.playByAlbumId(albumId);

        }
    }

    private void initView() {
        mMagicIndicator = findViewById(R.id.main_indicator);
        mMagicIndicator.setBackgroundColor(ContextCompat.getColor(this,R.color.main_color));

        //创建indicator的适配器
        mIndicatorAdapter = new IndicatorAdapter(this);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(mIndicatorAdapter);

        //ViewPager
        mContentPager = findViewById(R.id.content_pager);

        //创建内容适配器
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        MainContentAdapter mainContentAdapter = new MainContentAdapter(supportFragmentManager);

        mContentPager.setAdapter(mainContentAdapter);

        //把ViewPager和indicator绑定在一起
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, (ViewPager) mContentPager);

        //播放控制相关
        mRoundRectImageView = this.findViewById(R.id.main_track_cover);
        mHeaderTitle = this.findViewById(R.id.main_head_title);
        mHeaderTitle.setSelected(true);
        mSubTitle = this.findViewById(R.id.main_sub_title);
        mPlayControl = this.findViewById(R.id.mian_play_control);
        mPlayControlItem = findViewById(R.id.mian_play_control_item);
        //搜索
        mSearchBtn = this.findViewById(R.id.search_btn);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPresenter != null){
            mPlayerPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onPlayStart() {
        updatePlayControl(true);
    }

    private void updatePlayControl(boolean isPlaying){
        if (mPlayControl != null) {
            mPlayControl.setImageResource(isPlaying ? R.drawable.select_player_pause : R.drawable.select_player_play);
        }
    }

    @Override
    public void onPlayPause() {
        updatePlayControl(false);

    }

    @Override
    public void onPlayStop() {
        updatePlayControl(false);

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
            String trackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrlMiddle = track.getCoverUrlMiddle();
            LogUtil.d(TAG,"trackTitle --- >" + trackTitle);
            if (mHeaderTitle != null) {
                mHeaderTitle.setText(trackTitle);

            }
            LogUtil.d(TAG,"nickname --- >" + nickname);
            if (mSubTitle != null) {
                mSubTitle.setText(nickname);
            }
            LogUtil.d(TAG,"coverUrlMiddle --- >" + coverUrlMiddle);
            Picasso.with(this).load(coverUrlMiddle).into(mRoundRectImageView);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}