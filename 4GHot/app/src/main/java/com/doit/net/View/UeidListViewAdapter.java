package com.doit.net.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.doit.net.Activity.GameApplication;
import com.doit.net.Bean.UeidBean;
import com.doit.net.Event.AddToLocalBlackListener;
import com.doit.net.Event.AddToLocationListener;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.ImsiMsisdnConvert;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Model.VersionManage;
import com.doit.net.Model.WhiteListInfo;
import com.doit.net.Utils.UtilOperator;
import com.doit.net.ucsi.R;
import com.doit.net.Utils.Logger;
import com.doit.net.Utils.StringUtils;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

public class UeidListViewAdapter extends BaseSwipeAdapter {
    private final static Logger log = Logger.getLogger(UeidListViewAdapter.class);
    private Context mContext;

    private onItemLongClickListener mOnItemLongClickListener;
    private MotionEvent motionEvent;

    public UeidListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private DbManager dbManager;

    public void refreshData(){
        notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.doit_layout_ueid_list_item, null);
        dbManager = UCSIDBManager.getDbManager();

        return v;
    }

    public void setOnItemLongClickListener(onItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    TextView ueidContent = null;
    @Override
    public synchronized void fillValues(int position, View convertView) {
        LinearLayout layoutItemText = (LinearLayout)convertView.findViewById(R.id.layoutItemText);
        if (position % 2 == 0){
            layoutItemText.setBackgroundColor(mContext.getResources().getColor(R.color.deepgrey2));
        }else{
            layoutItemText.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        }

        TextView index = (TextView)convertView.findViewById(R.id.position);
        index.setText((position + 1) + ".");

        ueidContent = (TextView)convertView.findViewById(R.id.tvUeidItemText);
        UeidBean resp = CacheManager.realtimeUeidList.get(position);

        String msisdn = ImsiMsisdnConvert.getMsisdnFromLocal(resp.getImsi());
        if (CacheManager.currentWorkMode.equals("0")){
            ueidContent.setText("IMSI:"+resp.getImsi() + "                "+"制式: "+ UtilOperator.getOperatorNameCH(resp.getImsi()) +"\n"
                    +"手机号:"+ msisdn+"                 "+"\n"+mContext.getString(R.string.ueid_last_rpt_time)+resp.getRptTime());
        }else if(CacheManager.currentWorkMode.equals("2")){
            ueidContent.setText("IMSI:"+resp.getImsi() + "                "+"制式: "+ UtilOperator.getOperatorNameCH(resp.getImsi()) +"\n"
                    +"手机号:"+msisdn+"           "+ mContext.getString(R.string.ueid_last_intensity) +resp.getSrsp()+"        "+ "次数:"+resp.getRptTimes()+"\n"
                    +mContext.getString(R.string.ueid_last_rpt_time)+resp.getRptTime() +"       " );
        }

        if (VersionManage.isPoliceVer()){
            convertView.findViewById(R.id.add_to_black).setOnClickListener(new AddToLocalBlackListener(mContext,resp.getImsi()));
        }else if (VersionManage.isArmyVer()){
            convertView.findViewById(R.id.add_to_black).setVisibility(View.GONE);
        }

        if(CacheManager.getLocMode()){
            convertView.findViewById(R.id.add_to_localtion).setOnClickListener(new AddToLocationListener(position,mContext,resp.getImsi(),resp.getTmsi()));
        }else{
            convertView.findViewById(R.id.add_to_localtion).setVisibility(View.GONE);
        }

        if (mOnItemLongClickListener != null) {
            //获取触摸点的坐标，以决定pop从哪里弹出
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            motionEvent = event;
                            break;
                        default:
                            break;
                    }
                    // 如果onTouch返回false,首先是onTouch事件的down事件发生，此时，如果长按，触发onLongClick事件；
                    // 然后是onTouch事件的up事件发生，up完毕，最后触发onClick事件。
                    return false;
                }
            });


            final int pos = position;
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //int position = holder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(motionEvent, pos);
                    //返回true 表示消耗了事件 事件不会继续传递
                    return true; //长按了就禁止swipe弹出
                }
            });
        }

        checkBlackWhiteList(resp, msisdn);

        ueidContent.setTag(position);
    }

    private void checkBlackWhiteList(UeidBean resp, String msisdn){
        if(ueidContent == null){
            return;
        }

        //优先先检查是否为黑名单
        if (VersionManage.isPoliceVer()){
            DBBlackInfo dbBlackInfo = null;
            try {
                dbBlackInfo = dbManager.selector(DBBlackInfo.class).where("imsi","=",resp.getImsi()).findFirst();
            } catch (DbException e) {log.error("查询黑名单异常",e);}
            if(dbBlackInfo != null){
                //black_warn.setVisibility(View.VISIBLE);
                String name = "";
                if(!StringUtils.isBlank(dbBlackInfo.getName())){
                    name = mContext.getString(R.string.lab_name)+ dbBlackInfo.getName();
                }

                String tipTxt = ueidContent.getText() + "     " + name;
                ueidContent.setText(tipTxt);
                ueidContent.setTextColor(GameApplication.appContext.getResources().getColor(R.color.red));
                return;
            }
        }

        //如果是管控模式，其次检查白名单
        if (CacheManager.currentWorkMode.equals("2")){
            boolean isWhitelist = false;
            try {
                if(!"".equals(resp.getImsi())){
                    WhiteListInfo info = dbManager.selector(WhiteListInfo.class).where("imsi","=",resp.getImsi()).findFirst();
                    if (info != null){
                        isWhitelist = true;
                    }else {
                        if (!"".equals(msisdn) && !msisdn.equals("未获取")){
                            info = dbManager.selector(WhiteListInfo.class).where("msisdn","=",msisdn).findFirst();
                            isWhitelist = (info != null);
                        }
                    }
                    isWhitelist = (info != null);
                }

                if (isWhitelist){
                    ueidContent.setTextColor(GameApplication.appContext.getResources().getColor(R.color.forestgreen));
                    return;
                }
            } catch (DbException e) {
                log.error("查询白名单异常",e);
            }
        }

        ueidContent.setTextColor(GameApplication.appContext.getResources().getColor(R.color.white));
    }

    @Override
    public int getCount() {
        return CacheManager.realtimeUeidList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface onItemLongClickListener {
        void onItemLongClick(MotionEvent motionEvent, int position);
    }

}
