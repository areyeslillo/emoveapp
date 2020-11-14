package com.mutual.emove;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mutual.emove.entidades.DiaActividad;
import com.mutual.emove.recyclerView.DiaActividadAdapter;
import com.mutual.emove.entidades.EmoveSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public Integer getVersionApp() {
        return versionApp;
    }

    //para actualizaciones
    public Integer versionApp=1;

    //paciente
    private String usuario;
    private static MediaPlayer mediaPlayer;
    private String identificador;
    Integer diaActual=0;
    //recyclerView
    Toolbar toolbar;
    RecyclerView recyclerView;
    List<DiaActividad> diaActividadList = new ArrayList<>();
    DiaActividadAdapter diaActividadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cargarSesion();
        actualizacion();
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
    public void eliminar_usuario(View view) {
        SharedPreferences preferences= getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString("user", null);
        editor.putString("identificador", null);
        editor.commit();
        finish();
    }

    private  void cargarSesion(){
        SharedPreferences preferences= getSharedPreferences("credenciales",Context.MODE_PRIVATE);
        usuario = preferences.getString("user", null);
        identificador = preferences.getString("identificador", null);
        if (usuario==null && identificador == null){
            Intent MiIntent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(MiIntent);
        }else {
            Toast.makeText(getApplicationContext(),"Bienvenido: " + usuario, Toast.LENGTH_LONG).show();
            recyclerView = findViewById(R.id.recyclerview1);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            cargarDiasActividad();
        }
    }

    private boolean cargarDiasActividad() {
        String url =  EmoveSingleton.getInstance(getApplicationContext()).getUrl() +"/app/movil/avanceDia";
        url=url.replace(" ","%20");
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Creating JsonObject from response String
                            JSONObject jsonObject= new JSONObject(response.toString());
                            //extracting json array from response string
                            String status=jsonObject.getString("status");
                            if (status.equals("error")){
                                Toast.makeText(getApplicationContext(),"Error al cargar información del día, por favor intentelo nuevamente", Toast.LENGTH_LONG).show();
                            }
                            else if(status.equals("success")){
                                JSONArray jsonArray = jsonObject.getJSONArray("avanceDia");
                                JSONArray json =jsonArray;
                                try{
                                    for(int i=0;i<json.length();i++) {
                                        jsonObject = json.getJSONObject(i);
                                        if(jsonObject.getString("dia")!=null && jsonObject.getString("estado")!=null) {
                                            DiaActividad diaActividad = new DiaActividad(jsonObject.getString("dia"), jsonObject.getString("estado"));
                                            diaActividadList.add(diaActividad);
                                            if(diaActividad.getEstado().equals("Actual")){
                                                diaActual=i;
                                            }
                                        }
                                    }
                                    RecyclerViewDia();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"No se pudo conectar "+error.toString(), Toast.LENGTH_SHORT).show();
                cargarDiasActividad();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("id", getIdentificador());
                return params;
            }
        };
        // Add the request to the RequestQueue.
        EmoveSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        return true;
    }

    public void RecyclerViewDia(){
        diaActividadAdapter = new DiaActividadAdapter(diaActividadList);
        recyclerView.setAdapter(diaActividadAdapter);
        recyclerView.scrollToPosition(diaActual); //use to focus the item with index
        diaActividadAdapter.notifyDataSetChanged();
    }

    public void comenzar_actividad(View view) {
//        Toast.makeText(getApplicationContext(),"se debe iniciar ejercicio.", Toast.LENGTH_LONG).show();
        ultimoEjercicio();
    }
    public void Progreso(View view) {
       Intent MiIntent = new Intent(MainActivity.this, Progreso.class);
        Bundle MiBundle= new Bundle();
        MiBundle.putString("id", getIdentificador());
        MiIntent.putExtras(MiBundle);
        startActivity(MiIntent);
    }
    void ultimoEjercicio(){
        String url =  EmoveSingleton.getInstance(getApplicationContext()).getUrl() +"/app/movil/ultimoejercicio";
        url=url.replace(" ","%20");
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Creating JsonObject from response String
                            JSONObject jsonObject= new JSONObject(response.toString());

                            //extracting json array from response string
                            String status=jsonObject.getString("status");
                            //  JSONObject jsonRow = jsonArray.getJSONObject(0);
                            //get value from jsonRow
                            //  String resultStr = jsonRow.getString("result");
                            if (status.equals("error")){
                                String msg=jsonObject.getString("message");
                                //ete error en tiempo de espera
                                if (msg.equals("ete")){
                                    Toast.makeText(getApplicationContext(),"aún no puedes realizar los ejercicios (5 minutos en pruebas).", Toast.LENGTH_LONG).show();
                                }
                                //eac error actividades completadas
                                else if (msg.equals("eac")){
                                    Toast.makeText(getApplicationContext(),"has completado las actividades del dia.", Toast.LENGTH_LONG).show();
                                }
                            }
                            else if(status.equals("success")){
                                Intent MiIntent = new Intent(MainActivity.this, Ejercicio1.class);
                                Bundle MiBundle= new Bundle();
                                MiBundle.putString("id",getIdentificador());
                                MiBundle.putString("serie",jsonObject.getString("proximaactividad"));
                                MiIntent.putExtras(MiBundle);
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
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("idPaciente", getIdentificador());
                return params;
            }
        };
        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
        EmoveSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
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

    private boolean actualizacion() {
        String url =  EmoveSingleton.getInstance(getApplicationContext()).getUrl() +"/version";
        url=url.replace(" ","%20");
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Creating JsonObject from response String
                            JSONObject jsonObject= new JSONObject(response.toString());
                            //extracting json array from response string
                            String versionString=jsonObject.getString("version");
                            int version =0;
                            try {
                                version = Integer.parseInt(versionString);
                            } catch(NumberFormatException nfe) {
                            }
                            if (version > getVersionApp()){
                                String linkDescarga=EmoveSingleton.getInstance(getApplicationContext()).getUrl()+"/apk/"+jsonObject.getString("url");
                                Toast.makeText(getApplicationContext(),"Existe una actualización", Toast.LENGTH_LONG).show();
                                Bundle miBundle= new Bundle();
                                miBundle.putString("url",linkDescarga);
                                Intent Update = new Intent(MainActivity.this, Update.class);
                                Update.putExtras(miBundle);
                                startActivity(Update);
                            }
                            else{
                            }
                        } catch (JSONException e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"No se pudo conectar "+error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                return params;
            }
        };
        // Add the request to the RequestQueue.
        EmoveSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        return true;
    }

}