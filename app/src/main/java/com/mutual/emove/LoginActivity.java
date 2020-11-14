package com.mutual.emove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mutual.emove.entidades.EmoveSingleton;
import com.mutual.emove.entidades.Paciente;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    TextView txtCodigo;
    private static MediaPlayer mediaPlayer;
    private ProgressBar pgsBar;
    Button btn_ingresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //reproducir sonido
        mediaPlayer = MediaPlayer.create(this, R.raw.menu);
        mediaPlayer.setVolume(0.1f, 0.1f);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        pgsBar = (ProgressBar) findViewById(R.id.pBar);
        txtCodigo= (TextView) findViewById(R.id.codigo_paciente);
        btn_ingresar= findViewById(R.id.btn_login);
    }

    public void onClick(View view) {
        pgsBar.setVisibility(view.VISIBLE);
        btn_ingresar.setEnabled(false);
        cargarWebSercive();
    }
    //Volley
//%20 es espacio

    private void cargarWebSercive() {

        String url = EmoveSingleton.getInstance(getApplicationContext()).getUrl()+"/app/movil/login";
        // Toast.makeText(getApplicationContext(),url, Toast.LENGTH_LONG).show();
        // url=url.replace(" ","%20");
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //  Toast.makeText(getApplicationContext(),response, Toast.LENGTH_SHORT).show();
                        try {
                            //Creating JsonObject from response String
                            JSONObject jsonObject= new JSONObject(response.toString());
                            //extracting json array from response string
                            String status=jsonObject.getString("status");

                            //  JSONObject jsonRow = jsonArray.getJSONObject(0);
                            //get value from jsonRow
                            //  String resultStr = jsonRow.getString("result");
                            Paciente paciente= new Paciente();
                            if (status.equals("error")){
                                Toast.makeText(getApplicationContext(),"Error en código de paciente, por favor ingréselo nuevamente", Toast.LENGTH_LONG).show();
                                btn_ingresar.setEnabled(true);
                                pgsBar.setVisibility(View.GONE);
                            }
                            else if(status.equals("success")){
                                JSONArray jsonArray = jsonObject.getJSONArray("paciente");

                                JSONArray json =jsonArray;
                                try{
                                    for(int i=0;i<json.length();i++) {
                                        jsonObject=json.getJSONObject(i);
                                        paciente.setId(jsonObject.getString("id"));
                                        paciente.setCodigo(jsonObject.getString("codigo"));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                guardarSesion(paciente.getCodigo(),paciente.getId());
                                mediaPlayer.release();
                                mediaPlayer = null;
                                Intent MiIntent = new Intent(LoginActivity.this, MainActivity.class);
                                finish();
                                startActivity(MiIntent);

                            }
                        } catch (JSONException e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //textView.setText("That didn't work!");
                Toast.makeText(getApplicationContext(),"No se pudo conectar "+error.toString(), Toast.LENGTH_SHORT).show();
                btn_ingresar.setEnabled(true);
                pgsBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("codigo", txtCodigo.getText().toString());
                return params;
            }
        };
        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
        EmoveSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }
    private  void guardarSesion(String usuario, String identificador){
        SharedPreferences preferences= getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString("user", usuario);
        editor.putString("identificador", identificador);
        editor.commit();
    }
    //Pantalla Completa
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}