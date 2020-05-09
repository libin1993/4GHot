package com.doit.net.View;

import com.doit.net.Model.VersionManage;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.doit.net.Base.BaseFragment;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Event.IHandlerFinish;
import com.doit.net.Event.ProtocolManager;
import com.doit.net.Event.UIEventManager;
import com.doit.net.Model.AccountManage;
import com.doit.net.Model.CacheManager;
import com.doit.net.Model.DBUeidInfo;
import com.doit.net.Model.PrefManage;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.MySweetAlertDialog;
import com.doit.net.Utils.UtilBaseLog;
import com.doit.net.Utils.DateUtil;
import com.doit.net.Utils.Logger;
import com.doit.net.Utils.StringUtils;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.Utils.LSettingItem;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ViewInject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE;

public class AppFragment extends BaseFragment implements IHandlerFinish {
    private final static Logger log = Logger.getLogger(AppFragment.class);
    private DbManager dbManager;
    private MainActivity mainActivity;

    private MySweetAlertDialog mProgressDialog;

    @ViewInject(R.id.tvLoginAccount)
    private TextView tvLoginAccount;

    @ViewInject(R.id.btClearUeid)
    private LSettingItem btClearUeid;

    @ViewInject(R.id.tvLocalImsi)
    private LSettingItem tvLocalImsi;

    @ViewInject(R.id.tvSupportVoice)
    private LSettingItem tvSupportVoice;

    @ViewInject(R.id.tvVersion)
    private LSettingItem tvVersion;

    @ViewInject(R.id.btSetWhiteList)
    private LSettingItem btSetWhiteList;

    @ViewInject(R.id.btUserManage)
    private LSettingItem btUserManage;

    @ViewInject(R.id.btBlackBox)
    private LSettingItem btBlackBox;

    @ViewInject(R.id.btn_history_view)
    private LSettingItem historyItem;

    @ViewInject(R.id.btWifiSetting)
    private LSettingItem btWifiSetting;

    @ViewInject(R.id.btDeviceParam)
    private LSettingItem btDeviceParam;

    @ViewInject(R.id.btDeviceInfoAndUpgrade)
    private LSettingItem btDeviceInfoAndUpgrade;

    @ViewInject(R.id.tvSystemSetting)
    private LSettingItem tvSystemSetting;

    @ViewInject(R.id.btAuthorizeCodeInfo)
    private LSettingItem btAuthorizeCodeInfo;

    @ViewInject(R.id.tvTest)
    private LSettingItem just4Test;

    private ListView lvPackageList;
    private ArrayAdapter upgradePackageAdapter;
    private LinearLayout layoutUpgradePackage;

    private View rootView;
    private String[] playTypes;

    //handler消息
    private final int EXPORT_SUCCESS = 0;
    private final int EXPORT_ERROR = -1;
    private final int UPGRADE_STATUS_RPT = 1;

