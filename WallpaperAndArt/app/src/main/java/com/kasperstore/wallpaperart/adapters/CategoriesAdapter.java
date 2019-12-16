package com.kasperstore.wallpaperart.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kasperstore.wallpaperart.R;
import com.kasperstore.wallpaperart.activities.WallpapersActivity;
import com.kasperstore.wallpaperart.models.Category;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private Context mCtx;
    private List<Category> categoryList;

    private InterstitialAd mInterstitialAd;


    public CategoriesAdapter(Context mCtx, List<Category> categoryList) {
        this.mCtx = mCtx;
        this.categoryList = categoryList;

        mInterstitialAd = new InterstitialAd(mCtx);
        mInterstitialAd.setAdUnitId("ca-app-pub-1268658999517461/8378471716");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_categories,parent,false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category c = categoryList.get(position);
        holder.textView.setText(c.name);
        Glide.with(mCtx).load(c.thumb).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        if(categoryList != null){
            return categoryList.size();
        }
        else {
            return 0;
        }

    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView;
        ImageView imageView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view_cat_name);
            imageView = itemView.findViewById(R.id.image_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            int p = getAdapterPosition();
            Category c = categoryList.get(p);

            Intent i = new Intent(mCtx, WallpapersActivity.class);
            i.putExtra("category",c.name);
            mCtx.startActivity(i);
        }
    }
}
