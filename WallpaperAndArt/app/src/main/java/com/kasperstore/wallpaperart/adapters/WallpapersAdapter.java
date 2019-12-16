package com.kasperstore.wallpaperart.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kasperstore.wallpaperart.R;
import com.kasperstore.wallpaperart.models.Wallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperViewHolder> {

    private Context mCtx;
    private List<Wallpaper> wallpaperList;


    public WallpapersAdapter(Context mCtx, List<Wallpaper> wallpaperList) {
        this.mCtx = mCtx;
        this.wallpaperList = wallpaperList;

    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_wallpapers,parent,false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        Wallpaper w = wallpaperList.get(position);
        holder.textView.setText(w.title);
        Glide.with(mCtx).load(w.url).into(holder.imageView);

        if(w.isFavourite){
            holder.checkBox.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        if(wallpaperList != null){
            return wallpaperList.size();
        }
        else {
            return 0;
        }

    }

    class WallpaperViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        TextView textView;
        ImageView imageView;

        CheckBox checkBox;
        ImageButton buttonShare, buttonDownload;

        public WallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view_title);
            imageView = itemView.findViewById(R.id.image_view);

            checkBox = itemView.findViewById(R.id.checkbox_favourite);
            buttonShare = itemView.findViewById(R.id.button_share);
            buttonDownload = itemView.findViewById(R.id.button_download);

            checkBox.setOnCheckedChangeListener(this);
            buttonDownload.setOnClickListener(this);
            buttonShare.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_share:
                    shareWallpaper(wallpaperList.get(getAdapterPosition()));
                    break;

                case R.id.button_download:
                    downloadWallpaper(wallpaperList.get(getAdapterPosition()));
                    break;
            }

        }

        private void shareWallpaper(Wallpaper w) {
            ((Activity)mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
            Glide.with(mCtx)
                    .asBitmap()
                    .load(w.url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            ((Activity)mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri myuri = getLocalBitmapUri(resource);
                            intent.setDataAndType(myuri, "image/*");
                            intent.putExtra(Intent.EXTRA_STREAM,myuri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            mCtx.startActivity(intent);
                        }
                    });

        }

        private Uri getLocalBitmapUri(Bitmap bmp) {
            Uri bmpUri = null;
            File file = new File(mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "wallpaper_hub_"+System.currentTimeMillis()+".png");
            try {
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG,90,out);
                out.close();
                bmpUri = FileProvider.getUriForFile(mCtx,mCtx.getApplicationContext().getPackageName()+".provider",file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmpUri;
        }

        private void downloadWallpaper(final Wallpaper w){
            ((Activity)mCtx).findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
            Glide.with(mCtx)
                    .asBitmap()
                    .load(w.url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            ((Activity)mCtx).findViewById(R.id.progressbar).setVisibility(View.GONE);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                            Uri myuri = saveWallpaperAndGetUri(resource,w.id);
                            if(myuri!=null){
                                intent.setDataAndType(myuri,"image/*");
                                intent.putExtra(Intent.EXTRA_STREAM,myuri);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                mCtx.startActivity(intent);
                            }
                        }
                    });
        }

        private Uri saveWallpaperAndGetUri(Bitmap bitmap, String id) {
            if(ContextCompat.checkSelfPermission(mCtx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale((Activity)mCtx,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                    Uri uri = Uri.fromParts("package",mCtx.getPackageName(),null);
                    intent.setData(uri);
                    mCtx.startActivity(intent);
                }else {
                    ActivityCompat.requestPermissions((Activity) mCtx,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
                }
                return null;
            }

            File folder = new File(Environment.getExternalStorageDirectory().toString()+"/wallpapers&art");
            folder.mkdirs();

            File file = new File(folder,id+".jpg");
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                out.flush();
                out.close();
                return FileProvider.getUriForFile(mCtx,mCtx.getApplicationContext().getPackageName()+".provider",file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                Toast.makeText(mCtx,"Please Login First....",Toast.LENGTH_LONG).show();
                buttonView.setChecked(false);
                return;
            }

            int position = getAdapterPosition();
            Wallpaper w = wallpaperList.get(position);

            DatabaseReference dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(w.category);

            if(isChecked){
                dbFavs.child(w.id).setValue(w);
            }else {
                dbFavs.child(w.id).setValue(null);
            }

        }
    }
}
