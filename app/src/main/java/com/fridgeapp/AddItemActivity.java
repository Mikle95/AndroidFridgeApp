package com.fridgeapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddItemActivity extends Activity {

    EditText nameTxt;
    TextView dateTxt;
    ImageView icon;
    ImageView camera;
    ImageView delete;
    ImageView scan;
    Button accept;

    Date date = new Date();
    Bitmap bitmap = null;
    boolean withBitmap = false;

    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialize();

        accept.setOnClickListener(acceptListener);

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(AddItemActivity.this);
                dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        try {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, month, dayOfMonth);
                            date = calendar.getTime();
                            dateTxt.setText(MainActivity.dateForm.format(date));
                        }catch (Exception ex){
                            Toast.makeText(AddItemActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();
            }
        });

        delete.setOnClickListener(deleteListener);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });

        final IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanIntegrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            findProductById(scanContent);
        }
        else {
            try{
                bitmap = (Bitmap) data.getExtras().get("data");
                icon.setImageBitmap(bitmap);
                withBitmap = true;
            } catch (Exception ex){
                Toast toast = Toast.makeText(getApplicationContext(),
                        "No data received!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    public void findProductById(String id){
        mDBHelper = new DatabaseHelper(this);

        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        try {
            Cursor cursor = mDb.rawQuery("SELECT * FROM items WHERE id=" + id, null);
            cursor.moveToFirst();
            nameTxt.setText(cursor.getString(1));
        }catch (Exception ex){}
    }

    private void initialize(){
        nameTxt = findViewById(R.id.editText);
        dateTxt = findViewById(R.id.dateTxt);
        camera = findViewById(R.id.cameraBtn);
        delete = findViewById(R.id.deleteBtn);
        icon = findViewById(R.id.itemIcon);
        scan = findViewById(R.id.scannerBtn);
        accept = findViewById(R.id.button2);

        icon.setImageResource(R.drawable.food1);
        camera.setImageResource(R.drawable.camera);
        scan.setImageResource(R.drawable.scanner);
        delete.setImageResource(R.drawable.delete);
        dateTxt.setText(MainActivity.dateForm.format(date));
    }


    View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialog = new
                    AlertDialog.Builder(AddItemActivity.this);
            dialog.setMessage("Delete photo?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Yes", new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            icon.setImageResource(R.drawable.food1);
                            withBitmap = false;
                            bitmap = null;
                        }
                    });
            dialog.setNegativeButton("No", new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
    };

    View.OnClickListener acceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialog = new
                    AlertDialog.Builder(AddItemActivity.this);
            dialog.setMessage("Add this item?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Yes", new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(date.getTime() < new Date().getTime() ||
                                    nameTxt.getText().toString().equals(getResources().getString(R.string.prodName)) ||
                                    nameTxt.equals(""))
                            {
                                Toast.makeText(AddItemActivity.this, "wrong Data", Toast.LENGTH_LONG).show();
                                return;
                            }

                            MainActivity.addItem(new Item(nameTxt.getText().toString(), date, bitmap));
                            finish();
                        }
                    });
            dialog.setNegativeButton("No", new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
    };
}
