package com.mutual.emove;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;

import androidx.camera.core.ImageCapture;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import androidx.camera.core.CameraX;
//import androidx.camera.core.CameraX.LensFacing;
import androidx.camera.core.Preview;
//import androidx.camera.core.PreviewConfig;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.mediapipe.glutil.ShaderUtil;
import com.google.protobuf.InvalidProtocolBufferException;

public class Ejercicio1 extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String OUTPUT_HAND_PRESENCE_STREAM_NAME = "hand_presence";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;

    // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
    // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
    // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
    // corner, whereas MediaPipe in general assumes the image origin is at top-left.
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private CustomExternalTextureConverter converter;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;
    private static TextView textEstado;
    private static TextView textFinger;
    private static TextView textRepeticiones;
    private static boolean estado = false;
    static Handler handler = new Handler();
    private static final int TIEMPO = 100;

    // finger states
    private static boolean thumbIsOpen = false;
    private static boolean firstFingerIsOpen = false;
    private static boolean secondFingerIsOpen = false;
    private static boolean thirdFingerIsOpen = false;
    private static boolean fourthFingerIsOpen = false;
    private static float KeyPointTempthumbIsOpen ;
    private static float KeyPointTempfirstFingerIsOpen ;
    private static float KeyPointTempsecondFingerIsOpen ;
    private static float KeyPointTempthirdFingerIsOpen ;
    private static float KeyPointTempfourthFingerIsOpen ;
    private static Context mContext;
    private static int estadoEjercicio = 0;
    private static int contadorEjercicio = 0;
    private static MediaPlayer mediaPlayer;
    Button btn_continuar;
    private String idPaciente;
    private String serie;
    private int tiempoEjercicio;
    CountDownTimer countDownTimer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio1);
        textEstado = findViewById(R.id.textEstado);
        textFinger = findViewById(R.id.textFinger);
        textRepeticiones = findViewById(R.id.textRepeticiones);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ejercicio);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.start();
        mContext = getApplicationContext();
        Bundle miBundle = this.getIntent().getExtras();
        btn_continuar = findViewById(R.id.btn_progreso);

        if (miBundle != null) {
            setIdPaciente(miBundle.getString("id"));
            setSerie(miBundle.getString("serie"));
        }

        TextView textView = (TextView) findViewById(R.id.textView3);

        countDownTimer = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutos = millisUntilFinished / 60000;
                long segundos = millisUntilFinished % 60000 / 1000;
                String seg;
                if (segundos <= 9) {
                    seg = "0" + segundos;
                } else {
                    seg = Long.toString(segundos);
                }
                textView.setText(minutos + ":" + seg);
                setTiempoEjercicio(getTiempoEjercicio() + 1);
            }

            public void onFinish() {
                textView.setText("¡Se ha acabado el tiempo!");
                finalizarEjercicio();
            }
        }.start();

        cambiarEstado();
        previewDisplayView = new SurfaceView(this);


        setupPreviewDisplayView();
        AndroidAssetUtil.initializeNativeAssetManager(this);
        eglManager = new EglManager(null);
        processor =
                new FrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);

        processor.addPacketCallback(
                OUTPUT_HAND_PRESENCE_STREAM_NAME,
                (packet) -> {
                    Boolean handPresence = PacketGetter.getBool(packet);
                    if (!handPresence) {
                        Log.d(TAG, "[TS:" + packet.getTimestamp() + "] Hand presence is false, no hands detected.");
                    }
                });

        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                    try {
                        NormalizedLandmarkList landmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
                        if (landmarks == null) {
                            Log.d(TAG, "[TS:" + packet.getTimestamp() + "] No hand landmarks.");
                            return;
                        }
                        // Note: If hand_presence is false, these landmarks are useless.
                        Log.d(
                                TAG,
                                "[TS:"
                                        + packet.getTimestamp()
                                        + "] #Landmarks for hand: "
                                        + landmarks);
                        Log.d(TAG, getLandmarksDebugString(landmarks));
                    } catch (InvalidProtocolBufferException e) {
                        Log.e(TAG, "Couldn't Exception received - " + e);
                        return;
                    }
                });
        PermissionHelper.checkAndRequestCameraPermissions(this);

    }

    private static String getLandmarksDebugString(NormalizedLandmarkList landmarks) {
        int landmarkIndex = 0;
        float vectorX = 0;
        float vectorY = 0;


        String landmarksString = "";
        for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
            landmarksString +=
                    "\t\tLandmark["
                            + landmarkIndex
                            + "]: ("
                            + landmark.getX()
                            + ", "
                            + landmark.getY()
                            + ", "
                            + landmark.getZ()
                            + ")\n";
            vectorX = landmark.getX();
            vectorY = landmark.getY();
            ++landmarkIndex;
        }

        //dar 2 condiciones dedos extendidos o en postura

        if  (landmarks.getLandmark(5).getX() >= landmarks.getLandmark(6).getX() && landmarks.getLandmark(7).getX() >= landmarks.getLandmark(8).getX()) {
            firstFingerIsOpen = true;
            KeyPointTempfirstFingerIsOpen=landmarks.getLandmark(8).getY();
            Log.d(TAG, "dedo 1 arriba");
        } else {
            firstFingerIsOpen = false;
            Log.d(TAG, "dedo 1 abajo");
        }
        if (landmarks.getLandmark(9).getY() >= landmarks.getLandmark(10).getY() && landmarks.getLandmark(11).getY() >= landmarks.getLandmark(12).getY()) {
            secondFingerIsOpen = true;
            KeyPointTempsecondFingerIsOpen=landmarks.getLandmark(12).getY();
            Log.d(TAG, "dedo 2 arriba");
        } else {
            secondFingerIsOpen = false;
            Log.d(TAG, "dedo 2 Abajo");
        }
        if (landmarks.getLandmark(13).getY() >= landmarks.getLandmark(14).getY() && landmarks.getLandmark(15).getY() >= landmarks.getLandmark(16).getY()) {
            thirdFingerIsOpen = true;
            KeyPointTempthirdFingerIsOpen=landmarks.getLandmark(16).getY();
            Log.d(TAG, "dedo 3 Arriba");

        } else {
            thirdFingerIsOpen = false;
            Log.d(TAG, "dedo 3 Abajo");

        }
        if (landmarks.getLandmark(17).getY() >= landmarks.getLandmark(18).getY() && landmarks.getLandmark(19).getY() >= landmarks.getLandmark(20).getY()){
            fourthFingerIsOpen = true;
            KeyPointTempfourthFingerIsOpen=landmarks.getLandmark(20).getY();
            Log.d(TAG, "dedo 4 Arriba");

        } else  {
            fourthFingerIsOpen = false;
            Log.d(TAG, "dedo 4 Abajo");

        }
        if (landmarks.getLandmark(2).getY() >= landmarks.getLandmark(3).getY() && landmarks.getLandmark(3).getY() >= landmarks.getLandmark(4).getY()) {
            thumbIsOpen = true;
            Log.d(TAG, "dedo 5 Arriba");

        } else {
            thumbIsOpen = false;
            Log.d(TAG, "dedo 5 Abajo");

        }

        if (vectorX == 0 && vectorY == 0 && estado == true) {
            //  Estado.setText("No se reconoce la mano");
            estado = false;
            //cambiarEstado();

        } else if (vectorX != 0 && vectorY != 0 && estado == false) {
            //  Estado.setText("Mano reconocida");
            estado = true;
            //cambiarEstado();

        }
        return landmarksString;
    }

    public String getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(String idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getTiempoEjercicio() {
        return tiempoEjercicio;
    }

    public void setTiempoEjercicio(int tiempoEjercicio) {
        this.tiempoEjercicio = tiempoEjercicio;
    }

    private void cambiarEstado() {
        handler.postDelayed(new Runnable() {
            public void run() {
                if (estado == true) {
                    textEstado.setText("Mano Reconocida");
                } else {
                    textEstado.setText("No se reconoce la mano");
                }
                if (firstFingerIsOpen == true && secondFingerIsOpen == true && thirdFingerIsOpen == true && fourthFingerIsOpen == true && thumbIsOpen == true) {
                    textFinger.setText("Ahora cierra la mano");
                    estadoEjercicio = 1;
                } else if (firstFingerIsOpen != true && secondFingerIsOpen != true && thirdFingerIsOpen != true && fourthFingerIsOpen != true && thumbIsOpen == true) {
                    if (estadoEjercicio == 1) {
                        contadorEjercicio++;
                        MediaPlayer mediaPlayer2 = MediaPlayer.create(mContext, R.raw.ejercicio_correcto);
//                        mediaPlayer.setLooping(true);
                        mediaPlayer2.start();
                        textFinger.setText("Ahora abre la mano");
                        estadoEjercicio = 0;
                        textRepeticiones.setText(Integer.toString(contadorEjercicio) + "/40");
                    }
                    if (contadorEjercicio >= 40) {
                        finalizarEjercicio();
                    }
                }
                handler.postDelayed(this, TIEMPO);
            }
        }, TIEMPO);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        converter = new ExternalTextureConverter(eglManager.getContext());
        converter = new CustomExternalTextureConverter(eglManager.getContext(),2,270);

        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();
    }

    @Override
    public void onVisibleBehindCanceled() {
        // App-specific method to stop playback and release resources
        //sonido a segundo plano
        super.onVisibleBehindCanceled();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);

        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);
        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());

                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                // (Re-)Compute the ideal size of the camera-preview display (the area that the
                                // camera-preview frames get rendered onto, potentially with scaling and rotation)

                                // based on the size of the SurfaceView that contains the display.
                                Size viewSize = new Size(width, height);
                                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);

                                // Connect the converter to the camera-preview frames as its input (via
                                // previewFrameTexture), and configure the output width and height as the computed
                                // display size.
                                converter.setSurfaceTextureAndAttachToGLContext(
                                        previewFrameTexture, displaySize.getWidth(), displaySize.getHeight());
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

    public void regresar(View view) {
        finalizarEjercicio();
    }

    public class CustomSurfaceTexture extends SurfaceTexture {
        public CustomSurfaceTexture(int texName) {
            super(texName);
            init();
        }

        private void init() {
            super.detachFromGLContext();
        }
    }

    private void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    previewFrameTexture = surfaceTexture;
                    // Make the display view visible to start showing the preview. This triggers the
                    // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
                    // added
                    // To update the SurfaceTexture, we have to remove it and re-add it
                    ViewGroup viewGroup = (ViewGroup) previewDisplayView.getParent();
                    viewGroup.removeView(previewDisplayView);
                    viewGroup.addView(previewDisplayView);
                    // added
                    previewDisplayView.setVisibility(View.VISIBLE);
                });
        cameraHelper.isCameraRotated();
        cameraHelper.startCamera(this, CAMERA_FACING, /*surfaceTexture=*/ null, new Size(1920, 1200));
    }

    public void finalizarEjercicio() {
        btn_continuar.setEnabled(false);
        countDownTimer.cancel();
        onPause();
        Intent MiIntent = new Intent(Ejercicio1.this, Pausa_Ejercicios.class);
        Bundle MiBundle = new Bundle();
        MiBundle.putString("id", getIdPaciente());
        MiBundle.putString("serie", getSerie());
        MiBundle.putString("ejerciciosRealizados", Integer.toString(contadorEjercicio));
        MiBundle.putString("tiempo", Integer.toString(getTiempoEjercicio()));
        MiBundle.putString("idEjercicio", "1");
        MiIntent.putExtras(MiBundle);
        startActivity(MiIntent);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        finish();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
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
