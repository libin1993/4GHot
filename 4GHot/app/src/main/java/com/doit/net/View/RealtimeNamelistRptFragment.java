package com.doit.net.View;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.doit.net.Activity.GameApplication;
import com.doit.net.Base.BaseFragment;
import com.doit.net.Bean.BlackNameBean;
import com.doit.net.Bean.UeidBean;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.IHandlerFinish;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.StringUtils;
import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.ucsi.R;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zxc on 2018/11/21.
 */

public class RealtimeNamelistRptFragment extends BaseFragment implements IHandlerFinish, EventAdapter.EventCall {
    private View rootView = null;
    private ListView lvRealTimeNum;
    private RealtimeNamelistRptAdapter mAdapter;
    private BootstrapButton btClear;
    private TextView tvRealTimeNum;
    private int lastOpenSwipePos = 0;

    //handler消息
    private final int NAMLELIST_RPT = 0;
    private final int UPDATE_LIST = 1;
    private final int CLEAR_LIST = 2;

    public RealtimeNamelistRptFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;

        rootView = inflater.inflate(R.layout.fragment_namelist_rpt, null);
        lvRealTimeNum = (ListView) rootView.findViewById(R.id.lvRealTimeNum);
        btClear = (BootstrapButton) rootView.findViewById(R.id.btClear);
        tvRealTimeNum = (TextView) rootView.findViewById(R.id.tvRealTimeNum);
        btClear.setOnClickListener(clearListener);

        mAdapter = new RealtimeNamelistRptAdapter(getActivity());
        lvRealTimeNum.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        lvRealTimeNum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastOpenSwipePos = position - lvRealTimeNum.getFirstVisiblePosition();
                openSwipe(lastOpenSwipePos);
            }
        });

        EventAdapter.setEvent(EventAdapter.BLACK_NAME_RPT,this);
        UIEventManager.register(UIEventManager.KEY_REFRESH_NAMELIST_RPT_LIST,this);

        return rootView;
    }

    private void openSwipe(int position){
        ((SwipeLayout) (lvRealTimeNum.getChildAt(position))).open(true);
        ((SwipeLayout) (lvRealTimeNum.getChildAt(position))).setClickToClose(true);
    }

    private void closeSwipe(int position){
        ((SwipeLayout) (lvRealTimeNum.getChildAt(position))).close(true);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == NAMLELIST_RPT){
                BlackNameBean blackName = (BlackNameBean) msg.obj;
                if (CacheManager.getLocState() && CacheManager.getCurrentLoction().getImsi().equals(blackName.getIMSI()))
                    return;

                if(mAdapter != null && msg.obj != null){
                    mAdapter.addItem("IMSI:"+blackName.getIMSI() +"\n"+"姓名:"+getNameByIMSI(blackName.getIMSI())+"\n"+getContext().getString(R.string.lab_rpt_time)+ blackName.getReportTime());
                }

                if(tvRealTimeNum != null){
                    tvRealTimeNum.setText(String.valueOf(mAdapter.getCount()));
                }
            }else if(msg.what == UPDATE_LIST){
                if(tvRealTimeNum != null){
                    tvRealTimeNum.setText(String.valueOf(mAdapter.getCount()));
                }

                if(mAdapter != null ){
                    mAdapter.refreshData();
                }
                closeSwipe(lastOpenSwipePos);
            }else if(msg.what == CLEAR_LIST){
                if(mAdapter != null ){
                    mAdapter.clear();
                }

                if(tvRealTimeNum != null){
                    tvRealTimeNum.setText(String.valueOf(0));
                }
            }
        }
    };

    private String getNameByIMSI(String imsi) {
        String name = "未设置";
        try {
            DBBlackInfo info = UCSIDBManager.getDbManager().selector(DBBlackInfo.class).where("imsi","=", imsi).findFirst();
            if(info != null){
                name = info.getName();
            }
        } catch (DbException e) {
            e.printStackTrace();
        }


        return name;
    }

    @Override
    public void onFocus() {
        super.onFocus();
    }


    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mHandler.sendEmptyMessage(CLEAR_LIST);
        }
    };

    @Override
    public void handlerFinish(String key) {
        if (key.equals(UIEventManager.KEY_REFRESH_NAMELIST_RPT_LIST)) {
            mHandler.sendEmptyMessage(UPDATE_LIST);
        }
    }

    @Override
    public void call(String key, Object val) {
        if (key.equals(EventAdapter.BLACK_NAME_RPT)) {
            Message msg = new Message();
            msg.what = NAMLELIST_RPT;
            msg.obj = val;
            mHandler.sendMessage(msg);
        }
    }

}
