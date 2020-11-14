package com.mutual.emove;

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

public class Progreso extends AppCompatActivity {

    //paciente
    private String idPaciente;
    private String codigoPaciente;

    Integer diaActual=0;
    //recyclerView
    Toolbar toolbar;
    RecyclerView recyclerViewSemana1;
    RecyclerView recyclerViewSemana2;
    RecyclerView recyclerViewSemana3;
    RecyclerView recyclerViewSemana4;

    List<DiaActividad> semana1ActividadList = new ArrayList<>();
    List<DiaActividad> semana2ActividadList = new ArrayList<>();
    List<DiaActividad> semana3ActividadList = new ArrayList<>();
    List<DiaActividad> semana4ActividadList = new ArrayList<>();


    DiaActividadAdapter semana1ActividadAdapter;
    DiaActividadAdapter semana2ActividadAdapter;
    DiaActividadAdapter semana3ActividadAdapter;
    DiaActividadAdapter semana4ActividadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);
        Bundle miBundle= this.getIntent().getExtras();

        if(miBundle!=null){
            setIdPaciente(miBundle.getString("id"));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Titulo");
        builder.setMessage(getIdPaciente());
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        recyclerViewSemana1 = findViewById(R.id.recyclerview1);
        recyclerViewSemana2 = findViewById(R.id.recyclerview2);
        recyclerViewSemana3 = findViewById(R.id.recyclerview3);
        recyclerViewSemana4 = findViewById(R.id.recyclerview4);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(this);
        linearLayoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);
        LinearLayoutManager linearLayoutManager4 = new LinearLayoutManager(this);
        linearLayoutManager4.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewSemana1.setLayoutManager(linearLayoutManager1);
        recyclerViewSemana2.setLayoutManager(linearLayoutManager2);
        recyclerViewSemana3.setLayoutManager(linearLayoutManager3);
        recyclerViewSemana4.setLayoutManager(linearLayoutManager4);
        cargarDiasActividad();

    }


    private boolean cargarDiasActividad() {
        String url =  EmoveSingleton.getInstance(getApplicationContext()).getUrl() +"/app/movil/avanceDia";
        url=url.replace(" ","%20");
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                        Toast.makeText(getApplicationContext(),response, Toast.LENGTH_SHORT).show();

                        try {
                            //Creating JsonObject from response String
                            JSONObject jsonObject= new JSONObject(response.toString());

                            //extracting json array from response string
                            String status=jsonObject.getString("status");

                            //  JSONObject jsonRow = jsonArray.getJSONObject(0);
                            //get value from jsonRow
                            //  String resultStr = jsonRow.getString("result");
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
                                            if(i<7){
                                                semana1ActividadList.add(diaActividad);
                                            }else if(i>6 & i<14){
                                                semana2ActividadList.add(diaActividad);
                                            }else if(i>13 & i<21){
                                                semana3ActividadList.add(diaActividad);
                                            }else if(i>20 & i<28){
                                                semana4ActividadList.add(diaActividad);
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
                //textView.setText("That didn't work!");
                Toast.makeText(getApplicationContext(),"No se pudo conectar "+error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("id", getIdPaciente());
                return params;
            }
        };
        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
        EmoveSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        return true;
    }

    public void RecyclerViewDia(){
        semana1ActividadAdapter = new DiaActividadAdapter(semana1ActividadList);
        semana2ActividadAdapter = new DiaActividadAdapter(semana2ActividadList);
        semana3ActividadAdapter = new DiaActividadAdapter(semana3ActividadList);
        semana4ActividadAdapter = new DiaActividadAdapter(semana4ActividadList);
        recyclerViewSemana1.setAdapter(semana1ActividadAdapter);
        recyclerViewSemana2.setAdapter(semana2ActividadAdapter);
        recyclerViewSemana3.setAdapter(semana3ActividadAdapter);
        recyclerViewSemana4.setAdapter(semana4ActividadAdapter);
        semana1ActividadAdapter.notifyDataSetChanged();
        semana2ActividadAdapter.notifyDataSetChanged();
        semana3ActividadAdapter.notifyDataSetChanged();
        semana4ActividadAdapter.notifyDataSetChanged();
    }

    public void regresar(View view) {
            finish();
    }

//recyclerView


    //datos Paciente
    public String getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(String idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getCodigoPaciente() {
        return codigoPaciente;
    }

    public void setCodigoPaciente(String codigoPaciente) {
        this.codigoPaciente = codigoPaciente;
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