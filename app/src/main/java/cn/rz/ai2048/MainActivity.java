package cn.rz.ai2048;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

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

public class MainActivity extends Activity {

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
        webViewGame.saveState(outState);
    }

    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        // Rect frame = new Rect();
        // int width =
        // activity.getWindowManager().getDefaultDisplay().getWidth();
        // int height = activity.getWindowManager().getDefaultDisplay()
        // .getHeight();
        //
        // Bitmap b = Bitmap.createBitmap(b1, 0, 0, width, height);
        // view.destroyDrawingCache();
        return b1;
    }

    public static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            Log.v("-----error-----", e.getMessage());
        } catch (IOException e) {
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

            File dir = new File(Environment.getExternalStorageDirectory()
                    + "/data/2048/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = dir.getAbsolutePath() + "/2048s.png";
            Log.v("-----save png-----", fileName);
            this.savePic(bm, fileName);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            Uri uri = Uri.fromFile(new File(fileName));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "这2048 也太容易了！https://github.com/isee15/2048-with-AI-and-undo");
            shareIntent.putExtra("Kdescription",
                    "这2048 也太容易了！https://github.com/isee15/2048-with-AI-and-undo");
            startActivity(Intent.createChooser(shareIntent, "分享到"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
