package itstep.learning.android_212;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import itstep.learning.android_212.orm.NbuRate;

public class RateActivity extends AppCompatActivity {
    private final String nbuUrl = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private TextView tvDemo;
    private List<NbuRate> nbuRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rate);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvDemo = findViewById( R.id.rate_tv_demo );
        new Thread( this::loadRates ).start();
    }

    private void loadRates() {
        try( InputStream urlStream = new URL( nbuUrl ).openStream() ) {
            String content = readStreamToString( urlStream );
            JSONArray arr = new JSONArray( content ) ;
            nbuRates = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                nbuRates.add( NbuRate.fromJsonObject( arr.getJSONObject( i ) ) ) ;
            }
            runOnUiThread( () -> tvDemo.setText( content ) );
        }
        catch (MalformedURLException ex) {
            Log.e("RateActivity::loadRates", "MalformedURLException: " + ex.getMessage());
        }
        catch (IOException ex) {
            Log.e("RateActivity::loadRates", "IOException: " + ex.getMessage());
        }
        catch( JSONException ex ) {
            Log.e("RateActivity::loadRates", "JSONException: " + ex.getMessage());
        }
    }

    private String readStreamToString( InputStream inputStream ) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        int len;
        while( ( len = inputStream.read( buffer ) ) > 0 ) {
            byteBuilder.write( buffer, 0, len );
        }
        return byteBuilder.toString() ;
    }
}
/*
Робота з мережею Інтернет.
Основу виконання запитів становить java.net.URL
Він є аналогом File для доступу до файлів, у т.ч. в тому, що створення програмного
об'єкту не виконує мережних дій.
Реальне звернення до мережі відбувається при підключенні або відкритті потоку.
І при цьому є ряд зауважень:
- android.os.NetworkOnMainThreadException - всі запити мають здійснюватись асинхронно,
    причому в окремому потоці.

- java.lang.SecurityException: Permission denied (missing INTERNET permission?)
    робота з мережею блокується дозволами. Запит на дозвіл включається до маніфесту.
    <uses-permission android:name="android.permission.INTERNET"/>

- android.view.ViewRootImpl$CalledFromWrongThreadException:
    Only the original thread that created a view hierarchy can touch its views.
    Для передачі роботи до основного потоку вживається метод runOnUiThread
 */