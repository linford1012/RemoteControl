package com.yf.remotecontrolclient.activity.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.yf.remotecontrolclient.App;
import com.yf.remotecontrolclient.CommonConstant;
import com.yf.remotecontrolclient.MediaPlayMode;
import com.yf.remotecontrolclient.R;
import com.yf.remotecontrolclient.activity.MediaMusicActivity;
import com.yf.remotecontrolclient.activity.view.RefreshListView;
import com.yf.remotecontrolclient.adapt.MusicAdapter;
import com.yf.remotecontrolclient.domain.GetSongStuatus;
import com.yf.remotecontrolclient.domain.Setmode;
import com.yf.remotecontrolclient.domain.SetplaysongProgress;
import com.yf.remotecontrolclient.domain.Setplaysongid;
import com.yf.remotecontrolclient.domain.Setplaystatus;
import com.yf.remotecontrolclient.domain.Setvolumeadd;
import com.yf.remotecontrolclient.domain.Song;
import com.yf.remotecontrolclient.domain.SongList;
import com.yf.remotecontrolclient.service.MusicBusinessService;
import com.yf.remotecontrolclient.service.imp.MusicBusinessServiceImpl;
import com.yf.remotecontrolclient.util.JsonAssistant;
import com.yf.remotecontrolclient.util.SpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sujuntao on 2017/8/17 .
 */

public class MediaMusicRemotListFragment extends Fragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener ,RefreshListView.OnRefreshListener,AdapterView.OnItemClickListener{
    private static String TAG="MediaMusicRemotListFragment";
    //fragment的大View
    private View mView;
    //绑定的activity实例
    private Activity mActivity;

    private JsonAssistant jsonAssistant = new JsonAssistant();
    private RefreshListView listview;
    private MusicAdapter mMusicAdapter;
    private int total;
    private MusicBusinessService musicBusinessService;
    public static SongList songList;
    private List<Song> songs = new ArrayList<Song>();
    private ImageView volumeMinus;
    private ImageView previous;
    private ImageView startPause;
    private ImageView next;
    private ImageView repeatMode;
    private ImageView volumeAdd;
    private SeekBar sbProgess;

    private Integer progress;
    private Integer maxProgress;
    private TimerTask tt=null;
    //进度timer
    Timer progessTime =null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra(MusicBusinessServiceImpl.CMD);
            if (cmd.equals("BSgetsonglist")) {
                songList = (SongList) intent
                        .getSerializableExtra("BSgetsonglist");
                if (songList == null) {
                    return;
                }
                total = songList.getTotal();
                List<Song> list = songList.getSongList();
                songs.addAll(list);
                Message message = Message.obtain();
                handler.sendEmptyMessage(0);
            } else if (cmd.equals("BSsetplaystatus")) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = intent.getSerializableExtra("setplaystatus");
                handler.sendMessage(message);
            } else if (cmd.equals("BSsetmode")) {
                Message message = Message.obtain();
                message.what = 2;
                message.obj = intent.getSerializableExtra("setmode");
                handler.sendMessage(message);
            } else if (cmd.equals("BSgetsongstatus")) {
                Log.i(TAG,"BSgetsongstatus");
                handler.sendEmptyMessage(3);
            }
        }
    };
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mMusicAdapter.notifyDataSetChanged();
                    listview.onRefreshComplete();
                    break;
                case 1:
                    Setplaystatus setplaystatus = (Setplaystatus) msg.obj;
                    String status = setplaystatus.getStatus();
