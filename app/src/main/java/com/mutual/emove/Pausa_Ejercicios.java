package com.mutual.emove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mutual.emove.entidades.DiaActividad;
import com.mutual.emove.entidades.EmoveSingleton;
import com.mutual.emove.entidades.Paciente;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Pausa_Ejercicios extends AppCompatActivity {
    private String idPaciente;
    private String serie;
    private String ejerciciosRealizados;
    private String tiempo;
    private String idEjercicio;
    Button btn_continuar;
    public CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pausa__ejercicios);
        btn_continuar= findViewById(R.id.btn_continuar);
        btn_continuar.setEnabled(false);
        Bundle miBundle= this.getIntent().getExtras();
        if(miBundle!=null){
            setIdPaciente(miBundle.getString("id"));
            setSerie(miBundle.getString("serie"));
            setEjerciciosRealizados(miBundle.getString("ejerciciosRealizados"));
            setIdEjercicio(miBundle.getString("idEjercicio"));
            setTiempo(miBundle.getString("tiempo"));
        }

        TextView textViewDescanso = (TextView)findViewById(R.id.textViewDescanso);
        TextView textViewRepeticiones = (TextView)findViewById(R.id.textViewRepeticiones);
        String mensajeRepeticiones="Ejercicio "+ idEjercicio +" realizado\n"+ ejerciciosRealizados +" de 40 Repeticiones";
        textViewRepeticiones.setText(mensajeRepeticiones);
        //guardar resultados ejercicio
        guardarEjercicio();
        countDownTimer = new CountDownTimer(180000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutos = millisUntilFinished/60000;
                long segundos = millisUntilFinished%60000/1000;
                String seg;
                if(segundos<=9){
                    seg="0"+segundos;
                }else{
                    seg=Long.toString(segundos);
                }
                if(millisUntilFinished<=170000){
                    btn_continuar.setEnabled(true);
                    textViewDescanso.setText("¡ya puedes comenzar!\n"+minutos+":"+seg);
                }else{
                    textViewDescanso.setText("Tiempo restante para comenzar:\n"+minutos+":"+seg);
                }
            }

            public void onFinish() {
                textViewDescanso.setText("¡Se ha acabado el tiempo de descanso!");
                btn_continuar.callOnClick();
            }
        }.start();
        btn_continuar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String ejercicio = getIdEjercicio();
                Bundle PausaBundle= new Bundle();
                PausaBundle.putString("id",idPaciente);
                PausaBundle.putString("serie",getSerie());
                countDownTimer.cancel();

                //    Toast.makeText(getApplicationContext(),"id de usuario"+idPaciente, Toast.LENGTH_LONG).show();

                switch (ejercicio) {
                    case "1":
                        Intent MiIntentEj2 = new Intent(Pausa_Ejercicios.this, Ejercicio2.class);
                        MiIntentEj2.putExtras(PausaBundle);
                        startActivity(MiIntentEj2);
                        finish();
                        break;
                    case "2":
                        Intent MiIntentEj3 = new Intent(Pausa_Ejercicios.this, Ejercicio3.class);
                        MiIntentEj3.putExtras(PausaBundle);
                        startActivity(MiIntentEj3);
                        finish();
                        break;
                    case "3":
                        Intent MiIntentEj4 = new Intent(Pausa_Ejercicios.this, Ejercicio4.class);
                        MiIntentEj4.putExtras(PausaBundle);
                        startActivity(MiIntentEj4);
                        finish();
                        break;
                    case "4":
                        Intent MiIntentEj5 = new Intent(Pausa_Ejercicios.this, Ejercicio5.class);
                        MiIntentEj5.putExtras(PausaBundle);
                        startActivity(MiIntentEj5);
                        finish();
                        break;
                    case "5":
                        finish();
                        break;
                    default:
                        finish();
                }
            }
        });
    }
    public String getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(String idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getEjerciciosRealizados() {
        return ejerciciosRealizados;
    }

    public void setEjerciciosRealizados(String ejerciciosRealizados) {
        this.ejerciciosRealizados = ejerciciosRealizados;
    }

    public String getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(String idEjercicio) {
        this.idEjercicio = idEjercicio;
    }
    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    //Salvar datos Ejercicio
    private boolean guardarEjercicio() {
       // Toast.makeText(getApplicationContext(),idEjercicio +" "+ serie +" "+ tiempo+" "+ejerciciosRealizados, Toast.LENGTH_SHORT).show();
        String url =  EmoveSingleton.getInstance(getApplicationContext()).getUrl() +"/app/movil/ejercicio";
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
                                Toast.makeText(getApplicationContext(),"Error al guardar información, por favor intentelo nuevamente", Toast.LENGTH_LONG).show();
                            }
                            else if(status.equals("success")){
                              //  Toast.makeText(getApplicationContext(),"Guardado correctamente", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //textView.setText("That didn't work!");
                Toast.makeText(getApplicationContext(),"No se pudo conectar "+error.toString(), Toast.LENGTH_SHORT).show();
                guardarEjercicio();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("idEjercicio", getIdEjercicio());
                params.put("serie", getSerie());
                params.put("tiempo", getTiempo());
                params.put("ejerciciosRealizados", getEjerciciosRealizados());
                return params;
            }
        };
        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
        EmoveSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        return true;
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