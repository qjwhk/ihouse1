package com.lierda.kesi.ihouse.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.lechange.opensdk.listener.LCOpenSDK_DownloadListener;
import com.lechange.opensdk.media.LCOpenSDK_Download;
import com.lierda.kesi.ihouse.R;
import com.lierda.kesi.ihouse.business.Business;
import com.lierda.kesi.ihouse.business.entity.RecordInfo;
import com.lierda.kesi.ihouse.constants.CommonTitle;
import com.lierda.kesi.ihouse.constants.DatePicker;
import com.lierda.kesi.ihouse.constants.ProgressDialog;
import com.lierda.kesi.ihouse.util.ImageHelper;
import com.lierda.kesi.ihouse.util.MediaPlayHelper;
import com.lierda.kesi.ihouse.util.TaskPoolHelper;
import com.lierda.kesi.ihouse.util.TimeHelper;
import com.lierda.kesi.ihouse.constants.DatePicker.OnTimeClickListener;
import com.lierda.kesi.ihouse.business.Business.CloudStorageCode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecordListActivity extends FragmentActivity {
    private final String tag = "RecordList";

    private ListView mListview = null;
    private CommonTitle mCommonTitle;
    private DatePicker mDatePicker;
    private LinearLayout mViewContainer; 	   // 放置DatePicker的容器
    private ProgressDialog mProgressDialog;    //请求加载使用
    private RecrodListAdapter mRecordListAdapt;
    private List<RecordInfo> mRecordList;
    private String mChannelUUID = null;
    private int mType;
    private int mIndex = -1;				   //当前正在下载的索引号,目前仅支持单个下载，日后会扩展多个下载
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int firstIndex = mListview.getFirstVisiblePosition();
            int lastIndex = mListview.getLastVisiblePosition();
            for (int i = firstIndex; i <= lastIndex; i++) {
                ViewHolder holder = (ViewHolder)mListview.getChildAt(i - firstIndex).getTag();
                //渲染下载更新
                android.view.ViewGroup.LayoutParams params = holder.mDownload_bg.getLayoutParams();
                if (holder.mInfo.isDownload()) {
                    if (holder.mInfo.getDownLength() > 0) {
                        params.width = (int)(holder.mDownload_icon.getWidth() / (holder.mInfo.getFileLength() / holder.mInfo.getDownLength()));
                    }
                } else {
                    params.width = 0;
                    holder.mDownload_icon.setText("下载");
                }
                holder.mDownload_bg.setLayoutParams(params);
            }
            sendEmptyMessageDelayed(0, 1000);
        };
    }; //定时器，每500ms刷新一次，


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        Intent intent = getIntent();
        mChannelUUID = intent.getStringExtra("UUID");
        mType = intent.getIntExtra("TYPE", 0);
        Log.d(tag, mType + "::mChannelUUID:"+mChannelUUID);

        //绘制标题
        mCommonTitle = (CommonTitle) findViewById(R.id.title);
        mCommonTitle.initView(R.drawable.title_btn_back, R.drawable.title_btn_search, intent.getIntExtra("MEDIA_TITLE", 0));

        mCommonTitle.setOnTitleClickListener(new CommonTitle.OnTitleClickListener() {
            @Override
            public void onCommonTitleClick(int id) {
                // TODO Auto-generated method stub
                switch (id) {
                    case CommonTitle.ID_LEFT:
                        finish();
                        break;
                    case CommonTitle.ID_RIGHT:
                        //添加时间选择控件
                        Log.d("Business","Business" + mViewContainer.getChildCount());
                        if(mViewContainer.getChildCount()>0){
                            return;
                        }
                        // undo 添加datepicker
                        if (mDatePicker == null) {
                            mDatePicker = new DatePicker(getApplicationContext());
                            initDatePicker();
                        }
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                        mViewContainer.addView(mDatePicker, lp);
                        break;
                    default:
                        break;
                }
            }
        });

        //日期控件
        mViewContainer = (LinearLayout) findViewById(R.id.timerContainer);

        //开启请求加载控件
        mProgressDialog = (ProgressDialog) this.findViewById(R.id.query_load);
        mProgressDialog.setStart(getString(R.string.common_loading));

        //绘制list
        mListview = (ListView)this.findViewById(R.id.list_records);
        mRecordListAdapt = new RecrodListAdapter(this);
        mListview.setAdapter(mRecordListAdapt);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String startTime = df.format(new Date()) + " 00:00:00";
        String endTime = df.format(new Date()) + " 23:59:59";
        //初始化数据
        loadRecrodList(startTime,endTime);
        //设置监听
        setItemClick();

        //设置云录像下载监听
        if (mType == MediaPlayActivity.IS_VIDEO_REMOTE_CLOUD_RECORD) {
            LCOpenSDK_Download.setListener(new LCOpenSDK_DownloadListener(){
                @Override
                public void onDownloadReceiveData(int index, int dataLen) {
                    // TODO Auto-generated method stub
                    if (mRecordList.size() != 0) {
                        RecordInfo info = mRecordList.get((int)index);
                        info.setDownLength(info.getDownLength() + dataLen);
                    }
                    //Log.d(tag, info.getDownLength() + " :::: " + info.getFileLength());
                }

                @Override
                public void onDownloadState(final int index, String message, int Type) {
                    // TODO Auto-generated method stub
                    if (Type == Business.RESULT_SOURCE_OPENAPI
                            || message.equals(CloudStorageCode.HLS_DOWNLOAD_FAILD)
                            || message.equals(CloudStorageCode.HLS_SEEK_FAILD)
                            || message.equals(CloudStorageCode.HLS_KEY_ERROR)) {
                        //重置为可以下载状态
                        mRecordList.get((int) index).setDownLength(-1);
                        if (mHandler != null) {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(RecordListActivity.this, "下载失败/秘钥错误,index : " + index, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        //删除下载到一半的文件
                        MediaPlayHelper.deleteDownloadVideo(mRecordList.get(index).getRecordID(), mRecordList.get(index).getStartTime());
                        //屏蔽操作
                        mIndex = -1;

                    }
                    if (message.equals(CloudStorageCode.HLS_DOWNLOAD_END)) {
                        //重置为可以下载状态
                        mRecordList.get((int) index).setDownLength(-1);
                        if (mHandler != null) {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(RecordListActivity.this, "下载结束,index : " + index, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        //通知图库刷新
                        MediaScannerConnection.scanFile(RecordListActivity.this,
                                new String[] { MediaPlayHelper.getDownloadVideoPath(mRecordList.get(index).getRecordID(), mRecordList.get(index).getStartTime()) }, null, null);
                        //屏蔽操作
                        mIndex = -1;
                    }
                }
            });
            //开启定时刷新
            mHandler.obtainMessage().sendToTarget();
        }

    }

    public void initDatePicker() {
        if (mDatePicker == null) {
            return;
        }

        mDatePicker.setOnTimeClickListener(new OnTimeClickListener() {
            @Override
            public void onCommonTimeClick(int id) {
                if (id == DatePicker.ID_LEFT) { // 点击左边
                    mViewContainer.removeView(mDatePicker);
                } else { // 点击右边

                    if (mIndex != -1) {
                        LCOpenSDK_Download.stopDownload(mIndex); //重新加载前停止下载
                        MediaPlayHelper.deleteDownloadVideo(mRecordList.get(mIndex).getRecordID(), mRecordList.get(mIndex).getStartTime());
                        //屏蔽操作
                        mIndex = -1;
                    }

                    Date time = mDatePicker.getSelectedDate();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String date = df.format(time);

                    String startTime = date + " 00:00:00";
                    String endTime   = date + " 23:59:59";

                    mViewContainer.removeView(mDatePicker);
                    loadRecrodList(startTime, endTime);
                    //清空屏幕
                    if(mRecordList != null)
                        mRecordList.clear(); //清数据 临时使用
                    mRecordListAdapt.notifyDataSetChanged();

                    RecordListActivity.this.findViewById(R.id.list_records_novideo).setVisibility(View.GONE);
                    mProgressDialog.setStart(getString(R.string.common_loading));
                }

            }
        });
    }

    public void setItemClick(){
        //单个录像监听
        mListview.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position,
                                    long id) {
                // TODO Auto-generated method stub
                if (mIndex != -1) {
                    Toast.makeText(RecordListActivity.this, "目前仅支持一个播放/任务下载", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(tag, "click:"+ position+"-" + id + "ID:" + view.getId());
                Intent intent = new Intent(RecordListActivity.this, MediaPlayActivity.class);
                switch(mType){
                    case MediaPlayActivity.IS_VIDEO_REMOTE_RECORD:
                        intent.putExtra("TYPE", MediaPlayActivity.IS_VIDEO_REMOTE_RECORD);
                        break;
                    case MediaPlayActivity.IS_VIDEO_REMOTE_CLOUD_RECORD:
                        intent.putExtra("TYPE", MediaPlayActivity.IS_VIDEO_REMOTE_CLOUD_RECORD);
                        break;
                    default:
                        break;
                }
                intent.putExtra("ID", mRecordList.get(position).getId());
                intent.putExtra("MEDIA_TITLE", R.string.record_play_name);
                RecordListActivity.this.startActivity(intent);
            }

        });
    }



    public void loadRecrodList(final String startTime, final String endTime){
        switch(mType){
            case MediaPlayActivity.IS_VIDEO_REMOTE_RECORD:
                //查询1天之内的后10条录像
                Business.getInstance().queryRecordNum(mChannelUUID, startTime,
                        endTime, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what != 0) {
                                    mProgressDialog.setStop();  //关闭加载窗口
                                    Toast.makeText(RecordListActivity.this, "查询录像失败，错误码：" + msg.what, Toast.LENGTH_SHORT).show();
                                } else if (msg.what == 0) {
                                    if(msg.arg1 > 0){
                                        Business.getInstance().queryRecordList(mChannelUUID, startTime,
                                                endTime, msg.arg2, msg.arg1, new Handler() {
                                                    @SuppressWarnings("unchecked")
                                                    @Override
                                                    public void handleMessage(Message msg) {
                                                        mProgressDialog.setStop();  //关闭加载窗口
                                                        if (msg.what != 0) {
                                                            Toast.makeText(RecordListActivity.this, "查询录像失败，错误码：" + msg.obj, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            mRecordList = (List<RecordInfo>) msg.obj;
                                                            if (mRecordList != null && mRecordList.size() > 0) {
                                                                Log.d(tag,"loadRecrodList mRecordList.size:"+mRecordList.size());
                                                                mRecordListAdapt.notifyDataSetChanged();
                                                            }else {
                                                                RecordListActivity.this.findViewById(R.id.list_records_novideo).setVisibility(View.VISIBLE);
                                                                //Toast.makeText(RecordListActivity.this, "没有录像", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                    } else {
                                        mProgressDialog.setStop();  //关闭加载窗口
                                        RecordListActivity.this.findViewById(R.id.list_records_novideo).setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                break;
            case MediaPlayActivity.IS_VIDEO_REMOTE_CLOUD_RECORD:
                //查询1天之内的前10条录像
                Business.getInstance().queryCloudRecordNum(mChannelUUID, startTime,
                        endTime, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg.what != 0) {
                                    mProgressDialog.setStop();  //关闭加载窗口
                                    Toast.makeText(RecordListActivity.this, "查询录像失败，错误码：" + msg.what, Toast.LENGTH_SHORT).show();
                                } else if (msg.what == 0) {
                                    if(msg.arg1 > 0){
                                        Business.getInstance().queryCloudRecordList(mChannelUUID, startTime,
                                                endTime, msg.arg2, msg.arg1, new Handler() {
                                                    @Override
                                                    public void handleMessage(Message msg) {
                                                        mProgressDialog.setStop(); //关闭加载窗口
                                                        if (msg.what != 0) {
                                                            Toast.makeText(RecordListActivity.this, "查询录像失败，错误码：" + msg.arg1, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            mRecordList = (List<RecordInfo>) msg.obj;
                                                            if (mRecordList != null && mRecordList.size() > 0) {
                                                                Log.d(tag,"loadRecrodList mRecordList.size:"+mRecordList.size());
                                                                mRecordListAdapt.notifyDataSetChanged();
                                                            }else {
                                                                RecordListActivity.this.findViewById(R.id.list_records_novideo).setVisibility(View.VISIBLE);
                                                                //Toast.makeText(RecordListActivity.this, "没有录像", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                    }else {
                                        mProgressDialog.setStop();  //关闭加载窗口
                                        RecordListActivity.this.findViewById(R.id.list_records_novideo).setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mIndex != -1) {
            LCOpenSDK_Download.stopDownload(mIndex);
            MediaPlayHelper.deleteDownloadVideo(mRecordList.get(mIndex).getRecordID(), mRecordList.get(mIndex).getStartTime());
            mRecordList.get(mIndex).setDownLength(-1);
            //屏蔽操作
            mIndex = -1;
        }
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        TaskPoolHelper.clearTask();
    }

    private class RecrodListAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        public RecrodListAdapter(Context context){
            mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return mRecordList != null ? mRecordList.size():0;
        }

        @Override
        public RecordInfo getItem(int position) {
            return mRecordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if(convertView == null){
                convertView = mInflater.inflate(R.layout.activity_record_list_item, null);
                holder = new ViewHolder();
                holder.mBgVideo = (RelativeLayout) convertView.findViewById(R.id.list_bg_video);
                holder.mRecordTime = (TextView) convertView.findViewById(R.id.list_record_time);
                //云录像加载下载按钮
                if (mType == MediaPlayActivity.IS_VIDEO_REMOTE_CLOUD_RECORD) {
                    holder.mDownload = (FrameLayout) convertView.findViewById(R.id.list_record_download);
                    holder.mDownload_bg = convertView.findViewById(R.id.record_download_bg);
                    holder.mDownload_icon = (TextView) convertView.findViewById(R.id.record_download_icon);
                    holder.mDownload.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            if (mIndex == -1 || mIndex == holder.mPosition)
                                if (holder.mDownload_icon.getText().toString().equals("下载")) {
                                    //置为可以取消状态
                                    holder.mDownload_icon.setText("取消");
                                    holder.mInfo.setDownLength(0);
                                    LCOpenSDK_Download.startDownload(holder.mPosition,
                                            MediaPlayHelper.getDownloadVideoPath(holder.mInfo.getRecordID(), holder.mInfo.getStartTime()),
                                            Business.getInstance().getToken(), holder.mInfo.getRecordID(), holder.mInfo.getDeviceId(), holder.mInfo.getDeviceKey() != null ? holder.mInfo.getDeviceKey() : holder.mInfo.getDeviceId(), 0, 5000);
                                    //屏蔽操作
                                    mIndex = holder.mPosition;
                                } else {
                                    //置为可以下载状态
                                    android.view.ViewGroup.LayoutParams params = holder.mDownload_bg.getLayoutParams();
                                    params.width = 0;
                                    holder.mDownload_bg.setLayoutParams(params);
                                    holder.mDownload_icon.setText("下载");
                                    holder.mInfo.setDownLength(-1);
                                    LCOpenSDK_Download.stopDownload(holder.mPosition);

                                    //删除文件
                                    MediaPlayHelper.deleteDownloadVideo(holder.mInfo.getRecordID(), holder.mInfo.getStartTime());
                                    //屏蔽操作
                                    mIndex = -1;
                                }
                            else
                                Toast.makeText(RecordListActivity.this, "目前仅支持一个播放/任务下载", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            //修改数据信息
            holder.mPosition = position;
            holder.mInfo = getItem(position);
            //Log.d(tag, "index : " + position);

            switch (mType) {
                case MediaPlayActivity.IS_VIDEO_REMOTE_RECORD:
                    //加载背景图片,设备录像，不用解密
                    holder.mBgVideo.setBackgroundResource(R.drawable.list_bg_video);
                    if(holder.mInfo.getBackgroudImgUrl() != null && holder.mInfo.getBackgroudImgUrl().length() > 0){
                        //下载
                        ImageHelper.loadCacheImage(holder.mInfo.getBackgroudImgUrl(), new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                // TODO Auto-generated method stub
                                super.handleMessage(msg);
                                if(holder.mInfo.getBackgroudImgUrl().hashCode() == msg.what && msg.obj != null){
                                    holder.mBgVideo.setBackgroundDrawable((Drawable)msg.obj);
                                }
                            }
                        });
                    }
                    break;
                case MediaPlayActivity.IS_VIDEO_REMOTE_CLOUD_RECORD:
                    // 屏蔽云台返回的录像大小=0情况
                    if (holder.mInfo.getFileLength() != 0) {
                        holder.mDownload.setVisibility(View.VISIBLE);
                    } else {
                        holder.mDownload.setVisibility(View.INVISIBLE);
                    }

                    android.view.ViewGroup.LayoutParams params = holder.mDownload_bg.getLayoutParams();
                    //渲染下载更新
                    if (holder.mInfo.isDownload()) {
                        holder.mDownload_icon.setText("取消");
                        if (holder.mInfo.getDownLength() > 0) {
                            params.width = (int)(holder.mDownload_icon.getWidth() / (holder.mInfo.getFileLength() / holder.mInfo.getDownLength()));
                        } else {
                            params.width = 0;
                        }
                    } else {
                        holder.mDownload_icon.setText("下载");
                        params.width = 0;
                    }
                    holder.mDownload_bg.setLayoutParams(params);
                    //加载背景图片,云录像,要解密
                    holder.mBgVideo.setBackgroundResource(R.drawable.list_bg_device);
                    if(holder.mInfo.getBackgroudImgUrl() != null && holder.mInfo.getBackgroudImgUrl().length() > 0){
                        //下载
                        ImageHelper.loadCacheImage(holder.mInfo.getBackgroudImgUrl(), holder.mInfo.getDeviceKey() != null ? holder.mInfo.getDeviceKey() : holder.mInfo.getDeviceId(), new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                // TODO Auto-generated method stub
                                super.handleMessage(msg);
                                if(holder.mInfo.getBackgroudImgUrl().hashCode() == msg.what && msg.obj != null){
                                    holder.mBgVideo.setBackgroundDrawable((Drawable)msg.obj);
                                }
                            }
                        });
                    }
                    break;
                default:
                    break;
            }

            //时间
            holder.mRecordTime.setText(TimeHelper.getDateHMS(holder.mInfo.getStartTime())+"--"+TimeHelper.getDateHMS(holder.mInfo.getEndTime()));

            return convertView;
        }
    }
    static class ViewHolder{
        int				mPosition;
        RelativeLayout mBgVideo;
        TextView mRecordTime;
        RecordInfo      mInfo;
        FrameLayout mDownload;
        View mDownload_bg;
        TextView mDownload_icon;
    }

}
