package com.mutual.emove;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;
public class Ejercicio4 extends AppCompatActivity {
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
    private ExternalTextureConverter converter;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;
    private static TextView textEstado;
    private static TextView textFinger;
    private static TextView textRepeticiones;
    private  static boolean estado=false;
    static Handler handler = new Handler();
    private static final int TIEMPO = 500;

    // finger states
    // finger states
    private  static boolean contacto1 = false;
    private  static boolean contacto2 = false;
    private  static boolean contacto3 = false;
    private  static boolean contacto4 = false;
    private  static boolean contacto5 = false;
    private  static boolean thumbIsOpen = false;
    private  static boolean firstFingerIsOpen = false;
    private  static boolean secondFingerIsOpen = false;
    private  static boolean thirdFingerIsOpen = false;
    private  static boolean fourthFingerIsOpen = false;
    private static Context mContext;
    private static int estadoEjercicio=0;
    private static int contadorEjercicio=0;
    private static MediaPlayer mediaPlayer ;
    Button btn_continuar;
    private String idPaciente;
    private String serie;
    private int tiempoEjercicio;
    CountDownTimer countDownTimer;
    float distance;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicio4);
        textEstado = findViewById(R.id.textEstado);
        textFinger = findViewById(R.id.textFinger);
        textRepeticiones = findViewById(R.id.textRepeticiones);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.ejercicio);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f,0.5f);
        mediaPlayer.start();
        mContext=getApplicationContext();
        Bundle miBundle= this.getIntent().getExtras();
        btn_continuar= findViewById(R.id.btn_progreso);

        if(miBundle!=null){
            setIdPaciente(miBundle.getString("id"));
            setSerie(miBundle.getString("serie"));
        }

        TextView textView = (TextView)findViewById(R.id.textView3);

        countDownTimer = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutos = millisUntilFinished/60000;
                long segundos = millisUntilFinished%60000/1000;
                String seg;
                if(segundos<=9){
                    seg="0"+segundos;
                }else{
                    seg=Long.toString(segundos);
                }
                textView.setText(minutos+":"+seg);
             //   textView.setText(Float.toString(distance));
                setTiempoEjercicio(getTiempoEjercicio()+1);
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
                        Log.d(TAG,"[TS:" + packet.getTimestamp() + "] Hand presence is false, no hands detected.");
                    }
                });

        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                    try {
                        LandmarkProto.NormalizedLandmarkList landmarks = LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
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

    private String getLandmarksDebugString(LandmarkProto.NormalizedLandmarkList landmarks) {
        int landmarkIndex = 0;
        float  vectorX = 0;
        float  vectorY = 0;


        String landmarksString = "";
        for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
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
            vectorX=landmark.getX();
            vectorY=landmark.getY();
            ++landmarkIndex;
        }
        if (isThumbNearFirstFinger(landmarks.getLandmark(8),landmarks.getLandmark(12))){
            contacto1=true;
        } else {
            contacto1=false;
        }

        if (vectorX==0 && vectorY==0 && estado==true){
            //  Estado.setText("No se reconoce la mano");
            estado=false;
            //cambiarEstado();

        }else if (vectorX!=0 && vectorY!=0 && estado==false){
            //  Estado.setText("Mano reconocida");
            estado=true;
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

    private void cambiarEstado(){
        handler.postDelayed(new Runnable() {
            public void run() {
                if (estado==true){
                    textEstado.setText("Mano Reconocida");
                }
                else {
                    textEstado.setText("No se reconoce la mano");
                }
                if (contacto1==true){
                    textFinger.setText("Ahora abra la mano");
                    estadoEjercicio=1;
                }
                else if(contacto1!=true ) {
                    if (estadoEjercicio==1){
                        contadorEjercicio++;
                        MediaPlayer mediaPlayer2 = MediaPlayer.create(mContext, R.raw.ejercicio_correcto);
//                        mediaPlayer.setLooping(true);
                        mediaPlayer2.start();
                        textFinger.setText("Ahora cierre la mano");
                        estadoEjercicio=0;
                        textRepeticiones.setText(Integer.toString(contadorEjercicio)+"/40");
                    }
                    if(contadorEjercicio>=40){
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
        converter = new ExternalTextureConverter(eglManager.getContext());
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
                    previewDisplayView.setVisibility(View.VISIBLE);
                });
        cameraHelper.isCameraRotated();
        cameraHelper.startCamera(this, CAMERA_FACING, /*surfaceTexture=*/ null);
    }
    public void finalizarEjercicio(){
        btn_continuar.setEnabled(false);
        countDownTimer.cancel();
        onPause();
        Intent MiIntent = new Intent(Ejercicio4.this, Pausa_Ejercicios.class);
        Bundle MiBundle= new Bundle();
        MiBundle.putString("id",getIdPaciente());
        MiBundle.putString("serie",getSerie());
        MiBundle.putString("ejerciciosRealizados",Integer.toString(contadorEjercicio));
        MiBundle.putString("tiempo",Integer.toString(getTiempoEjercicio()));
        MiBundle.putString("idEjercicio","4");
        MiIntent.putExtras(MiBundle);
        startActivity(MiIntent);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        finish();
    }
    static float get_Euclidean_DistanceAB(float a_x, float a_y, float b_x, float b_y)
    {
        double x1 = a_x;
        double y1 = a_y;
        double x2 = b_x;
        double y2 = b_y;
        double x = Math.sqrt(Math.pow(x2 - x1, 2) - Math.pow(y2 - y1, 2));
        return (float) x;
    }
    boolean isThumbNearFirstFinger(LandmarkProto.NormalizedLandmark point1, LandmarkProto.NormalizedLandmark point2)
    {
        distance = get_Euclidean_DistanceAB(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        Log.d(TAG,"el valor de la distancia es:" + Float.toString(distance));
        return distance < 0.04;
    }
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}