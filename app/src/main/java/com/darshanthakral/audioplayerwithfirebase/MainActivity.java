package com.darshanthakral.audioplayerwithfirebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AudioAdapter.OnItemClickListener {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AudioAdapter audioAdapter;
    private List<audioModel> musicUploads;
    private DatabaseReference mDBRef;
    private ValueEventListener mListener;

    private MediaPlayer mediaPlayer;

    private TextView totalTime, currTime, trackName;
    private SeekBar play_timeline;
    private FloatingActionButton playPauseButton;
    private CardView play_view;

    private long maxDuration;
    private audioModel audioUploaded;

    private int currentSongListPosition = 0;
    private boolean wantsMusic = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Normal Start
        progressBar = findViewById(R.id.audio_gallery_progress_bar);


        musicUploads = new ArrayList<>();
        audioAdapter = new AudioAdapter(getApplicationContext(), musicUploads);

        recyclerView = findViewById(R.id.audio_gallery_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(audioAdapter);

        audioAdapter.setOnItemClickListener(MainActivity.this);

        play_view = findViewById(R.id.play_view);
        play_view.setCardBackgroundColor(Color.TRANSPARENT);
        play_view.setCardElevation(0);


        play_timeline = findViewById(R.id.song_timeline);
        playPauseButton = findViewById(R.id.play_pause_button);
        currTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        trackName = findViewById(R.id.trackName);

        mDBRef = FirebaseDatabase.getInstance().getReference("AudioUpload/GalleryAudio");
        mListener = mDBRef.addValueEventListener(new ValueEventListener() {

            //Connecting to respected database and fetching/updating the audio list which is available on database.
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musicUploads.clear();

                for (DataSnapshot dss : dataSnapshot.getChildren()) {

                    audioUploaded = dss.getValue(audioModel.class);
                    assert audioUploaded != null;
                    audioUploaded.setmKey(dss.getKey());
                    musicUploads.add(audioUploaded);

                }

                audioAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onItemClick(List<audioModel> arrayListSongs, int position) {
        audioUploaded = arrayListSongs.get(position);

        audioUploaded.setPosition(position);

        currentSongListPosition = audioUploaded.getPosition();

        audioAdapter.getLatestPosition(currentSongListPosition);

        playAudio();
    }

    private void playAudio() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUploaded.getAudioLink());

            mediaPlayer.prepareAsync();

            String loading = "Loading...";
            trackName.setText(loading);
            String timeLoading = "__:__";
            totalTime.setText(timeLoading);

            //Making FAB disables audio is prepared
            playPauseButton.setEnabled(false);


            mediaPlayer.setOnPreparedListener(mp -> {

                mp.start();

                //getting max duration of playing audio and displaying on textView at playing section
                maxDuration = mediaPlayer.getDuration();
                totalTime.setText(timerConversion(maxDuration));

                //Setting audio title to texView at playing section
                trackName.setText(audioUploaded.getAudioTitle());
                trackName.setSelected(true);

                //Enabling FAB after audio player is prepared
                playPauseButton.setEnabled(true);

                /*Setting seekbar max limit and updating seekbar till max
                 audio duration by getting audioPlayer reference*/
                play_timeline.setMax((int) maxDuration);
                play_timeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                //Handles Buffering on seekbar
                mediaPlayer.setOnBufferingUpdateListener((mp1, percent) -> {
                    double ratio = percent / 100.0;
                    int bufferingLevel = (int) (mp.getDuration() * ratio);
                    play_timeline.setSecondaryProgress(bufferingLevel);

                });

            });
//TODO:Figure Out
            playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
            requestAudioFocusForMyApp(getApplicationContext());

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Making playing section vision when user click's on any of the audio from list
        play_view.setVisibility(View.VISIBLE);


        //Updating seekbar of according to audio duration
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        long currDuration = mediaPlayer.getCurrentPosition();
                        play_timeline.setProgress((int) currDuration);
                        currTime.setText(timerConversion(currDuration));
                    }
                });

            }
        }, 0, 1000);

        mediaPlayer.setOnCompletionListener(mp -> nextAudio());

    }

    //Below code of onAudioFocusChangeListener handles all kind of hurdles which come when audio is playing is the player.

    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // resume playback
                    if (mediaPlayer == null)
                        mediaPlayer = new MediaPlayer();
                    else if (!mediaPlayer.isPlaying() && wantsMusic)
                        mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost focus for an unbounded amount of time: stop playback and release media player
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because playback
                    // is likely to resume
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        }
    };

    private void requestAudioFocusForMyApp(final Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        assert am != null;
        int result = am.requestAudioFocus(onAudioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("Audio Focus", "Audio focus received");
        } else {
            Log.d("Audio Focus", "Audio focus NOT received");
        }

    }

    private void releaseAudioFocusForMyApp(final Context context) {
        //Release audio focus from playback.
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        am.abandonAudioFocus(onAudioFocusChangeListener);
    }

    @Override
    public void onBackPressed() {

        //If User CLicks back button, stop and release player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            releaseAudioFocusForMyApp(getApplicationContext());
            Toast.makeText(getApplicationContext(), "To keep the track playing, minimize app by staying on the song page!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Leave Database Reference
        mDBRef.removeEventListener(mListener);
    }

    @SuppressLint("DefaultLocale")
    public String timerConversion(long value) {

        //Calculating audio track time for displaying on textView

        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }


    public void songPlayPauseButton(View v) {

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            wantsMusic = false;
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_baseline_pause_24);
            wantsMusic = true;
        }
    }

    public void nextSongButton(View v) {
        nextAudio();
    }

    private void nextAudio() {
        int nextSongListPosition = currentSongListPosition + 1;
        if (nextSongListPosition >= musicUploads.size()) {

            nextSongListPosition = 0;
        }

        musicUploads.get(nextSongListPosition);

        audioAdapter.updateList(musicUploads);

        recyclerView.scrollToPosition(nextSongListPosition);

        onItemClick(musicUploads, nextSongListPosition);

        audioUploaded.setPosition(nextSongListPosition);
    }

    public void prevSongButton(View v) { prevAudio(); }

    private void prevAudio() {
        int prevSongListPosition = currentSongListPosition - 1;
        if (prevSongListPosition < 0) {

            prevSongListPosition = musicUploads.size() - 1; //Play Last audio of list
        }

        musicUploads.get(prevSongListPosition);

        audioAdapter.updateList(musicUploads);

        recyclerView.scrollToPosition(prevSongListPosition);

        onItemClick(musicUploads, prevSongListPosition);

        audioUploaded.setPosition(prevSongListPosition);
    }

}