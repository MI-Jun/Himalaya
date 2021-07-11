package com.example.himalaya.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.himalaya.R;
import com.example.himalaya.adapters.PlayListAdapter;
import com.example.himalaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class SobPopWindow extends PopupWindow {

    private final View mPopView;
    private View mCloseBtn;
    private RecyclerView mTracksList;
    private PlayListAdapter mPlayListAdapter;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PlayListActionListener mPlayModeClickListener = null;
    private View mOrderContainer;
    private ImageView mOrderIcon;
    private TextView mOrderText;

    public SobPopWindow(){
        //设置它宽高
        super( ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载进来View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list, null);
        //设置内容
        setContentView(mPopView);
        //设置窗口进入和退出的动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        initEven();
    }



    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        //找到控件
        mTracksList = mPopView.findViewById(R.id.play_list_rv);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(BaseApplication.getAppContext());
        mTracksList.setLayoutManager(layoutManager);
        //设置适配器
        mPlayListAdapter = new PlayListAdapter();
        mTracksList.setAdapter(mPlayListAdapter);
        //播放模式相关
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        mOrderContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);

    }

    private void initEven() {
        //点击关闭以后，窗口消失
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobPopWindow.this.dismiss();
            }
        });

        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onPlayModeClick();
                }
            }
        });

        mOrderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放列表为顺序或者逆序
                mPlayModeClickListener.onOrderClick();
            }
        });

    }

    /**
     * 给适配器设置数据
     * @param data
     */
    public void setListData(List<Track> data){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);
        }
    }

    public void setCurrentPlayPosition(int position){
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(position);
            mTracksList.scrollToPosition(position);
        }
    }

    public void setPlayListItemClickerListener(PlayListItemClickListener listener){
        mPlayListAdapter.setOnItemClickListener(listener);
    }

    /**
     * 更新列表的播放模式
     * @param currentMode
     */
    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
        updatePlayModeBtnImg(currentMode);
    }

    /**
     * 更新切换列表顺序和逆序的按钮和文字更新
     * @param isReverse
     */
    public void updateOrderIcon(boolean isReverse){
        mOrderIcon.setImageResource(isReverse ? R.drawable.select_play_mode_list_order : R.drawable.select_play_mode_list_revers);
        mOrderText.setText(isReverse? "顺序" : "逆序");

    }


    /**
     * 根据当前的状态，更新播放模式图标
     *
     */
    private void updatePlayModeBtnImg(XmPlayListControl.PlayMode  playMode) {
        int resId = R.drawable.select_play_mode_list_order;
        int textID = R.string.play_mode_order_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.select_play_mode_list_order;
                textID = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.select_play_mode_random;
                textID = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.select_play_mode_list_order_looper;
                textID = R.string.play_mode_list_play_text;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.select_play_mode_single_loop;
                textID = R.string.play_mode_single_play_text;
                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textID);
    }

    public interface PlayListItemClickListener{
        void onItemClick(int position);
    }

    public void setPlayListActionListener(PlayListActionListener playModeListener){
        mPlayModeClickListener = playModeListener;
    }

    public interface PlayListActionListener {

        //播放模式被点击了
        void onPlayModeClick();

        //播放逆序或者顺序切换按钮被点击了
        void onOrderClick();
    }
}