//				client收到：{"cmd":"BSsetplaystatus","status":"play"}
//				client收到：{"cmd":"BSsetplaystatus","status":"stop"}
                    if (status.equals("play")) {
                        startPause.setImageResource(R.drawable.video_button_pause);
                    } else {
                        startPause.setImageResource(R.drawable.video_button_play);
                    }
                    break;
                case 2:
                    Setmode setmode = (Setmode) msg.obj;
                    switch (setmode.getMode()) {
                        case MediaPlayMode.RANDOM_PLAY_MODE:
                            repeatMode.setImageResource(R.drawable.et_media_mode_random_n);
                            break;
                        case MediaPlayMode.ALL_PLAY_MODE:
                            repeatMode.setImageResource(R.drawable.et_media_mode_circulate_n);
                            break;
                        case MediaPlayMode.SINGLE_PLAY_MODE:
                            repeatMode.setImageResource(R.drawable.et_media_mode_single_n);
                            break;
                    }
                    break;
                case 3:
                    String data = SpUtil.getString(mActivity, CommonConstant.GET_SONG_STATUS_KEY, null);

                    if (TextUtils.isEmpty(data)) {
                        return;
                    }
                    GetSongStuatus getSongStuatus = jsonAssistant.paseGetsongstatus(data);
                    if (getSongStuatus == null) {
                        return;
                    }
                    if (!TextUtils.isEmpty(getSongStuatus.getPlaystatus())) {
                        if (getSongStuatus.getPlaystatus().equals("play")) {
                            startPause.setImageResource(R.drawable.video_button_pause);
                        } else {
                            startPause.setImageResource(R.drawable.video_button_play);
                        }
                    }
                    if (getSongStuatus.getPlaymode() != null) {
                        switch (getSongStuatus.getPlaymode()) {
                            case MediaPlayMode.RANDOM_PLAY_MODE:
                                repeatMode.setImageResource(R.drawable.et_media_mode_random_n);
                                break;
                            case MediaPlayMode.ALL_PLAY_MODE:
                                repeatMode.setImageResource(R.drawable.et_media_mode_circulate_n);
                                break;
                            case MediaPlayMode.SINGLE_PLAY_MODE:
                                repeatMode.setImageResource(R.drawable.et_media_mode_single_n);
                                break;
                        }
                    }

                    if (getSongStuatus.getDuration() != null) {
                        maxProgress=getSongStuatus.getDuration().intValue();
                        sbProgess.setMax(getSongStuatus.getDuration().intValue());
                    }
                    if (getSongStuatus.getPlayprogress() != null && getSongStuatus.getPlaystatus().equals("play")) {
                        progress = getSongStuatus.getPlayprogress();
                        sbProgess.setProgress(progress);
                        stopTimer(getSongStuatus);
                        startTimer(getSongStuatus);
                    }else{
                        stopTimer(getSongStuatus);
                    }
                    break;
                case 4:
                    progress+=1000;
                    sbProgess.setProgress(progress);
