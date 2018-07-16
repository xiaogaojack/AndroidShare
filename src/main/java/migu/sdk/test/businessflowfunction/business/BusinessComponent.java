package migu.sdk.test.businessflowfunction.business;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;

import java.util.ArrayList;

import test.sdk.mugu.reflectfunction.ReflectUtils;

/**
 * Created by xiang on 2018/7/5.
 */

public class BusinessComponent {
    private BusinessHandler mHandler;
    private BusinessThread mBusinessThread;

    private ArrayList<Integer> mBarrierTokens = new ArrayList<Integer>();
    private ArrayList<BusinessMessage> mBusinessMessages = new ArrayList<BusinessMessage>();

    public BusinessComponent() {
        mBusinessThread = new BusinessThread();
        mBusinessThread.start();

        mHandler = new BusinessHandler(mBusinessThread.getLooper());
    }

    public void sendBusinessAction(IBusinessAction action) {
        if (action == null) {
            return;
        }

        try {
            Message msg = Message.obtain();
            buildBusinessMessage(msg, action);
            mHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 务必完成后调用
     */
    public void notifyNext() {
        ArrayList<Integer> listBarries = new ArrayList<Integer>();

        synchronized (mBusinessMessages){
            for (int nIndex = 0; nIndex < mBusinessMessages.size(); nIndex++){
                BusinessMessage message = mBusinessMessages.get(nIndex);

                if (message != null && message.hasExcuted()) {
                    listBarries.add(message.token);
                    removeBusinessMessage(message);
                    nIndex--;
                }
            }
        }

        synchronized (mBarrierTokens){
            for (int nIndex = 0; nIndex < listBarries.size(); nIndex++){
                removeBarrier(listBarries.get(nIndex));
            }
        }
    }

    /**
     * 添加障碍，阻塞同步消息
     */
    private int addBarrier() {
        Integer objToken = -1;
        long barrierTime = buildBarrierMessageTime();

        try {
            if (Build.VERSION.SDK_INT > 23) {
                MessageQueue queue = mBusinessThread.getLooper().getQueue();
                objToken = (Integer) ReflectUtils.invokeDeclaredMethod(queue, "postSyncBarrier", new Object[]{barrierTime}, new Class[]{long.class});
            } else {
                Looper looper = mBusinessThread.getLooper();
                MessageQueue queue = (MessageQueue) ReflectUtils.getDeclaredFieldValue(looper, "mQueue");

                objToken = (Integer) ReflectUtils.invokeDeclaredMethod(queue, "enqueueSyncBarrier", new Object[]{barrierTime}, new Class[]{long.class});
            }

            synchronized (mBarrierTokens) {
                mBarrierTokens.add(objToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return objToken;
    }

    /**
     * 确保当前消息要在 所有消息之前
     * @return
     */
    private long buildBarrierMessageTime(){
       return  SystemClock.uptimeMillis() - 60 * 60 * 1000;
    }

    /**
     * 解除障碍，继续执行后续消息
     *
     * @param token
     */
    private void removeBarrier(Integer token) {
        try {
            if (Build.VERSION.SDK_INT > 23) {
                MessageQueue queue = mBusinessThread.getLooper().getQueue();
                ReflectUtils.invokeDeclaredMethod(queue, "removeSyncBarrier", new Object[]{token}, new Class[]{int.class});
            } else {
                ReflectUtils.invokeDeclaredMethod(mBusinessThread.getLooper(), "removeSyncBarrier", new Object[]{token}, new Class[]{int.class});
            }

            synchronized (mBarrierTokens){
                mBarrierTokens.remove(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BusinessHandler extends Handler {
        public BusinessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void dispatchMessage(Message msg) {
            if (msg.getTarget() != null) {
                int token = addBarrier();                //  针对业务消息加锁

                BusinessMessage businessMessage = findBusinessMessage(msg);

                if (businessMessage != null) {
                    businessMessage.token = token;
                    businessMessage.status = BusinessMessageStatus.excute;

                    if (businessMessage.mAction != null){
                        businessMessage.mAction.doAction();
                    }
                }
            }

            super.dispatchMessage(msg);
        }
    }

    private void buildBusinessMessage(Message message, IBusinessAction action) {
        BusinessMessage businessMessage = new BusinessMessage();
        businessMessage.msg = message;
        businessMessage.mAction = action;

        synchronized (mBusinessMessages) {
            mBusinessMessages.add(businessMessage);
        }
    }

    private BusinessMessage findBusinessMessage(Message message) {
        synchronized (mBusinessMessages) {
            for (BusinessMessage businessMessage : mBusinessMessages) {
                if (businessMessage.msg == message) {
                    return businessMessage;
                }
            }
        }

        return null;
    }

    private void removeBusinessMessage(BusinessMessage message) {
        if (message == null) {
            return;
        }

        synchronized (mBusinessMessages) {
            mBusinessMessages.remove(message);
        }
    }

    private class BusinessMessage {
        public Message msg;
        public IBusinessAction mAction;
        public int token;
        public BusinessMessageStatus status = BusinessMessageStatus.init;

        public boolean hasExcuted() {
            return status != BusinessMessageStatus.init;
        }
    }

    private enum BusinessMessageStatus {
        init, excute;
    }
}
