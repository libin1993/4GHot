package com.doit.net.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.doit.net.Event.AddToLocalBlackListener;
import com.doit.net.Event.ProtocolManager;
import com.doit.net.Model.BlackBoxManger;
import com.doit.net.Event.EventAdapter;
import com.doit.net.Model.DBBlackInfo;
import com.doit.net.Model.UCSIDBManager;
import com.doit.net.Utils.ToastUtils;
import com.doit.net.ucsi.R;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zxc on 2019/2/18.
 */

public class ModifyNamelistInfoDialog extends Dialog {
    private String modifyName;
    private String modifyRemake;
    private String modifyIMSI;
    private View mView;
    private EditText etName;
    private EditText etRemake;
    private EditText etIMSI;
    private Button btSave;
    private Button btCancel;

    public ModifyNamelistInfoDialog(Context context, String name, String imsi, String remake ) {
        super(context, R.style.Theme_dialog);
        modifyName = name;
        modifyRemake = remake;
        modifyIMSI = imsi;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);
        x.view().inject(this,mView);
    }

    private void initView(){
        LayoutInflater inflater= LayoutInflater.from(getContext());
        mView = inflater.inflate(R.layout.layout_modify_namelist_info, null);
        setCancelable(false);

        etName = (EditText)mView.findViewById(R.id.etName);
        etName.setText(modifyName);
        etRemake = (EditText)mView.findViewById(R.id.etRemark);
        etRemake.setText(modifyRemake);
        etIMSI = (EditText)mView.findViewById(R.id.etPassword);
        etIMSI.setText(modifyIMSI);
        btSave = (Button)mView.findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String imsi =  etIMSI.getText().toString();
                String name =  etName.getText().toString();
                String remark =  etRemake.getText().toString();

                try {
                    DbManager db = UCSIDBManager.getDbManager();
                    DBBlackInfo tmpNamelist = db.selector(DBBlackInfo.class)
                            .where("imsi", "=", modifyIMSI)
                            .findFirst();

                    if (tmpNamelist == null){
                        ToastUtils.showMessage(getContext(),R.string.modify_namelist_fail);
                        return;
                    }

                    //如果IMSI发生了变动就比较麻烦了
                    if (!imsi.equals(modifyIMSI)){
                        ProtocolManager.setBlackList("3", "#"+modifyIMSI);
                        db.delete(tmpNamelist);
                        new AddToLocalBlackListener(getContext(),name,imsi,remark).onClick(v);
                    }else{
                        tmpNamelist.setName(etName.getText().toString());
                        tmpNamelist.setRemark(etRemake.getText().toString());
                        db.update(tmpNamelist, "name", "remark");
                    }

                    ToastUtils.showMessage(getContext(),R.string.modify_namelist_success);
                    EventAdapter.call(EventAdapter.ADD_BLACKBOX, BlackBoxManger.MODIFY_NAMELIST+"修改名单"+modifyName+"为:"+ etName.getText().toString()
                            + "+" + etIMSI.getText().toString() + "+" + etRemake.getText().toString());
                } catch (DbException e) {
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getContext().getString(R.string.modify_namelist_fail))
                            .show();
                }

                dismiss();
            }
        });


        btCancel = (Button)mView.findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}