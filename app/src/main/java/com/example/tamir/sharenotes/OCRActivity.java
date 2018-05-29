package com.example.tamir.sharenotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.quickblox.chat.model.QBChatDialog;

import java.io.IOException;

public class OCRActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView ocrText;
    Button send;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;

    //Checks if Camera permission is enabled in Manifest and starts camera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case RequestCameraPermissionID:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
                    return;
                }
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        ocrText = (TextView) findViewById(R.id.ocrtxt);
        send = (Button) findViewById(R.id.BUTTON_SEND);


        //Creates a new Text Recogniser object which processes images and determines the text within the images
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        //Ensures OCR available on device
        if (!textRecognizer.isOperational()) {
            Log.w("OCRActivity", "Detector dependencies are not available");
        } else {
            //Camera source used to read text from camera view live
            //Set high resolution so that text can be viewed clearly
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            //Checks if camera permission enabled in manifest then Requests permission from user
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(OCRActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            //Processor required to read text from camera
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                //This method receives TextBlocks from the text recogniser
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0)
                    {
                        ocrText.post(new Runnable() {
                            @Override
                            public void run() {
                                //Creates a new string using the text detected
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i=0;i<items.size();++i)
                                {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                //Sets the text to the TextView
                                ocrText.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }

        //Sends the text back to the chat message activity
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OCRActivity.this,ChatMessageActivity.class);
                //Keeps the activity in the state it was so that it is the same chat dialog
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //Gets the text from TextView and passes to intent
                String OCR = ocrText.getText().toString();
                intent.putExtra("OCR",OCR);
                startActivity(intent);
                finish();
            }
        });
    }
}
