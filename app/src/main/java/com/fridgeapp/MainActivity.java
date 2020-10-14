package com.fridgeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    static MyAdapter adapter;
    static DateFormat dateForm = DateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag("en"));

    public static void addItem(Item item){
        adapter.add(item);
        adapter.items.sort(new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if(o1.k > o2.k) return -1;
                if(o1.k < o2.k) return 1;
                return 0;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView txt = findViewById(R.id.textView);
        txt.setText("Date: " + dateForm.format(Calendar.getInstance().getTime()));

        adapter = new MyAdapter(this, android.R.layout.simple_list_item_1, R.id.textView2, new ArrayList<Item>());
        initiallizeList();
        //////
//        testListView(2);
        //////
        ListView lst = findViewById(R.id.listView);
        lst.setAdapter(adapter);

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });
    }


    private void initiallizeList(){
        SQLiteDatabase db = openOrCreateDatabase("foodItems", MODE_PRIVATE, null);
//        db.execSQL("DROP TABLE IF EXISTS Items");
        db.execSQL("CREATE TABLE IF NOT EXISTS Items (Name VARCHAR, Date BIGINT, Bitmap BIGINT);");
        Cursor cursor = db.rawQuery("SELECT * FROM Items", null);

        if(cursor.moveToFirst()){
            String name = cursor.getString(0);
            Date date = new Date(cursor.getLong(1));
            byte[] map = cursor.getBlob(2);

            if(map.length > 2)
                addItem(new Item(name, date, BitmapFactory.decodeByteArray(map, 0, map.length)));
            else
                addItem(new Item(name, date));

            while (cursor.moveToNext()){
                name = cursor.getString(0);
                date = new Date(cursor.getLong(1));
                map = cursor.getBlob(2);
                adapter.add(new Item(name, date));

                if(map.length > 2)
                    adapter.items.get(adapter.getCount() - 1).image = BitmapFactory.decodeByteArray(map, 0, map.length);
            }
        }
        db.close();
    }


    @Override
    protected void onStop() {
        super.onStop();
        ArrayList<Item> items = adapter.items;

        SQLiteDatabase db = openOrCreateDatabase("foodItems", MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS Items");
        db.execSQL("CREATE TABLE IF NOT EXISTS Items (Name VARCHAR, Date BIGINT, Bitmap BIGINT);");

        for (Item item : items) {
//            db.execSQL("INSERT INTO Items VALUES ('"+ item.name +"', "+ item.BBD.getTime() +");");
            try {
                ContentValues cv = new ContentValues();
                cv.put("Name", item.name);
                cv.put("Date", item.BBD.getTime());
                if(item.image != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    item.image.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    cv.put("Bitmap", bos.toByteArray());
                }
                else
                    cv.put("Bitmap", new byte[]{0});
                db.insert("Items", null, cv);
            }catch (Exception ex){
                int a = 1;
            }
        }
        db.close();
        //super.finish();
    }

    private class MyAdapter extends ArrayAdapter<Item> {
        ArrayList<Item> items;
        public MyAdapter(Context context, int resource, int textViewResourceId, ArrayList items){
            super(context, resource, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View row = inflater.inflate(R.layout.list_item, parent, false);
            final Item item = adapter.getItem(position);

            ImageView image = row.findViewById(R.id.imageView);

            if(item.image == null)
                image.setImageResource(R.drawable.food1);
            else
                image.setImageBitmap(item.image);

            ImageButton btn = row.findViewById(R.id.imageButton2);
            btn.setImageResource(R.drawable.delete);


            TextView text = row.findViewById(R.id.textView2);
            text.setText(items.get(position).name + "\n" + dateForm.format(items.get(position).BBD));

            final float[] col = new float[]{151, 100, 73};
            final float[] col2 = new float[]{151, 20, -27};
            final ConstraintLayout clt = row.findViewById(R.id.constraitLayout);



            float k = item.k;
            clt.setBackgroundColor(Color.HSVToColor(new float[]{col[0] - col2[0]*k, col[1] - col2[1]*k, col[2] - col2[2]*k}));

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new
                            AlertDialog.Builder(MainActivity.this);
                    dialog.setMessage("Delete " + item.name + "?");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Yes", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final int height = clt.getHeight();

                                    //clt.setBackgroundColor(Color.rgb(0,185,96));
                                    //clt.setBackgroundColor(Color.rgb(154,10,0));

                                    final int mills = 500;

                                    new CountDownTimer(mills, 5){
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            millisUntilFinished = mills - millisUntilFinished;
                                            clt.setBackgroundColor(Color.HSVToColor(new float[]{col[0] - col2[0]*millisUntilFinished/mills,
                                                    col[1] - col2[1]*millisUntilFinished/mills, col[2] - col2[2]*millisUntilFinished/mills}));

                                            clt.setMaxHeight((int)(height - height*millisUntilFinished/mills));
                                        }

                                        @Override
                                        public void onFinish() {
                                            adapter.remove(item);
                                        }
                                    }.start();
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
            });

            return row;
        }
    }

    private void testListView(int count){
        for (int i = 0; i < count; i++)
            adapter.items.add(new Item("Milk_" + i, Calendar.getInstance().getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.show_fio)
        {
            androidx.appcompat.app.AlertDialog.Builder dialog = new
                    androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
            try {
                dialog.setMessage(getTitle().toString()+ " версия "+
                        getPackageManager().getPackageInfo(getPackageName(),0).versionName +
                        "\r\n\n" + //Программа с примером выполнения диалогового окна\r\n\n " +
                        "Автор - Александров Михаил БПИ184-2");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            dialog.setTitle("О программе");
            dialog.setNeutralButton("OK", new
                    DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            dialog.setIcon(R.mipmap.ic_launcher_round);
            androidx.appcompat.app.AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
