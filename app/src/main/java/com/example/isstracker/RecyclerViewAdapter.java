package com.example.isstracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.isstracker.FragmentInfo.recyclerView;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> peopleName, peopleDescription, peopleImage;
    private Context context;
    private int mExpandedPosition = -1;

    RecyclerViewAdapter(Context context, ArrayList<String> name, ArrayList<String> description, ArrayList<String> imageURL){
        this.context = context;
        peopleName = name;
        peopleDescription = description;
        peopleImage = imageURL;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.info_recycler_view, viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final int position = i;

        viewHolder.name.setText(peopleName.get(i));
        viewHolder.description.setText(peopleDescription.get(i));
        //viewHolder.image.setImageResource(R.drawable.info_icon);


        final boolean isDescExpanded = (i==mExpandedPosition);

        viewHolder.description.setVisibility(isDescExpanded?View.VISIBLE:View.GONE);
        viewHolder.itemView.setActivated(isDescExpanded);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isDescExpanded ? -1:position;
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });

        final boolean isPicExpanded = (i==mExpandedPosition);

        viewHolder.image.setVisibility(isPicExpanded?View.VISIBLE:View.GONE);
        viewHolder.itemView.setActivated(isPicExpanded);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isPicExpanded ? -1:position;
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });

        Picasso.with(context)
                .load(peopleImage.get(i))
                .into(viewHolder.image);
    }

    /*public void clear() {
        int size = peopleName.size();
        if (size > 0) {
            peopleName.subList(0, size).clear();
            peopleDescription.subList(0, size).clear();
            peopleImage.subList(0,size).clear();
            notifyItemRangeRemoved(0, size);
        }
    }*/

    @Override
    public int getItemCount() {
        return peopleName.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,description;
        ImageView image;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.people_name);
            description = itemView.findViewById(R.id.people_description);
            image = itemView.findViewById(R.id.people_image);
        }
    }
}
