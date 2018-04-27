package tw.org.iii.travelapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.githang.statusbar.StatusBarCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by wei-chengni on 2018/4/22.
 */

public class SearchPage extends AppCompatActivity {
    private Toolbar toolbar;
    private RequestQueue queue;
    private ListView listView;
    private SearchAdapter adapter;
    private LinkedList<AttrListModel> data;
    private String jstring;
    private SearchAsync searchAsync;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issign;
    private String memberid;
    private ViewHolder holder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));
        toolbar = findViewById(R.id.search_toolbar);
        toolbar.inflateMenu(R.menu.search_menu);
        setSupportActionBar(toolbar);
        queue= Volley.newRequestQueue(SearchPage.this);
        listView = findViewById(R.id.search_list);

        sp = getSharedPreferences("memberdata",MODE_PRIVATE);
        editor = sp.edit();
        issign = sp.getBoolean("signin",true);
        memberid = sp.getString("memberid","0");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_real_btn).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return false;
            }
        });
        return true;
    }

    private void doSearch(String param) {

        final String p1 =param;
        String url = String.format(HomePageActivity.urlIP + "/fsit04/Allviews");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new SearchAsync().execute(response);
                    }
                }, null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> m1 =new HashMap<>();
                m1.put("param",p1);
                return m1;
            }
        };
        queue.add(stringRequest);
    }

    private class SearchAsync extends AsyncTask <String ,Void,LinkedList<AttrListModel>>{

        @Override
        protected LinkedList<AttrListModel> doInBackground(String... strings) {
            String jstring = strings[0];
            data = new LinkedList<>();
            try {
                JSONArray jsonArray = new JSONArray(jstring);
                for(int i=0;i<jsonArray.length();i++){
                    ArrayList<String> photo_url = new ArrayList<>();
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    JSONArray imgarray = jsonObject2.getJSONArray("Img");
                    for(int y = 0; y < imgarray.length(); y++){
                        String imgUrl = imgarray.getJSONObject(y).getString("url");
                        photo_url.add(imgUrl);
                    }
                    JSONObject jsonObject3 = imgarray.getJSONObject(0);
                    AttrListModel listModel = new AttrListModel();
                    listModel.setAid(jsonObject2.getString("total_id"));
                    listModel.setName(jsonObject2.getString("name"));
                    listModel.setAddress(jsonObject2.getString("address"));
                    listModel.setOpentime(jsonObject2.getString("MEMO_TIME"));
                    listModel.setDescription(jsonObject2.getString("xbody"));
                    listModel.setImgs(jsonObject3.getString("url"));
                    listModel.setLat(jsonObject2.getDouble("lat"));
                    listModel.setLng(jsonObject2.getDouble("lng"));
                    listModel.setPhoto_url(photo_url);
                    data.add(listModel);
                }
                return data;
            } catch (Exception e) {
                Log.v("grey","error22 = " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(LinkedList jsonresult) {
            super.onPostExecute(jsonresult);
            adapter = new SearchAdapter(SearchPage.this,data);
            listView.setAdapter(adapter);
        }
    }

    public class SearchAdapter extends BaseAdapter{

        private Context context;
        private LayoutInflater inflater;
        private LinkedList<AttrListModel> data;
        private AttrListModel reslut = new AttrListModel();
        private TextView itemtitle;
        private TextView itemaddr;

        public SearchAdapter(Context context,
                             LinkedList<AttrListModel> linklist){
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.data = linklist;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            reslut = data.get(i);
            if(view==null){
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.item_layout,viewGroup,false);
                inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                holder.itemtitle = (TextView)view.findViewById(R.id.item_title);
                holder.itemaddress = (TextView)view.findViewById(R.id.item_addr);
                holder.itemimage = (ImageView)view.findViewById(R.id.item_image);

                view.setTag(holder);
                holder.mesbtn = view.findViewById(R.id.item_message_btn);
                holder.addbtn = view.findViewById(R.id.item_add_btn);
            }else{
                holder = (ViewHolder) view.getTag();
            }
            reslut = data.get(i);
            //set reslut to textview
            holder.itemtitle.setText(reslut.getName());
            holder.itemaddress.setText(reslut.getAddress());
            GlideApp.with(context).load(reslut.getImgs()).into(holder.itemimage);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reslut = data.get(i);
                    Intent intent = new Intent(SearchPage.this,DetailActivity.class);
                    intent.putExtra("total_id",reslut.getAid());
                    intent.putExtra("stitle",reslut.getName());
                    intent.putExtra("address",reslut.getAddress());
                    intent.putExtra("img_url",reslut.getImgs());
                    intent.putExtra("xbody",reslut.getDescription());
                    intent.putExtra("memo_time",reslut.getOpentime());
                    intent.putExtra("lat", reslut.getLat());
                    intent.putExtra("lng", reslut.getLng());
                    intent.putStringArrayListExtra("photos", reslut.getPhoto_url());
                    startActivity(intent);
                }
            });

            //addbtn
            holder.addbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (issign==true){
                        reslut = data.get(i);
                        addFavorite(memberid,reslut.getAid());
                        Log.v("grey",reslut.getAid());
                        addtest();
                    }else {
                        Intent intent = new Intent(SearchPage.this,LoginActivity.class);
                        startActivity(intent);
                    }

                }
            });
            //mesbtn
            holder.mesbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SearchPage.this,MessagePage.class);
                    startActivity(intent);
                }
            });
            return view;
        }
    }

    public class ViewHolder
    {
        public TextView itemtitle;
        public TextView itemaddress;
        public ImageView itemimage;
        public Button mesbtn,addbtn;
    }

    private void addtest(){
        new AlertDialog.Builder(SearchPage.this)
                .setTitle(" ")
                .setMessage("成功")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        finish();
                    }
                }).show();
    }

    private void addFavorite(String user_id,String total_id){
        String url =HomePageActivity.urlIP+"/fsit04/User_favorite";
        Log.v("grey","user_id = "+ user_id);
        Log.v("grey","total_id="+total_id);
        final String p1 =user_id;
        final String p2=total_id;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.v("grey","attress = "+response);
                    }
                }, null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> m1 =new HashMap<>();
                m1.put("user_id",p1);
                m1.put("total_id", p2);
                return m1;
            }
        };
        queue.add(stringRequest);

    }
}
