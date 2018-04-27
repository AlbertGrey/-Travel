package tw.org.iii.travelapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.githang.statusbar.StatusBarCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback  {

    private GoogleMap mMap;
    private Marker[] marker;
    private MyDragView dragView;
    private MyListView listView;
    private MyAdapter adapter;
    private String stitle, img_url;
    private double lat, lng;
    private ArrayList<DataStation> dataList, dataList2;
    private RequestQueue queue;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issignin;
    private String memberid;
    private String memberemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));
        //sp
        sp = getSharedPreferences("memberdata",MODE_PRIVATE);
        editor = sp.edit();
        issignin = sp.getBoolean("signin",false);
        memberid = sp.getString("memberid","0");
        memberemail = sp.getString("memberemail","xxx");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //TODO 從intent拿到資料後把要呈現的資料加入到  destinations是要畫標記的陣列  跟  data是LISTVIEW要呈現的資料
        queue= Volley.newRequestQueue(this);
        dataList = new ArrayList<>();
        dataList2 = new ArrayList<>();
        getFavorite(memberid);
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute(
                HomePageActivity.urlIP +
                        "/fsit04/User_favorite?user_id=" +memberid);
//        marker = new Marker[destinations.size()];
    }
    //找出DragView 跟他裡面的ListView
    private  void getDragViewAndSetListView(){
        dragView = findViewById(R.id.myDragView);
        listView = dragView.getListView();
        adapter = new MyAdapter(this);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                mMap.clear();
                stitle = dataList.get(position).getStitle();
                lat = dataList.get(position).getLat();
                lng = dataList.get(position).getLng();

                MarkerOptions m2 =new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(stitle);
                mMap.addMarker(m2).showInfoWindow();
                //移動視角到哪個經緯度
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                //設定視角的大小程度
                mMap.moveCamera(CameraUpdateFactory.zoomTo(13.0f));
            }
        });
    }
    //地圖加載完成調用會傳入一個GoogleMap
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //這個經緯度是預設視角的位置
        LatLng taipei = new LatLng(25.047155, 121.514465);

        //移動視角到哪個經緯度
        mMap.moveCamera(CameraUpdateFactory.newLatLng(taipei));
        //設定視角的大小程度
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10.0f));
        //設定MarkClickListener
        mMap.setOnMarkerClickListener(new MyOnMarkerClickListener());

    }
    //在地圖上載入圖片
    private void addImg(){
        LatLng postion =new LatLng(25.171855,121.440422);
        GroundOverlayOptions imgOnMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.ds))
                .position(postion, 8600f, 6500f);
        mMap.addGroundOverlay(imgOnMap);
    }
    //開始導航
    public void gotoMap(View view) {
        // Search for restaurants nearby
        //google.navigation:q=a+street+address
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
    //標記的clickListener
    private class MyOnMarkerClickListener implements GoogleMap.OnMarkerClickListener{
        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng latLng =marker.getPosition();
            lat = latLng.latitude;
            lng = latLng.longitude;
            return false;
        }
    }

    protected class MyAdapter extends BaseAdapter {

        private Context myContext;
        private LayoutInflater inflater;

        public MyAdapter(Context context){
            this.myContext = context;
            inflater = LayoutInflater.from(this.myContext);
        }
        //取得list的數量
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //設置ListView的layout,沒有根目錄root值為null
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.sample_list, null);
            }
            //取得ListView layout的每個view
            TextView number = convertView.findViewById(R.id.sample_list_number);
            TextView title = convertView.findViewById(R.id.sample_list_stitle);
            //設定title
            String stitle = dataList.get(position).getStitle();
            number.setText("" + (position+1));
            title.setText(stitle);
            //回傳convertView
            return convertView;
        }
    }
    //解析JSON字串
    private void parseJSON(String s){
        try {
            JSONArray array = new JSONArray(s);
            for (int i=0; i<array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                JSONArray imgs = obj.getJSONArray("Img");
                JSONObject obj2 = imgs.getJSONObject(0);
                String total_id = obj.getString("total_id");
                String description = obj2.getString("description");
                img_url = obj2.getString("url");
                String cat2 = obj.getString("CAT2");
                String xbody = obj.getString("xbody");
                String address = obj.getString("address");
                stitle = obj.getString("name");
                String memo_time = obj.getString("MEMO_TIME");
                double lng = obj.getDouble("lng");
                double lat = obj.getDouble("lat");

                DataStation data = new DataStation(
                        total_id, stitle, img_url, xbody, lat, lng, address, memo_time);
                dataList.add(data);
            }
        } catch (JSONException e) {
            Log.v("brad", e.toString());
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = getData(strings[0]);
            parseJSON(data);
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getDragViewAndSetListView();
        }
        //從URL獲取JSON字串
        private String getData(String url){
            StringBuffer sb = new StringBuffer();
            try {
                URL newURL = new URL(url);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(newURL.openStream()));
                String line;
                while( (line = br.readLine()) != null){
                    sb.append(line);
                }
            } catch (IOException e) {
                Log.v("brad", e.toString());
            }
            return sb.toString();
        }
    }
    /** 取得我的最愛
     *
     * @param user_id 用戶id
     */
    private void getFavorite(String user_id){
        final String p1 = user_id;
        String getFavoriteUrl =
                HomePageActivity.urlIP +
                        "/fsit04/User_favorite?user_id=" + memberid;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getFavoriteUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseGetFavorite(response);
                    }
                }, null);
        queue.add(stringRequest);
    }
    /**  用戶我的最愛parseJSON
     *
     * @param response
     * total_id = 地點ID, name = 地點名稱, type = 地點類型, CAT2 = 分類
     * MEMO_TIME = 營業時間, address = 地址, xbody = 簡介
     * lat = 緯度, lng = 經度
     * description = 照片的描述, url = 照片的url
     */
    private void parseGetFavorite(String response){
        try {
            JSONArray array1 = new JSONArray(response);
            for(int i= 0;i<array1.length();i++) {
                ArrayList<String> photo_url = new ArrayList<>();
                JSONObject ob1 =array1.getJSONObject(i);
                String total_id = ob1.getString("total_id");
                String name = ob1.getString("name");
                String type= ob1.getString("type");
                String CAT2 = ob1.getString("CAT2");
                String MEMO_TIME = ob1.getString("MEMO_TIME");
                String address = ob1.getString("address");
                String xbody = ob1.getString("xbody");
                double lat = ob1.getDouble("lat");
                double lng = ob1.getDouble("lng");
                JSONArray imgs =ob1.getJSONArray("Img");
                for(int y = 0; y < imgs.length(); y++){
                    String imgUrl = imgs.getJSONObject(y).getString("url");
                    photo_url.add(imgUrl);
                }
                DataStation data = new DataStation(total_id, name, type, CAT2, MEMO_TIME,
                        address, xbody, lat, lng, photo_url);
                dataList2.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}