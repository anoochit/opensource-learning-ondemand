package net.redlinesoft.app.osslod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.ads.*;

import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	private AdView adView;

	String url = "https://raw.github.com/anoochit/opensource-learning-ondemand/master/playlist.json";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// break policy
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		//ProgressDialog progress = ProgressDialog.show(this, null,"loading...",false);

		// get xml data form yql
		if (checkNetworkStatus()) {

			// Create the adView
			adView = new AdView(this, AdSize.SMART_BANNER, "a151174be34d211");
			// Lookup your LinearLayout assuming it’s been given
			// the attribute android:id="@+id/mainLayout"
			LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
			// Add the adView to it
			layout.addView(adView);
			// Initiate a generic request to load it with an ad
			adView.loadAd(new AdRequest());

			// load json data
			try {
				JSONArray json_data = new JSONArray(getJSONUrl(url));

				final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> map;

				for (int i = 0; i < json_data.length(); i++) {

					// parse json
					JSONObject c = json_data.getJSONObject(i);
					Log.d("JSON", c.getString("title").toString());
					Log.d("JSON", c.getString("playlist").toString());

					// put into hashmap
					map = new HashMap<String, String>();
					map.put("title", c.getString("title"));
					map.put("playlist", c.getString("playlist"));
					MyArrList.add(map);

					ListView listItem = (ListView) findViewById(R.id.listItem);					
					SimpleAdapter adapter;		 
					adapter = new SimpleAdapter(MainActivity.this, MyArrList, R.layout.activity_playlist_column,			 
					new String[] {"title"}, new int[] {R.id.ColTitle});				 
					listItem.setAdapter(adapter);
					
					listItem.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							Intent newActivity = new Intent(MainActivity.this,VideoActivity.class);
			            	newActivity.putExtra("title", MyArrList.get(arg2).get("title"));
			            	newActivity.putExtra("playlist", MyArrList.get(arg2).get("playlist"));
			            	startActivity(newActivity);						
						}
					
					});

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(getBaseContext(), "cannot load data !",
						Toast.LENGTH_SHORT).show();

			}

		} else {
			Toast.makeText(getBaseContext(), "No network connection!",
					Toast.LENGTH_SHORT).show();

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public String getJSONUrl(String url) {
		StringBuilder str = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) { // Download OK
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					str.append(line);
				}
			} else {
				Log.e("Log", "Failed to download file..");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str.toString();
	}

	public boolean checkNetworkStatus() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

}
