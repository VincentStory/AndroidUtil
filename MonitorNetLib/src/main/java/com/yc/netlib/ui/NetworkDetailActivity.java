package com.yc.netlib.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.netlib.R;
import com.yc.netlib.data.IDataPoolHandleImpl;
import com.yc.netlib.data.NetworkFeedBean;
import com.yc.netlib.utils.CompressUtils;
import com.yc.netlib.utils.NetToolUtils;
import com.yc.netlib.utils.ScreenShotsUtils;
import com.yc.netlib.utils.ToolFileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.LinkedHashMap;
import java.util.Map;


public class NetworkDetailActivity extends AppCompatActivity {

    public static final int JSON_INDENT = 4;
    private NetworkFeedBean mNetworkFeedModel;
    private LinearLayout mLlBack;
    private TextView mTvDelete;
    private TextView mTvScreenshot;
    private TextView mTvUrlContent;
    private TextView mTvRequestHeaders;
    private TextView mTvResponseHeaders;
    private TextView mTvBody;
    private NestedScrollView mScrollView;
    private ImageView mIvScreenShot;
    private Handler handler = new Handler();


    public static void start(Context context, String requestId) {
        Intent starter = new Intent(context, NetworkDetailActivity.class);
        starter.putExtra("requestId", requestId);
        context.startActivity(starter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detail);
        initFindViewById();
        initData();
        initListener();
    }

    private void initFindViewById() {
        mLlBack = findViewById(R.id.ll_back);
        mTvDelete = findViewById(R.id.tv_delete);
        mTvScreenshot = findViewById(R.id.tv_screenshot);
        mTvUrlContent = findViewById(R.id.tv_url_content);
        mTvRequestHeaders = findViewById(R.id.tv_request_headers);
        mTvResponseHeaders = findViewById(R.id.tv_response_headers);
        mTvBody = findViewById(R.id.tv_body);
        mScrollView = findViewById(R.id.scrollView);
        mIvScreenShot = findViewById(R.id.iv_screen_shot);
    }

    private void initData() {
        String requestId = getIntent().getStringExtra("requestId");
        if (TextUtils.isEmpty(requestId)) {
            return;
        }
        mNetworkFeedModel = IDataPoolHandleImpl.getInstance().getNetworkFeedModel(requestId);
        if (mNetworkFeedModel == null) {
            return;
        }
        setCURLContent();
        setRequestHeaders();
        setResponseHeaders();
        setBody();
    }

