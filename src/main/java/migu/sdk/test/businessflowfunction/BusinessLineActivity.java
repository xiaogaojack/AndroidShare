package migu.sdk.test.businessflowfunction;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import business.gao.com.logfuction.LogUtils;
import migu.sdk.test.businessflowfunction.business.BusinessComponent;
import migu.sdk.test.businessflowfunction.business.IBusinessAction;

/**
 * Created by xiang on 2018/7/5.
 */

public class BusinessLineActivity extends Activity {
    private BusinessComponent mBusinessComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = new LinearLayout(this);
        setContentView(linearLayout);

        mBusinessComponent = new BusinessComponent();

        Button mButtonSendMessage = new Button(this);
        mButtonSendMessage.setText("发送消息");
        mButtonSendMessage.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(mButtonSendMessage);


        mButtonSendMessage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mBusinessComponent.sendBusinessAction(new IBusinessAction() {
                    @Override
                    public void doAction() {
                        LogUtils.log("开始执行第一个任务：");

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(5000);
                                }catch (Exception e){

                                }

                                mBusinessComponent.notifyNext();

                                LogUtils.log(" 第一个任务执行完毕：");
                            }
                        });

                        thread.start();
                    }
                });

                mBusinessComponent.sendBusinessAction(new IBusinessAction() {
                    @Override
                    public void doAction() {
                        LogUtils.log("开始执行第二个任务：");

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                }catch (Exception e){

                                }

                                mBusinessComponent.notifyNext();

                                LogUtils.log(" 第二个任务执行完毕：");
                            }
                        });

                        thread.start();
                    }
                });

                mBusinessComponent.sendBusinessAction(new IBusinessAction() {
                    @Override
                    public void doAction() {
                        LogUtils.log("开始执行第三个任务：");

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                }catch (Exception e){

                                }

                                mBusinessComponent.notifyNext();

                                LogUtils.log(" 第三个任务执行完毕：");
                            }
                        });

                        thread.start();
                    }
                });
            }
        });

        super.onCreate(savedInstanceState);
    }
}
