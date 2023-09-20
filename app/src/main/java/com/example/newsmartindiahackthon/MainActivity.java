package com.example.newsmartindiahackthon;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.newsmartindiahackthon.ml.Crop32000;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class MainActivity extends AppCompatActivity {

    Button camera, gallery,copbut;
    ImageView imageView;
    TextView result;
    int imageSize = 256;
    String summa="";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        copbut=findViewById(R.id.copbut);
        copbut.setVisibility(View.INVISIBLE);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        getSupportActionBar().hide();
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 3);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                }
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });
        copbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b=new Bundle();
                b.putString("query",summa);
                Intent i=new Intent(MainActivity.this,SearchResult.class);
                i.putExtras(b);
                startActivity(i);
            }
        });
    }

    public void classifyImage(Bitmap image){
        try {
            Crop32000 model = Crop32000.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer=ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int []intValues=new int[imageSize*imageSize];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel=0;
            for(int i=0;i<imageSize;i++)
                for(int j=0;j<imageSize;j++)
                {
                    int val=intValues[pixel++];
                    byteBuffer.putFloat(((val>>16)&0xFF)*(1.f/1));
                    byteBuffer.putFloat(((val>>8)&0xFF)*(1.f/1));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/1));
                }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Crop32000.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float []confidence=outputFeature0.getFloatArray();
            int maxpos=0;
            float maxconfi=0;
            for(int i=0;i<confidence.length;i++)
            {
                //System.out.println(confidence[i]);
                if(confidence[i]>maxconfi)
                {
                    maxconfi=confidence[i];
                    maxpos=i;
                }
            }
            String classes[]={"Apple_Apple scab","Apple_Black Rot","Apple_Cedar apple rust","Apple_healthy","Corn (maize)_Cercospora leaf spot Gray leaf spot","Corn (maize)_Common rust","Corn (maize)_Northern Leaf Blight","Corn (maize)_Healthy","Tomato_Bacterial spot","Tomato_Early blight",
                    "Tomato_Late blight","Tomato_Healthy","Cotton_Diseased leaf","Cotton_Diseased plant","Cotton_Healthy Leaf","Cotton_Healthy plant"};
            // Releases model resources if no longer used.
            String res="";
            System.out.println(maxconfi);
            String []resarr=classes[maxpos].split("_");
            res+="Plant: "+resarr[0]+"\nDisease: "+resarr[1];
            summa+=resarr[0]+" "+resarr[1];
            result.setText(res);
            copbut.setVisibility(View.VISIBLE);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == 3){
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }else{
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}