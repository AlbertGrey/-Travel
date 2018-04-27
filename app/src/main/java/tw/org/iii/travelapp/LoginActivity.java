package tw.org.iii.travelapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.githang.statusbar.StatusBarCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private Button loginbtn;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private RequestQueue queue;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private boolean issign;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //變更通知列底色
        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#4f4f4f"));

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        queue= Volley.newRequestQueue(LoginActivity.this);

        sp = getSharedPreferences("memberdata",MODE_PRIVATE);
        editor = sp.edit();
        issign = sp.getBoolean("signin",true);

        callbackManager = CallbackManager.Factory.create();

        loginbtn = findViewById(R.id.login_button2);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,LoginPage.class);
                startActivity(intent);
            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
//        loginButton.setFragment(this);
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                String user_id = accessToken.getUserId();
                String token = accessToken.getToken();

                GraphRequest request =
                        GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String email = object.getString("email");
                                    String birthday = object.getString("birthday");
                                    String name = object.getString("name");
                                    sighin(email,name,"123","fb");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle params = new Bundle();
                params.putString("fields","id,name,email,gender,birthday");
                request.setParameters(params);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
                SignOut();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void newmember(View view) {
        Uri uri = Uri.parse("https://topic-timgyes123.c9users.io/phoneregister.html");
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);

    }

    private void sighin(String mail,String name,String password,String type){
        final String p1=mail;
        final String p2=password;
        final String p3=type;
        final String p4=name;
        String url = HomePageActivity.urlIP + "/fsit04/app/sighin";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String res = response.trim();
                        if (res.equals("erro")){
                            Log.v("grey","error="+response);
                        }else{
                            try {
                                JSONObject j2 = new JSONObject(res);
                                String mid = j2.getString("id");
                                String memail = j2.getString("mail");
                                Log.v("grey","success");
                                Log.v("grey","mid = "+mid);
                                Log.v("grey","memail = "+memail);

                                editor.putBoolean("signin",true);
                                editor.putString("memberid",mid);
                                editor.putString("memberemail",memail);
                                editor.commit();
                                Log.v("grey","logicbooleanpage = "+(issign?true:false));
                                signsuccess();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }, null){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> m1 =new HashMap<>();
                m1.put("mail",p1);
                m1.put("password", p2);
                m1.put("type",p3);
                m1.put("name",p4);
                return m1;
            }
        };
        queue.add(stringRequest);
    }
    private void signsuccess(){
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("")
                .setMessage("success")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(),HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }).show();
    }

    private void SignOut(){

        new AlertDialog.Builder(LoginActivity.this)
                .setTitle("")
                .setMessage("sign out")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        editor.putBoolean("signin",false);
                        editor.putString("memberid","");
                        editor.putString("memberemail","");
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(),HomePageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }).show();
    }
}
