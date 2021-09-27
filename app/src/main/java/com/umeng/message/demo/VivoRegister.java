package com.umeng.message.demo;

import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.taobao.accs.utl.ALog;
import com.taobao.accs.utl.UtilityImpl;
import com.taobao.agoo.BaseNotifyClickActivity;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import org.android.agoo.control.NotifManager;
import org.android.agoo.vivo.VivoBadgeReceiver;
import org.android.agoo.vivo.VivoMsgParseImpl;

public class VivoRegister {
    public static final String TAG = "VivoRegister";
    private static Context mContext;
    private static VivoBadgeReceiver vivoBadgeReceiver;

    public VivoRegister() {
    }

    public static void register(final Context context) {
        try {
            if (context == null) {
                return;
            }

            mContext = context.getApplicationContext();
            if (!UtilityImpl.isMainProcess(context)) {
                ALog.i("VivoRegister", "not in main process, return", new Object[0]);
                return;
            }

            if (PushClient.getInstance(context).isSupport()) {
                ALog.d("VivoRegister", "register start", new Object[0]);
                BaseNotifyClickActivity.addNotifyListener(new VivoMsgParseImpl());
                PushClient.getInstance(context).initialize();
                PushClient.getInstance(context).turnOnPush(new IPushActionListener() {
                    public void onStateChanged(int state) {
                        ALog.d("VivoRegister", "turnOnPush", new Object[]{"state", state});
                        if (state == 0) {
                            String regId = PushClient.getInstance(context).getRegId();
                            if (!TextUtils.isEmpty(regId)) {
                                NotifManager notifyManager = new NotifManager();
                                notifyManager.init(context.getApplicationContext());
                                notifyManager.reportThirdPushToken(regId, "VIVO_TOKEN", "1.1.5", true);
                            }
                        }

                    }
                });
                vivoBadgeReceiver = new VivoBadgeReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction("msg.action.ACTION_MPM_MESSAGE_BOX_UNREAD");
                mContext.registerReceiver(vivoBadgeReceiver, filter);
            } else {
                ALog.e("VivoRegister", "this device is not support vivo push", new Object[0]);
            }
        } catch (Throwable var2) {
            ALog.e("VivoRegister", "register", var2, new Object[0]);
        }

    }

    public static void unregister() {
        ALog.i("VivoRegister", "unregister", new Object[0]);
        if (vivoBadgeReceiver != null) {
            mContext.unregisterReceiver(vivoBadgeReceiver);
        }

        PushClient.getInstance(mContext).turnOffPush(new IPushActionListener() {
            public void onStateChanged(int state) {
                ALog.d("VivoRegister", "turnOffPush", new Object[]{"state", state});
            }
        });
    }
}