package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
     ListView songs;
     String[] items;
     int totalsongs;






    @Override
    protected void onResume() {
        super.onResume();

        ListView button1 = (ListView)findViewById(R.id.Listview_Song);
        button1.setEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songs=findViewById(R.id.Listview_Song);
        RuntimePermission();

    }


    public void RuntimePermission()                //it asks for permisiion for access storage    Dexter Library..
    {
           Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
               @Override
               public void onPermissionsChecked(MultiplePermissionsReport report) {
                   DisplaySongs();
               }

               @Override
               public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                   token.continuePermissionRequest();
               }
           }).check();

    }

    public ArrayList<File> findSong(File file)            // a method to check for all MP3 files in storage...and update them in music player list
    {


        ArrayList<File> arrayList= new ArrayList<>();
        File[] files= file.listFiles();

        for(File singleFile: files)
        {
            if(singleFile.isDirectory() && !singleFile.isHidden())      //here we are checking if file is a folder is yes then make a recursive call and pass that folder to chack it insides.
            {
                arrayList.addAll(findSong(singleFile));
            }

            else
            {
                if(singleFile.getName().endsWith(".mp3")|| singleFile.getName().endsWith(".wav"))   //check if file is music file
                {
                    arrayList.add(singleFile);
                }


            }
        }
        return arrayList;
    }


    public void DisplaySongs() // a method to display all songs
    {
        final ArrayList<File> mysongs= findSong(Environment.getExternalStorageDirectory());           //here my song will store all the music files
        items= new String[mysongs.size()];                       //items string with size of total no of songs
        totalsongs=mysongs.size();
        for(int i=0;i<mysongs.size();i++)                    //here we will iterate on each song and get its name...
        {
            items[i]=mysongs.get(i).getName().replace(".mp3","").replace("mp3","");

        }


        //this was a basic default array adapter

     /*   ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);     //it will show songs in listview
        songs.setAdapter(myAdapter);

      */


     // we implemented a new array adapter a custom one

        customAdapter customAdapter=new customAdapter();
        songs.setAdapter(customAdapter);                   // here all the data of listview.xml will be sent in the final list view


 //we are seeting an on click listner on the listview songs
        songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id)

            {
                songs.setEnabled(false);
                disablefor1sec(view);
                 String currsong= (String) items[position];
                 startActivity(new Intent(getApplicationContext(),Player_activity.class)
                         .putExtra("songs" ,mysongs )
                         .putExtra("songname",currsong)
                         .putExtra("position",position));



            }

        });

    }


    class customAdapter extends BaseAdapter
    {


        @Override
        public int getCount() {
         return  totalsongs;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View myview =getLayoutInflater().inflate(R.layout.list_items,null);          //here we set data on  list items.xml layout
            TextView textsong=myview.findViewById(R.id.text_songname);
            textsong.setSelected(true);
            textsong.setText(items[position]);
              return myview;
        }
    }

    public static void disablefor1sec(final View v) {
        try {
            v.setEnabled(false);
            v.setAlpha((float) 0.5);
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        v.setEnabled(true);
                        v.setAlpha((float) 1.0);
                    } catch (Exception e) {
                        Log.d("disablefor1sec", " Exception while un hiding the view : " + e.getMessage());
                    }
                }
            }, 100);
        } catch (Exception e) {
            Log.d("disablefor1sec", " Exception while hiding the view : " + e.getMessage());
        }
    }
}
