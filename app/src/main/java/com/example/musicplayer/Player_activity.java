package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;

import java.io.File;
import java.util.ArrayList;

public class Player_activity extends AppCompatActivity {
    public static final String Extra_name = "Song_name";
    static MediaPlayer mediaPlayer;
    Button play, next, prev, fastfrwd, fastrewind;
    TextView txtname, songstart, songend;
    BarVisualizer visualizer;
    SeekBar seekBar;
    ImageView imageView;
    ListView prevlist;
    int position;
    Thread updateseekbar;
    String sname;
    ArrayList<File> mysongs;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) ;
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override

    protected void onDestroy() {

        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_activity);


        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        fastfrwd = findViewById(R.id.fast_forward);
        fastrewind = findViewById(R.id.fast_rewind);
        txtname = findViewById(R.id.textview1);
        songend = findViewById(R.id.txtsend);
        songstart = findViewById(R.id.txtsstart);
        seekBar = findViewById(R.id.seek_bar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.imagesong);

        if (mediaPlayer != null)                   //if someold song is playing  then stop and release it
        {
            mediaPlayer.stop();
            mediaPlayer.release();

        }

        Intent i = getIntent();             //get the data passed from previous activity
        Bundle bundle = i.getExtras();       //we can use bundle to store data from intent


        mysongs = (ArrayList) bundle.getParcelableArrayList("songs");       //we need to use same keys as passed in intent
        String songname = i.getStringExtra("songname");                   //getting the songname passed by prev activity
        position = bundle.getInt("position");                             //getting the song pos in file passed by prev activity
        txtname.setSelected(true);
        Uri uri = Uri.parse(mysongs.get(position).toString());
        sname = mysongs.get(position).getName();
        txtname.setText(sname);                            //setting value of textview of song name


        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);       //start the media player by using the above mention info
        mediaPlayer.start();


//updaes seekbar every 5millisecond
        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalduration = mediaPlayer.getDuration();
                int currpos = 0;


                while (currpos < totalduration) {
                    try {
                        {
                            sleep(500);
                            currpos = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(currpos);
                        }
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);


//increasing seekbaar manually
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });


        String endtime = timeDur(mediaPlayer.getDuration());
        songend.setText(endtime);


        //this will update current time every second
        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currtime = timeDur(mediaPlayer.getCurrentPosition());
                songstart.setText(currtime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        play.setOnClickListener(new View.OnClickListener() {                         //when clicked on pay/pause button
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    play.setBackgroundResource(R.drawable.ic_play);                       //if mediaplayer is palying change button to play
                    mediaPlayer.pause();
                } else {
                    play.setBackgroundResource(R.drawable.ic_pause);            //change to pause
                    mediaPlayer.start();
                    int audiosessionId = mediaPlayer.getAudioSessionId();
                    if (audiosessionId != 1) {
                        visualizer.setAudioSessionId(audiosessionId);
                    }
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mysongs.size());

                Uri u = Uri.parse(mysongs.get(position).toString());

                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);       //start the media player by using the above mention info

                seekBar.setProgress(0);
                sname = mysongs.get(position).getName();
                txtname.setText(sname);
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.ic_pause);
                startanime(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != 1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
                String endtime = timeDur(mediaPlayer.getDuration());
                songend.setText(endtime);
                seekBar.setMax(mediaPlayer.getDuration());


            }


        });


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {          //whenever a song end it atomatically moves to next one
            @Override
            public void onCompletion(MediaPlayer mp) {
                next.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != 1) {
            visualizer.setAudioSessionId(audiosessionId);
        }


        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) < 0) ? (mysongs.size() - 1) : position - 1;

                Uri u = Uri.parse(mysongs.get(position).toString());

                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);       //start the media player by using the above mention info
                seekBar.setProgress(0);
                sname = mysongs.get(position).getName();
                txtname.setText(sname);
                mediaPlayer.start();
                play.setBackgroundResource(R.drawable.ic_pause);
                startanime(imageView);
                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != 1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
                String endtime = timeDur(mediaPlayer.getDuration());
                songend.setText(endtime);
                seekBar.setMax(mediaPlayer.getDuration());
            }
        });



        fastfrwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });



        fastrewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });


    }


    public void startanime(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String timeDur(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";
        if (sec < 10) {
            time += "0";
        }
        time += sec;

        return time;
    }


}
