package com.darshanthakral.audioplayerwithfirebase;

public class audioModel {
    public String audioTitle, audioDuration, audioLink, mKey;
    public int position;

    public audioModel() {
        //Empty Constructor Needed
    }

    public audioModel(String audioTitle, String audioDuration, String audioLink, int position) {
        if (audioTitle.trim().equals("")) {
            audioTitle = "No Name";
        }

        this.audioTitle = audioTitle;
        this.audioDuration = audioDuration;
        this.audioLink = audioLink;
        this.position = position;
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
