package com.yc.netlib.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yc.netlib.BuildConfig;
import com.yc.netlib.R;
import com.yc.netlib.connect.ConnectionManager;
import com.yc.netlib.connect.ConnectionQuality;
import com.yc.netlib.connect.ConnectionStateChangeListener;
import com.yc.netlib.connect.DeviceBandwidthSampler;
import com.yc.netlib.data.IDataPoolHandleImpl;
import com.yc.netlib.data.NetworkFeedBean;
import com.yc.netlib.ping.PingView;
import com.yc.netlib.utils.NetDeviceUtils;
import com.yc.netlib.utils.NetLogUtils;
import com.yc.netlib.utils.NetWorkUtils;
import com.yc.netlib.utils.NetworkTool;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.yc.netlib.connect.ConnectionQuality.UNKNOWN;

public class NetRequestPhoneFragment extends Fragment {

    private Activity activity;
    private TextView mTvBandWidth;
    private TextView tvPhoneContent;
    private TextView mTvAppInfo;
    private TextView tvContentInfo;
    private TextView tvWebInfo;
    private PingView tvNetInfo;
    private List<NetworkFeedBean> mNetworkFeedList;
    private static final int MESSAGE = 1;
    private ConnectionManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;
    private ConnectionQuality mConnectionClass = UNKNOWN;
    //????????????????????????
    private final String mURL = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1601463169772&di=80c295c40c3c236a6434a5c66cb84c41&imgtype=0&src=http%3A%2F%2Fimg1.kchuhai.com%2Felite%2F20200324%2Fhead20200324162648.jpg";
    private int mTries = 0;
    private boolean isSetCon = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE){
                String content = (String) msg.obj;
                tvWebInfo.setText(content);
            }
        }
    };


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_phone_info, container, false);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFindViewById(view);
        initData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mConnectionClassManager.remove(mListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mConnectionClassManager.register(mListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectionClassManager = ConnectionManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        mListener = new ConnectionChangedListener();
        mConnectionClassManager.reset();
        isSetCon = false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDeviceBandwidthSampler!=null){
            mDeviceBandwidthSampler.stopSampling();
        }
    }


    private void initFindViewById(View view) {
        mTvBandWidth = view.findViewById(R.id.tv_band_width);
        tvPhoneContent = view.findViewById(R.id.tv_phone_content);
        mTvAppInfo = view.findViewById(R.id.tv_app_info);
        tvContentInfo = view.findViewById(R.id.tv_content_info);
        tvWebInfo = view.findViewById(R.id.tv_web_info);
        tvNetInfo = view.findViewById(R.id.tv_net_info);
        mTvBandWidth.setText("????????????:???????????????");
        mTvBandWidth.postDelayed(new Runnable() {
            @Override
            public void run() {
                new DownloadImage().execute(mURL);
            }
        },100);
    }

    private void initData() {
        initListData();
        //??????????????????
        setPhoneInfo();
        //??????????????????
        setAppInfo();
        //????????????
        //??????mac????????????????????????ip???wifi??????
        setLocationInfo();
        //???????????????
        //?????????host?????????ip?????????mac???????????????????????????ipv4??????ipv6
        setNetInfo();
        //????????????????????????ping??????
        setPingInfo();
    }

    private void initListData() {
        mNetworkFeedList = new ArrayList<>();
        HashMap<String, NetworkFeedBean> networkFeedMap = IDataPoolHandleImpl.getInstance().getNetworkFeedMap();
        if (networkFeedMap != null) {
            Collection<NetworkFeedBean> values = networkFeedMap.values();
            mNetworkFeedList.addAll(values);
            try {
                Collections.sort(mNetworkFeedList, new Comparator<NetworkFeedBean>() {
                    @Override
                    public int compare(NetworkFeedBean networkFeedModel1, NetworkFeedBean networkFeedModel2) {
                        return (int) (networkFeedModel2.getCreateTime() - networkFeedModel1.getCreateTime());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setPhoneInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        final StringBuilder sb = new StringBuilder();
        sb.append("??????root:").append(NetDeviceUtils.isDeviceRooted());
        sb.append("\n???????????????:").append(NetDeviceUtils.getManufacturer());
        sb.append("\n???????????????:").append(NetDeviceUtils.getBrand());
        sb.append("\n???????????????:").append(NetDeviceUtils.getModel());
        sb.append("\n???????????????:").append(NetDeviceUtils.getId());
        sb.append("\nCPU?????????:").append(NetDeviceUtils.getCpuType());
        sb.append("\n???????????????:").append(NetDeviceUtils.getSDKVersionName());
        sb.append("\n???????????????:").append(NetDeviceUtils.getSDKVersionCode());
        sb.append("\nSd???????????????:").append(NetDeviceUtils.getSDCardSpace(application));
        sb.append("\n??????????????????:").append(NetDeviceUtils.getRomSpace(application));
        sb.append("\n???????????????:").append(NetDeviceUtils.getTotalMemory(application));
        sb.append("\n??????????????????:").append(NetDeviceUtils.getAvailMemory(application));
        sb.append("\n???????????????:").append(NetDeviceUtils.getWidthPixels(application))
                .append("x").append(NetDeviceUtils.getRealHeightPixels(application));
        sb.append("\n????????????:").append(NetDeviceUtils.getScreenInch(activity));
        tvPhoneContent.setText(sb.toString());
        tvPhoneContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetWorkUtils.copyToClipBoard(activity,sb.toString());
            }
        });
    }


    private void setAppInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        //????????????
        String versionName = "";
        String versionCode = "";
        try {
            PackageManager pm = application.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(application.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = String.valueOf(pi.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("??????App??????:").append(NetworkTool.getInstance().getApplication().getPackageName());
        sb.append("\n?????????DEBUG??????:").append(BuildConfig.BUILD_TYPE);
        if (versionName!=null && versionName.length()>0){
            sb.append("\n????????????:").append(versionName);
            sb.append("\n?????????:").append(versionCode);
        }
        ApplicationInfo applicationInfo = application.getApplicationInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sb.append("\n?????????????????????:").append(applicationInfo.minSdkVersion);
            sb.append("\n?????????????????????:").append(applicationInfo.targetSdkVersion);
            sb.append("\n????????????:").append(applicationInfo.processName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sb.append("\nUUID:").append(applicationInfo.storageUuid);
            }
            sb.append("\nAPK????????????:").append(applicationInfo.sourceDir);
        }
        mTvAppInfo.setText(sb.toString());
    }


    private void setLocationInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        StringBuilder sb = new StringBuilder();
        sb.append("wifi????????????:").append(NetDeviceUtils.getWifiState(application));
        sb.append("\nAndroidID:").append(NetDeviceUtils.getAndroidID(application));
        boolean wifiProxy = NetWorkUtils.isWifiProxy(application);
        if (wifiProxy){
            sb.append("\nwifi????????????:").append("??????????????????");
        } else {
            sb.append("\nwifi????????????:").append("???????????????");
        }
        sb.append("\nMac??????:").append(NetDeviceUtils.getMacAddress(application));
        sb.append("\nWifi??????:").append(NetDeviceUtils.getWifiName(application));
        int wifiIp = NetDeviceUtils.getWifiIp(application);
        String ip = NetDeviceUtils.intToIp(wifiIp);
        sb.append("\nWifi???Ip??????:").append(ip);
        DhcpInfo dhcpInfo = NetDeviceUtils.getDhcpInfo(application);
        if (dhcpInfo!=null){
            //sb.append("\nipAddress???").append(NetDeviceUtils.intToIp(dhcpInfo.ipAddress));
            sb.append("\n?????????????????????").append(NetDeviceUtils.intToIp(dhcpInfo.netmask));
            sb.append("\n???????????????").append(NetDeviceUtils.intToIp(dhcpInfo.gateway));
            sb.append("\nserverAddress???").append(NetDeviceUtils.intToIp(dhcpInfo.serverAddress));
            sb.append("\nDns1???").append(NetDeviceUtils.intToIp(dhcpInfo.dns1));
            sb.append("\nDns2???").append(NetDeviceUtils.intToIp(dhcpInfo.dns2));
        }
        tvContentInfo.setText(sb.toString());
    }

    private void setNetInfo() {
        if (mNetworkFeedList.size()>0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String curl = mNetworkFeedList.get(0).getCURL();
                    String host = Uri.parse(curl).getHost();
                    StringBuilder sb = new StringBuilder();
                    sb.append("??????ip??????:").append(NetDeviceUtils.getHostIP(host));
                    sb.append("\n??????host??????:").append(NetDeviceUtils.getHostName(host));
                    sb.append("\n?????????:").append("???????????????ip???mac??????ipv4??????ipv6?????????");
                    String string = sb.toString();
                    Message message = new Message();
                    message.what = MESSAGE;
                    message.obj = string;
                    handler.sendMessage(message);
                }
            }).start();
        }
    }

    private void setPingInfo() {
        Application application = NetworkTool.getInstance().getApplication();
        tvNetInfo.setDeviceId(NetDeviceUtils.getAndroidID(application));
        tvNetInfo.setUserId(application.getPackageName());
        //????????????
        String versionName = "";
        try {
            PackageManager pm = application.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(application.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            if (pi != null) {
                versionName = pi.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvNetInfo.setVersionName(versionName);
        if (mNetworkFeedList.size()>0){
            String curl = mNetworkFeedList.get(0).getCURL();
            String host = Uri.parse(curl).getHost();
            tvNetInfo.pingHost(host);
        }
    }

    /**
     * ????????????connectionclass???????????????UI
     */
    private class ConnectionChangedListener implements ConnectionStateChangeListener {
        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
            setConnText(mConnectionClass);
        }
    }


    private void setConnText(final ConnectionQuality mConnectionClass) {
        if (isSetCon){
            return;
        }
        isSetCon = true;
        mTvBandWidth.postDelayed(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                NetLogUtils.e("DownloadImage-----onBandwidthStateChange----"+mConnectionClass);
                StringBuffer sb = new StringBuffer();
                sb.append("????????????:");
                if (mConnectionClass==ConnectionQuality.UNKNOWN){
                    sb.append("????????????");
                } else if (mConnectionClass==ConnectionQuality.EXCELLENT){
                    sb.append("????????????2000kbps");
                } else if (mConnectionClass==ConnectionQuality.GOOD){
                    sb.append("?????????550???2000kbps??????");
                } else if (mConnectionClass==ConnectionQuality.MODERATE){
                    sb.append("?????????150???550kbps??????");
                } else if (mConnectionClass==ConnectionQuality.POOR){
                    sb.append("????????????150kbps");
                } else {
                    sb.append("????????????");
                }
                double downloadKBitsPerSecond = ConnectionManager.getInstance().getDownloadKBitsPerSecond();
                sb.append("\n???????????????:").append(downloadKBitsPerSecond);
                mTvBandWidth.setText(sb.toString());
            }
        },300);
    }


    private class DownloadImage extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            mDeviceBandwidthSampler.startSampling();
            NetLogUtils.e("DownloadImage-----onPreExecute-----??????");
        }

        @Override
        protected Void doInBackground(String... url) {
            String imageURL = url[0];
            try {
                URLConnection connection = new URL(imageURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                InputStream input = connection.getInputStream();
                try {
                    byte[] buffer = new byte[1024];
                    while (input.read(buffer) != -1) {

                    }
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                NetLogUtils.e("Error while downloading image.");
            } finally {
                NetLogUtils.e("DownloadImage-----doInBackground-----");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDeviceBandwidthSampler.stopSampling();
            // ??????10??????????????????????????????ConnectionClass
            if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
                mTries++;
                NetLogUtils.e("DownloadImage-----onPostExecute-----"+mTries);
                new DownloadImage().execute(mURL);
            }
            if (mTries==10){
                ConnectionQuality quality = ConnectionManager.getInstance().getCurrentBandwidthQuality();
                setConnText(quality);
            }
        }
    }
}
