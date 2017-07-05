package scan.simple.zhang.com.simplescan.zbar;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;

public class ProcessDataTask extends AsyncTask<Void, Void, String> {
    private Camera mCamera;
    private byte[] mData;
    private Delegate mDelegate;

    public ProcessDataTask(Camera camera, byte[] data, Delegate delegate) {
        mCamera = camera;
        mData = data;
        mDelegate = delegate;
    }

    public ProcessDataTask perform() {
        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            execute();
        }
        return this;
    }

    public void cancelTask() {
        if (getStatus() != Status.FINISHED) {
            cancel(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mDelegate = null;
    }

    @Override
    protected String doInBackground(Void... params) {
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        byte[] rotatedData = new byte[mData.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedData[x * height + height - y - 1] = mData[x + y * width];
            }
        }
        int tmp = width;
        width = height;
        height = tmp;

        try {
            if (mDelegate == null) {
               // LogUtil.d("扫码抛出异常，mDelegate为空");
                return null;
            }
            return mDelegate.processData(rotatedData, width, height, false);
        } catch (Exception e1) {
            try {
              //  LogUtil.d("扫码抛出异常1："+ e1.getMessage());
                return mDelegate.processData(rotatedData, width, height, true);
            } catch (Exception e2) {
                //LogUtil.d("扫码抛出异常2："+e2.getMessage());
                return null;
            }
        }
    }

    public interface Delegate {
        String processData(byte[] data, int width, int height, boolean isRetry);
    }
}
