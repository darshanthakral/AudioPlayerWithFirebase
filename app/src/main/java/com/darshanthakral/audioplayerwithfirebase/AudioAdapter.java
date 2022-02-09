package com.darshanthakral.audioplayerwithfirebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

    Context context;
    List<audioModel> arrayList;
    public OnItemClickListener mListener;
    private int getLatestPosition = -1;

    public AudioAdapter(Context context, List<audioModel> arrayList) {

        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_items, parent, false);
        return new ViewHolder(view);
    }

    public void getLatestPosition(int position) {
        this.getLatestPosition = position;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        audioModel audioModel = arrayList.get(position);
        holder.title.setText(audioModel.getAudioTitle());
        holder.duration.setText(audioModel.getAudioDuration());

        if (getLatestPosition == position) {

            holder.root_layout.setBackgroundResource(R.drawable.custom_bg_color_playing);
            holder.audioPlayingAnimation.setVisibility(View.VISIBLE);
            Glide.with(context).load(R.drawable.audio_playing_wave).into(holder.audioPlayingAnimation);

        } else {
            holder.root_layout.setBackgroundResource(R.drawable.custom_bg_color_default);

            //Hiding audioPlayingAnimation
            holder.audioPlayingAnimation.setVisibility(View.GONE);

            //Clearing Glide
            Glide.with(context).clear(holder.audioPlayingAnimation);
        }

    }

    //To take input of new audio position and to update audioModel list accordingly
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<audioModel> list) {

        this.arrayList = list;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CardView root_layout;
        TextView title, duration;
        ImageView audioPlayingAnimation;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            audioPlayingAnimation = itemView.findViewById(R.id.audio_playing_animation);
            root_layout = itemView.findViewById(R.id.root_Layout);
            title = itemView.findViewById(R.id.audio_title);
            duration = itemView.findViewById(R.id.audio_duration);

            itemView.setOnClickListener(this);
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onClick(View view) {

            if (mListener != null) {
                int pos = getAdapterPosition();

                if (pos != RecyclerView.NO_POSITION) {
                    try {
                        mListener.onItemClick(arrayList, pos);
                        notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public interface OnItemClickListener {

        void onItemClick(List<audioModel> arrayListSongs, int position) throws IOException;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        mListener = listener;
    }
}
