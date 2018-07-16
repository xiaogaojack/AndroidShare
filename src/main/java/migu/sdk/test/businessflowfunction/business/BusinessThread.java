package migu.sdk.test.businessflowfunction.business;

import android.os.Looper;

/**
 * Created by xiang on 2018/7/5.
 */

public class BusinessThread extends Thread {
    private Looper mLooper;
    @Override
    public void run() {
        Looper.prepare();
            synchronized (this) {
                mLooper = Looper.myLooper();
                notifyAll();
        }
        Looper.loop();
    }

    public Looper getLooper(){
        if (!isAlive()) {
            return null;
        }

        // If the thread has been started, wait until the looper has been created.
        synchronized (this) {
            while (isAlive() && mLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return mLooper;
    }
}
