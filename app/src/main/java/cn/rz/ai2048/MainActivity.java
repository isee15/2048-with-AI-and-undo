package cn.rz.ai2048;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.z.ai.AI2;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webViewGame;
    private static Map<Long, Long> bitM = new HashMap<Long, Long>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.webViewGame = (WebView) findViewById(R.id.webViewGame);
        WebSettings settings = webViewGame.getSettings();
        String packageName = getPackageName();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        webViewGame.addJavascriptInterface(this, "external");

        // If there is a previous instance restore it in the webview
        if (savedInstanceState != null) {
            webViewGame.restoreState(savedInstanceState);
        } else {
            webViewGame.loadUrl("file:///android_asset/2048/index.html");
        }
        long m = 2;
        bitM.put(0L, 0L);
        for (int i = 1; i < 16; ++i) {
            bitM.put(m, (long) i);
            m = m << 1;

        }
    }

    private static int[] dirUni = {0, 2, 3, 1};

    @JavascriptInterface
    public String execute(String method, String param) {
        Log.e("ai", method + ": " + param);
        int dir = -1;
        if ("getbest".equalsIgnoreCase(method)) {
            try {
                JSONTokener jsonParser = new JSONTokener(param);
                JSONObject grid = (JSONObject) jsonParser.nextValue();
                // 接下来的就是JSON对象的操作了
                String gridData = grid.getString("grid");
                String[] data = gridData.split(",");
                BigInteger board = BigInteger.valueOf(0);
                for (int i = 0; i < 16; i++) {
                    board = board.add(BigInteger.valueOf(
                            bitM.get(Long.parseLong(data[i]))).shiftLeft(
                            4 * i));
                }
                dir = AI2.getAIResult(board.toString());
                if (dir >= 0 && dir <= 3) {
                    dir = dirUni[dir];
                }

            } catch (JSONException ex) {
                // 异常处理代码
                Log.e("ex", ex.getMessage());
            }
        } else if ("log".equalsIgnoreCase(method)) {
            Log.e("jslog", param);
        }
        return "{\"move\":" + dir + "}";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webViewGame.saveState(outState);
    }

    public static Bitmap takeScreenShot(Activity activity) {
        // 获取windows中最顶层的view
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        Point outSize = new Point();
        display.getSize(outSize);
        // 获取屏幕宽和高
        int widths = outSize.x;
        int heights = outSize.y;

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
    }

    public void savePic(Bitmap b, File strFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                MediaStore.Images.Media.insertImage(this.getContentResolver(), b, strFileName.getName(), "2048share");
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "file not found",
                    Toast.LENGTH_LONG).show();
            Log.v("-----error-----", e.getMessage());
        } catch (IOException e) {
            Toast.makeText(this, "io not found",
                    Toast.LENGTH_LONG).show();
            Log.v("-----error-----", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {

            Bitmap bm = this.takeScreenShot(this);

            File fileName = new File(this.getFilesDir() + "/2048s.png");
            if (!fileName.getParentFile().exists()) {
                fileName.getParentFile().mkdir();
            }
            Log.v("-----save png-----", fileName.getName());

            this.savePic(bm, fileName);

            Intent shareIntent = new Intent();
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "这2048 也太容易了！https://github.com/isee15/2048-with-AI-and-undo");
            shareIntent.putExtra("Kdescription",
                    "这2048 也太容易了！https://github.com/isee15/2048-with-AI-and-undo");
            shareIntent.setAction(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                Uri uri= FileProvider.getUriForFile(this,"cn.rz.ai2048.fileprovider",fileName);
                Log.i("info", "saveScreenshot: "+uri);

                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");
            } else {
                shareIntent.setDataAndType(Uri.fromFile(fileName), "image/*");
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            startActivity(Intent.createChooser(shareIntent, "分享到"));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
