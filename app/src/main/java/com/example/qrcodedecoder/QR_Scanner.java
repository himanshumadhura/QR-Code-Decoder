package com.example.qrcodedecoder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.Manifest;

import java.util.HashMap;
import java.util.Map;

public class QR_Scanner extends AppCompatActivity {
    SurfaceView cameraPreview;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private boolean isBarcodeDetected = false;
    Button scanBtn;
    String uid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        cameraPreview = findViewById(R.id.cameraPreview);
        scanBtn = findViewById(R.id.scan_again);
        scanBtn.setVisibility(View.INVISIBLE);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() > 0 && !isBarcodeDetected){
                    String qrCodeValue = barcodes.valueAt(0).displayValue;
                    isBarcodeDetected = true;

                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    if(firebaseAuth.getCurrentUser() != null){
                        uid = firebaseAuth.getCurrentUser().getUid();
                    }

                    FirebaseFirestore fireStoreDb = FirebaseFirestore.getInstance();
                    Map<String, Object> data = new HashMap<>();
                    data.put("Value", qrCodeValue);
                    fireStoreDb.collection("Values").document(uid)
                            .set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(QR_Scanner.this, "Value Stored Successfully", Toast.LENGTH_SHORT).show();
                                    scanBtn.setVisibility(View.VISIBLE);
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(QR_Scanner.this, "Error while storing value", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                    cameraPreview.setVisibility(View.INVISIBLE);
                    runOnUiThread(()-> Log.d("Himanshu", qrCodeValue));
                }
            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBarcodeDetected = false;
                cameraPreview.setVisibility(View.VISIBLE);
                startCameraPreview();
            }
        });

        startCameraPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBarcodeDetected = false;
        startCameraPreview();
    }

    private void startCameraPreview() {
        isBarcodeDetected = false;
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try{
                    if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(QR_Scanner.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                        return;
                    }
                    cameraSource.start(cameraPreview.getHolder());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraSource != null){
            cameraSource.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_REQUEST){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startCameraPreview();
            }else{
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_qr, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.sign_out){
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(QR_Scanner.this, GoogleSignIn.class);
            startActivity(i);
            finish();
        } else if (item.getItemId() == R.id.about) {
            Intent i = new Intent(QR_Scanner.this, About.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}