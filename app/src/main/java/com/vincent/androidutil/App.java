package com.vincent.androidutil;

import android.app.Application;

import com.vincent.monitorcrashlib.crash.CrashHandler;
import com.vincent.monitorcrashlib.crash.CrashListener;
import com.vincent.monitorcrashlib.util.CrashToolUtils;
import com.yc.netlib.utils.NetworkTool;

/**
 * @author : wangwenbo
 * @date : 2022/9/9
 * Desc :
 */
public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        initCrash();
        NetworkTool.getInstance().init(this);

        //建议只在debug环境下显示，点击去网络拦截列表页面查看网络请求数据
//        NetworkTool.getInstance().setFloat(this);
    }


    private void initCrash() {
        //ThreadHandler.getInstance().init(this);
        CrashHandler.getInstance().init(this, new CrashListener() {
            /**
             * 重启app
             */
            @Override
            public void againStartApp() {
                System.out.println("崩溃重启----------againStartApp------");
                CrashToolUtils.reStartApp1(App.this, 2000);
//                Toast.makeText(App.this, "崩溃了", Toast.LENGTH_SHORT).show();

                //CrashToolUtils.reStartApp2(App.this,2000, MainActivity.class);
                //CrashToolUtils.reStartApp3(App.this);
            }

            /**
             * 自定义上传crash，支持开发者上传自己捕获的crash数据
             * @param ex                        ex
             */
            @Override
            public void recordException(Throwable ex) {
                System.out.println("崩溃重启----------recordException------");

//                for (StackTraceElement i : ex.getStackTrace()) {
//                    System.out.println("异常信息----" + i.toString());
//                }

                //自定义上传crash，支持开发者上传自己捕获的crash数据
                //StatService.recordException(getApplication(), ex);
            }
        });
    }
}