    private void setCURLContent() {
        Map<String, String> map = new LinkedHashMap<>();
        String url = mNetworkFeedModel.getUrl();
        if (url.length()>40){
            String substring = url.substring(0, 40);
            url = substring + "??????";
            map.put("Request URL",url);
        } else {
            map.put("Request URL",url);
        }
        map.put("Request Method",mNetworkFeedModel.getMethod());
        int status = mNetworkFeedModel.getStatus();
        if (status==200){
            map.put("Status Code","200"+"  ok");
        } else {
            map.put("Status Code",status+"  ok");
        }
        map.put("Remote Address","??????");
        map.put("Referrer Policy","??????");
        Format format = new DecimalFormat("#.00");
        String dataSize = format.format(mNetworkFeedModel.getSize() * 0.001) + " KB";
        map.put("size",dataSize);
        map.put("connectTimeoutMillis",mNetworkFeedModel.getConnectTimeoutMillis()+"");
        map.put("readTimeoutMillis",mNetworkFeedModel.getReadTimeoutMillis()+"");
        map.put("writeTimeoutMillis",mNetworkFeedModel.getWriteTimeoutMillis()+"");
        String string = parseHeadersMapToString(map);
        mTvUrlContent.setText(string);
        mTvUrlContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetInfoUrlDialog dialog = new NetInfoUrlDialog(NetworkDetailActivity.this);
                String curl = mNetworkFeedModel.getCURL();
                dialog.setData(curl);
                dialog.setTitle("??????????????????url");
                dialog.show();
            }
        });
    }

    private void setRequestHeaders() {
        Map<String, String> requestHeadersMap = mNetworkFeedModel.getRequestHeadersMap();
        final String string = parseHeadersMapToString(requestHeadersMap);
        mTvRequestHeaders.setText(string);
        mTvRequestHeaders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetInfoUrlDialog dialog = new NetInfoUrlDialog(NetworkDetailActivity.this);
                String response;
                if (string.length()>200){
                    response = string.substring(0, 200);
                } else {
                    response = string;
                }
                dialog.setData(response);
                dialog.setTitle("?????????????????????");
                dialog.show();
            }
        });
    }

    private void setResponseHeaders() {
        Map<String, String> responseHeadersMap = mNetworkFeedModel.getResponseHeadersMap();
        final String string = parseHeadersMapToString(responseHeadersMap);
        mTvResponseHeaders.setText(string);
        mTvResponseHeaders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetInfoUrlDialog dialog = new NetInfoUrlDialog(NetworkDetailActivity.this);
                String response;
                if (string.length()>200){
                    response = string.substring(0, 200);
                } else {
                    response = string;
                }
                dialog.setData(response);
                dialog.setTitle("?????????????????????");
                dialog.show();
            }
        });
    }

    private void setBody() {
        if (mNetworkFeedModel.getContentType().contains("json")) {
            mTvBody.setText(formatJson(mNetworkFeedModel.getBody()));
        } else {
            mTvBody.setText(mNetworkFeedModel.getBody());
        }
        mTvBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetInfoUrlDialog dialog = new NetInfoUrlDialog(NetworkDetailActivity.this);
                String string = mTvBody.getText().toString();
                if (string.length()>200){
                    string = string.substring(0, 200);
                }
                dialog.setData(string);
                dialog.setTitle("???????????????body??????");
                dialog.show();
            }
        });
    }

    private String parseHeadersMapToString(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "Header is Empty.";
        }
        StringBuilder headersBuilder = new StringBuilder();
        for (String name : headers.keySet()) {
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            headersBuilder
                    .append(name)
                    .append(" : ")
                    .append(headers.get(name))
                    .append("\n");
        }
        return headersBuilder.toString().trim();
    }

    private String formatJson(String body) {
        String message;
        try {
            if (body.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(body);
                message = jsonObject.toString(JSON_INDENT);
            } else if (body.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(body);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = body;
            }
        } catch (JSONException e) {
            message = body;
        }
        return message;
    }

    private void initListener() {
        mLlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NetworkDetailActivity.this,"????????????",Toast.LENGTH_SHORT).show();
            }
        });
        mTvScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveScreenShot();
            }
        });
    }

    private void saveScreenShot() {
        showProgressLoading("??????????????????...");
        //????????????
        final Bitmap bitmap = ScreenShotsUtils.measureSize(this,mScrollView);
        new Thread(new Runnable() {
            @Override
            public void run() {
                savePicture(bitmap);
            }
        }).start();
    }


    private void savePicture(Bitmap bitmap) {
        if (bitmap != null) {
            String crashPicPath = ToolFileUtils.getCrashPicPath(NetworkDetailActivity.this) + "/net_pic_" + System.currentTimeMillis() + ".jpg";
            boolean saveBitmap = NetToolUtils.saveBitmap(NetworkDetailActivity.this, bitmap, crashPicPath);
            if (saveBitmap) {
                showToast("???????????????????????????????????????\n?????????" + crashPicPath);
                final Bitmap bitmapCompress = CompressUtils.getBitmap(new File(crashPicPath), 200, 200);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressLoading();
                        //????????????
                        mIvScreenShot.setImageBitmap(bitmapCompress);
                        //??????
                        mIvScreenShot.setVisibility(View.VISIBLE);
                        //????????????
                        ViewGroup.LayoutParams layoutParams = mIvScreenShot.getLayoutParams();
                        layoutParams.width = NetToolUtils.getScreenWidth(NetworkDetailActivity.this);
                        layoutParams.height = bitmapCompress.getHeight() * layoutParams.width / bitmapCompress.getWidth();
                        mIvScreenShot.setLayoutParams(layoutParams);
                        //??????????????????
                        mIvScreenShot.setPivotX(0);
                        mIvScreenShot.setPivotY(0);
                        AnimatorSet animatorSetScale = new AnimatorSet();
                        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mIvScreenShot, "scaleX", 1, 0.2f);
                        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mIvScreenShot, "scaleY", 1, 0.2f);
                        animatorSetScale.setDuration(1000);
                        animatorSetScale.setInterpolator(new DecelerateInterpolator());
                        animatorSetScale.play(scaleX).with(scaleY);
                        animatorSetScale.start();

                        //???????????????
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mIvScreenShot.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                });
            } else {
                showToast("??????????????????");
                dismissProgressLoading();
            }
        } else {
            showToast("??????????????????");
            dismissProgressLoading();
        }
    }


    public void showProgressLoading(String msg) {
        LinearLayout progress_view = findViewById(R.id.progress_view);
        TextView tv_progressbar_msg = findViewById(R.id.tv_progressbar_msg);
        if (progress_view != null) {
            progress_view.setVisibility(View.VISIBLE);
            tv_progressbar_msg.setText(msg);
            progress_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    public void dismissProgressLoading() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout progress_view = findViewById(R.id.progress_view);
                TextView tv_progressbar_msg = findViewById(R.id.tv_progressbar_msg);
                if (progress_view != null) {
                    progress_view.setVisibility(View.GONE);
                    tv_progressbar_msg.setText("?????????...");
                }
            }
        });
    }

    private void showToast(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NetworkDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