    public AppFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
            return rootView;
        }

        mainActivity = (MainActivity) getActivity();
        rootView = inflater.inflate(R.layout.doit_layout_app, container, false);
        dbManager = UCSIDBManager.getDbManager();
        UIEventManager.register(UIEventManager.RPT_UPGRADE_STATUS, this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playTypes = getResources().getStringArray(R.array.play_list);

        tvLoginAccount.setText(AccountManage.getCurrentLoginAccount());

        if (VersionManage.isPoliceVer()) {
            btSetWhiteList.setVisibility(View.GONE);
        } else if (VersionManage.isArmyVer()) {
            btSetWhiteList.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
                @Override
                public void click(LSettingItem item) {
                    startActivity(new Intent(getActivity(), WhitelistManegerActivity.class));
                }
            });
        }

        historyItem.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), HistoryListActivity.class));
            }
        });

        btClearUeid.setmOnLSettingItemClick(clearHistoryListener);

        btUserManage.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), UserManageActivity.class));
            }
        });

        if ((VersionManage.isPoliceVer())){
            btBlackBox.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
                @Override
                public void click(LSettingItem item) {
                    startActivity(new Intent(getActivity(), BlackBoxActivity.class));
                }
            });
        }

        btWifiSetting.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            }
        });

        tvSystemSetting.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), SystemSetting.class));
            }
        });

        btDeviceParam.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), DeviceParamActivity.class));
            }
        });

        btDeviceInfoAndUpgrade.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                showDeviceInfoDialog();
            }
        });

        btAuthorizeCodeInfo.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                LicenceDialog licenceDialog = new LicenceDialog(getActivity());
                licenceDialog.show();
            }
        });

        just4Test.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(LSettingItem item) {
                startActivity(new Intent(getActivity(), JustForTest.class));
            }
        });

        final String imsi = getImsi();
        tvLocalImsi.setRightText(imsi);
        tvLocalImsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(imsi);
            }
        });

        if (PrefManage.supportPlay) {
            tvSupportVoice.setRightText("支持");
        } else {
            tvSupportVoice.setRightText("不支持");
        }

        tvVersion.setRightText(VersionManage.getVersionName(getContext()));
        initProgressDialog();

        if (AccountManage.getCurrentPerLevel() >= AccountManage.PERMISSION_LEVEL2) {
            btUserManage.setVisibility(View.VISIBLE);
            if (VersionManage.isPoliceVer()){    //军队版本不使用黑匣子
                btBlackBox.setVisibility(View.VISIBLE);
            }
            btClearUeid.setVisibility(View.VISIBLE);
        }

        if (AccountManage.getCurrentPerLevel() >= AccountManage.PERMISSION_LEVEL3) {
            just4Test.setVisibility(View.VISIBLE);
            tvSystemSetting.setVisibility(View.VISIBLE);
        }
    }

    private void initProgressDialog() {
        mProgressDialog = new MySweetAlertDialog(getContext(), MySweetAlertDialog.PROGRESS_TYPE);
        mProgressDialog.setTitleText("升级包正在加载，请耐心等待...");
        mProgressDialog.setCancelable(false);
    }

    private void showDeviceInfoDialog() {
        if (!CacheManager.checkDevice(getContext()))
            return;

        final View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_device_info, null);
        TextView tvDeviceIP = (TextView) dialogView.findViewById(R.id.tvDeviceIP);
        tvDeviceIP.setText(CacheManager.DEVICE_IP);
        TextView tvHwVersion = (TextView) dialogView.findViewById(R.id.tvHwVersion);
        tvHwVersion.setText(CacheManager.getLteEquipConfig().getHw());
        TextView tvSwVersion = (TextView) dialogView.findViewById(R.id.tvSwVersion);
        tvSwVersion.setText(CacheManager.getLteEquipConfig().getSw());
        Button btDeviceUpgrade = (Button) dialogView.findViewById(R.id.btDeviceUpgrade);
        btDeviceUpgrade.setOnClickListener(upgradeListner);
        lvPackageList = (ListView) dialogView.findViewById(R.id.lvPackageList);
        layoutUpgradePackage = (LinearLayout) dialogView.findViewById(R.id.layoutUpgradePackage);

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    private String getImsi() {
        TelephonyManager telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return StringUtils.defaultIfBlank(telManager.getSubscriberId(), getString(R.string.no_sim_card));
    }

    @SuppressLint("MissingPermission")
    private String getImei() {
        TelephonyManager telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        return StringUtils.defaultIfBlank(telManager.getDeviceId(), getString(R.string.no));
    }

    LSettingItem.OnLSettingItemClick clearHistoryListener = new LSettingItem.OnLSettingItemClick() {
        @Override
        public void click(LSettingItem item) {
            ClearHistoryTimeDialog clearHistoryTimeDialog = new ClearHistoryTimeDialog(getActivity());
            clearHistoryTimeDialog.show();
        }
    };


    private String getPackageMD5(String FilePath) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(FilePath);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bi.toString(16);
    }

    View.OnClickListener upgradeListner = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            final String FTP_SERVER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/4GHotspot";
            final String UPGRADE_PACKAGE_PATH = "/upgrade/";

            File file = new File(FTP_SERVER_PATH+UPGRADE_PACKAGE_PATH);
            if (!file.exists()){
                ToastUtils.showMessageLong(getContext(), "未找到升级包，请确认已将升级包放在\"手机存储/4GHotspot/upgrade\"目录下");
                return;
            }

            File[] files = file.listFiles();
            if (files == null || files.length == 0){
                ToastUtils.showMessageLong(getContext(), "未找到升级包，请确认已将升级包放在\"手机存储/4GHotspot/upgrade\"目录下");
                return;
            }

            final List<String> fileList = new ArrayList<>();
            String tmpFileName = "";
            for(int i = 0 ;i < files.length; i++){
                tmpFileName = files[i].getName();
                //UtilBaseLog.printLog("获取升级包：" + tmpFileName);
                if (tmpFileName.endsWith(".tgz"))
                    fileList.add(tmpFileName);
            }
            if (fileList.size() == 0){
                ToastUtils.showMessageLong(getContext(), "文件错误，升级包必须是以\".tgz\"为后缀的文件");
                return;
            }

            layoutUpgradePackage.setVisibility(View.VISIBLE);
            upgradePackageAdapter = new ArrayAdapter<String>(getContext(), R.layout.comman_listview_text, fileList);
            lvPackageList.setAdapter(upgradePackageAdapter);
            lvPackageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String choosePackage = fileList.get(position);
                    UtilBaseLog.printLog("选择升级包："+choosePackage);

                    new MySweetAlertDialog(getContext(), MySweetAlertDialog.WARNING_TYPE)
                            .setTitleText("提示")
                            .setContentText("选择的升级包为："+choosePackage+", 确定升级吗？")
                            .setCancelText(getContext().getString(R.string.cancel))
                            .setConfirmText(getContext().getString(R.string.sure))
                            .showCancelButton(true)
                            .setConfirmClickListener(new MySweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(MySweetAlertDialog sweetAlertDialog) {
                                    String md5 = getPackageMD5(FTP_SERVER_PATH+UPGRADE_PACKAGE_PATH+choosePackage);
                                    if ("".equals(md5)){
                                        ToastUtils.showMessage(getContext(), "文件校验失败，升级取消！");
                                        sweetAlertDialog.dismiss();
                                        return;
                                    }else{
                                        UtilBaseLog.printLog("MD5：" + md5);
                                        String command = UPGRADE_PACKAGE_PATH + choosePackage + "#" + md5;
                                        UtilBaseLog.printLog(command);
                                        ProtocolManager.systemUpgrade(command);
                                        mProgressDialog.show();
                                        sweetAlertDialog.dismiss();
                                    }
                                }
                            }).show();

                }
            });
        }
    };

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == EXPORT_SUCCESS){
                new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("导出成功")
                        .setContentText("文件导出在："+msg.obj)
                        .show();
            }else if(msg.what == EXPORT_ERROR){
                new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("导出失败")
                        .setContentText("失败原因："+msg.obj)
                        .show();
            }else if (msg.what == UPGRADE_STATUS_RPT) {
                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }
        }
    };

    private void createExportError(String obj){
        Message msg = new Message();
        msg.what = UPGRADE_STATUS_RPT;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }

    @Override
    public void handlerFinish(String key) {
        if (key.equals(UIEventManager.RPT_UPGRADE_STATUS)){
            Message msg = new Message();
            msg.what = UPGRADE_STATUS_RPT;
            mHandler.sendMessage(msg);
        }
    }
}