//                    if(progress>=maxProgress){
//                        //请求下进度
//                        GetSongStuatus getSongStuatus1=new GetSongStuatus();
//                        getSongStuatus1.setCmd("BSgetsongstatus");
//                        //musicBusinessService.sendBsgetSongstatus(getSongStuatus1);
//                    }
                    break;
            }
        }
    };

    private void startTimer(GetSongStuatus getSongStuatus){
        if (progessTime == null) {
            progessTime = new Timer();
        }

        if (tt == null) {
            tt = new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(4);
                }
            };
        }
        progress=getSongStuatus.getPlayprogress().intValue();
        if(progessTime != null && tt != null )
            progessTime.schedule(tt, 1000, 1000);
    }

    private void stopTimer(GetSongStuatus getSongStuatus){
        if (progessTime != null) {
            progessTime.cancel();
            progessTime = null;
        }
        if (tt != null) {
            tt.cancel();
            tt = null;
        }
        progress=0;
    }

    @Override
    public void onDestroy() {
//        mActivity.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void initView() {
        volumeMinus = (ImageView) mView.findViewById(R.id.volume_minus);
        volumeMinus.setOnClickListener(this);
        previous = (ImageView) mView.findViewById(R.id.previous);
        previous.setOnClickListener(this);
        startPause = (ImageView) mView.findViewById(R.id.start_pause);
        startPause.setOnClickListener(this);
        next = (ImageView) mView.findViewById(R.id.next);
        next.setOnClickListener(this);
        repeatMode = (ImageView) mView.findViewById(R.id.repeat_mode);
        repeatMode.setOnClickListener(this);
        volumeAdd = (ImageView) mView.findViewById(R.id.volume_add);
        volumeAdd.setOnClickListener(this);
        sbProgess = (SeekBar) mView.findViewById(R.id.sb_progess);
        sbProgess.setOnSeekBarChangeListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaMusicActivity.MBROADCASTRECEIVER);
        mActivity.registerReceiver(mBroadcastReceiver, filter);
        listview = (RefreshListView) mView.findViewById(R.id.lv_rf_mc_rl);
        listview.setRefreshListener(this);
        listview.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.volume_minus:
                //声音
                Setvolumeadd setvolumeadd = new Setvolumeadd();
                setvolumeadd.setCmd("BSsetvolumeadd");
                setvolumeadd.setValume("-");
                musicBusinessService.sendBssetvolumeadd(setvolumeadd);
                break;
            case R.id.previous:
                //上一首
                Setplaystatus setplaystatus = new Setplaystatus();
                setplaystatus.setCmd("BSsetplaystatus");
                setplaystatus.setStatus("previous");
                musicBusinessService.sendBssetplaystatus(setplaystatus);
                break;
            case R.id.start_pause:
                Setplaystatus setplaystatus1 = new Setplaystatus();
                setplaystatus1.setCmd("BSsetplaystatus");
                setplaystatus1.setStatus("start_pause");
                musicBusinessService.sendBssetplaystatus(setplaystatus1);
                break;
            case R.id.next:
                //下一首
                Setplaystatus setplaystatus2 = new Setplaystatus();
                setplaystatus2.setCmd("BSsetplaystatus");
                setplaystatus2.setStatus("next");
                musicBusinessService.sendBssetplaystatus(setplaystatus2);
                break;
            case R.id.repeat_mode:
                senMode();
                break;
            case R.id.volume_add:
                Setvolumeadd setvolumeadd1 = new Setvolumeadd();
                setvolumeadd1.setCmd("BSsetvolumeadd");
                setvolumeadd1.setValume("+");
                musicBusinessService.sendBssetvolumeadd(setvolumeadd1);
                break;
        }
    }

    private void senMode() {
        Setmode setmode = new Setmode();
        setmode.setCmd("BSsetmode");

        if (repeatMode.getDrawable().getCurrent().getConstantState().
                equals(ContextCompat.getDrawable(App.getAppContext(), R.drawable.et_media_mode_circulate_n).getConstantState())) {
            repeatMode.setImageResource(R.drawable.et_media_mode_random_n);
            //发送随机模式
            setmode.setMode(MediaPlayMode.RANDOM_PLAY_MODE);
        } else if (repeatMode.getDrawable().getCurrent().getConstantState().
                equals(ContextCompat.getDrawable(App.getAppContext(), R.drawable.et_media_mode_random_n).getConstantState())) {
            repeatMode.setImageResource(R.drawable.et_media_mode_single_n);
            //发送单曲循环模式
            setmode.setMode(MediaPlayMode.SINGLE_PLAY_MODE);
        } else if (repeatMode.getDrawable().getCurrent().getConstantState().
                equals(ContextCompat.getDrawable(App.getAppContext(), R.drawable.et_media_mode_single_n).getConstantState())) {
            //发送全部循环播放模式
            repeatMode.setImageResource(R.drawable.et_media_mode_circulate_n);
            setmode.setMode(MediaPlayMode.ALL_PLAY_MODE);
        }
        musicBusinessService.sendMediaMode(setmode);
    }

    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            SetplaysongProgress setplaysongProgress=new SetplaysongProgress();
            setplaysongProgress.setCmd("BSsetplaysongProgress");
            setplaysongProgress.setSongprogress(progress);
            this.progress=progress;
            musicBusinessService.sendSetplaysongProgress(setplaysongProgress);
        }
    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView=inflater.inflate(R.layout.fragment_music_remot_list, null);
        this.mActivity=getActivity();
        initView();
        initData();
        return mView;
    }

    /**
     * oncreate执行完
     * @param savedInstanceState
     */
    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initData() {
        mMusicAdapter = new MusicAdapter();
        mMusicAdapter.setSongs(songs);
        listview.setAdapter(mMusicAdapter);
        musicBusinessService = new MusicBusinessServiceImpl();
        getSongList();
    }

    private void getSongList(){
        SongList songList = new SongList();
        songList.setCmd("BSgetsonglist");
        songList.setPageSize(10);
        songList.setPageIndex(Math.max(songs.size(), 0));
        musicBusinessService.sendBsgetSongList(songList);
    }

    @Override
    public void onRefresh() {
        //下拉刷新数据
        songs.clear();
        mMusicAdapter.notifyDataSetChanged();
        // 获取音乐列表
        SongList songList = new SongList();
        songList.setCmd("BSgetsonglist");
        songList.setPageSize(10);
        songList.setPageIndex(Math.max(songs.size() - 1, 0));
        musicBusinessService.sendBsgetSongList(songList);
    }

    @Override
    public void onLoadMore() {
        if (songs.size() != 0 && songs.size() > total) {
            listview.onRefreshComplete();
            return;
        }
        //加载数据
        SongList songList = new SongList();
        songList.setCmd("BSgetsonglist");
        songList.setPageSize(10);
        songList.setPageIndex(Math.max(songs.size(), 0));
        musicBusinessService.sendBsgetSongList(songList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        //播放那一首歌
        Setplaysongid setplaysongid = new Setplaysongid();
        setplaysongid.setCmd("BSsetplaysongid");
        setplaysongid.setSongid(position - 1);
        musicBusinessService.sendBssetplaysongid(setplaysongid);
    }
}
