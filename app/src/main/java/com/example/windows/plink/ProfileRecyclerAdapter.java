package com.example.windows.plink;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {
    List<Contact> contactList;
    Context context;
    private final static int FADE_DURATION = 1000;

    //constructor
    public ProfileRecyclerAdapter(List<Contact> contactList1, Context context) {
        super();
        this.contactList = contactList1;
        this.context = context;
    }

    //creates view holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_items, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //attach the values to the view using getter n setter method which is contact
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //get view position and set the background of the each item
        //dividing rows into even and odd position rows
        try {
            if (position % 2 == 0)
                holder.linearLayout.setBackgroundColor(Color.parseColor("#CFD8DC"));
            else
                holder.linearLayout.setBackgroundColor(Color.parseColor("#ECEFF1"));
        }catch (IndexOutOfBoundsException i){
            i.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        Contact contact = contactList.get(position);
        setScaleAnimation(holder.itemView);
        holder.name.setText(contact.getName());
        holder.profile.setText(contact.getProfile());

    }

    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, profile;
        LinearLayout linearLayout;

        public ViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.pname);
            profile = (TextView) view.findViewById(R.id.profile);
            linearLayout = view.findViewById(R.id.itemLayout);
        }

    }
}
