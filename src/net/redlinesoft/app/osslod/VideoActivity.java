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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class VideoActivity extends Activity {

	private AdView adView;
	
	int data_size = 0; 
	public ArrayList<HashMap<String, String>> MyArrList = new ArrayList<HashMap<String, String>>();
	public HashMap<String, String> map;
	ListView listItem;

	String url_head = "http://query.yahooapis.com/v1/public/yql?q=select%20title%2Clink%20from%20feed%20where%20url%3D%22https%3A%2F%2Fgdata.youtube.com%2Ffeeds%2Fapi%2Fplaylists%2F";
	String url_tail = "%3Fv%3D2%26max-results%3D50%22%20and%20link.rel%3D%22alternate%22&format=json&callback=";
	String url="";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main); 
		setProgressBarIndeterminateVisibility(false);

		// break policy
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
 
		// get xml data form yql
		if (checkNetworkStatus()) {
			
			// get data and complete url
			Intent intent= getIntent(); 
			final String title = intent.getStringExtra("title");
			final String playlist = intent.getStringExtra("playlist");
			url= url_head + playlist + url_tail;
			this.setTitle(title);

			// Create the adView
			adView = new AdView(this, AdSize.BANNER, "a151174be34d211");
			// Lookup your LinearLayout assuming it’s been given
			// the attribute android:id="@+id/mainLayout"
			LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
			// Add the adView to it
			layout.addView(adView);
			// Initiate a generic request to load it with an ad
			adView.loadAd(new AdRequest());
			
			new  LoadContentAsync().execute();
			
		} else {
			Toast.makeText(getBaseContext(), "No network connection!",Toast.LENGTH_SHORT).show(); 
		}
	}
	
	public class LoadContentAsync extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// load json data
			try {
				JSONObject json_data = new JSONObject(getJSONUrl(url));
				JSONObject json_query = json_data.getJSONObject("query");
				JSONObject json_result = json_query.getJSONObject("results");
				JSONArray json_entry = json_result.getJSONArray("entry");
				Log.d("JSON", String.valueOf(json_entry.length()));
				
				for (int i = 0; i < json_entry.length(); i++) {
					// parse json
					JSONObject c = json_entry.getJSONObject(i);
					Log.d("JSON", c.getString("title").toString());
					Log.d("JSON", c.getJSONObject("link").getString("href").toString());
					String link = c.getJSONObject("link").getString("href").toString();
					String[] fragments = link.split("&");
					String[] videoid = fragments[0].split("=");
					Log.d("JSON", videoid[1]);

					// put into hashmap
					map = new HashMap<String, String>();
					map.put("title", c.getString("title"));
					map.put("link", c.getJSONObject("link").getString("href"));
					map.put("videoid", videoid[1]);
					MyArrList.add(map);

				}

				data_size = json_entry.length();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			ShowResult(MyArrList);
			setProgressBarIndeterminateVisibility(false);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
		}		
	}
	
	public void ShowResult(ArrayList<HashMap<String, String>> myArrList) { 
		if (data_size>0) {
			ListView listItem = (ListView) findViewById(R.id.listItem);	
			LazyAdapter adapter = new LazyAdapter(this, MyArrList);
			listItem.setAdapter(adapter);				
			listItem.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					Intent fanPageIntent = new Intent(Intent.ACTION_VIEW);
					fanPageIntent.setType("text/url");
					fanPageIntent.setData(Uri.parse(MyArrList.get(arg2).get("link")));
					startActivity(fanPageIntent);			
				}
			});
		}
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//etMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
