package com.kasperstore.wallpaperart.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kasperstore.wallpaperart.R;
import com.kasperstore.wallpaperart.adapters.WallpapersAdapter;
import com.kasperstore.wallpaperart.models.Wallpaper;

import java.util.ArrayList;
import java.util.List;

public class WallpapersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WallpapersAdapter adapter;
    private List<Wallpaper> wallpaperList;
    private List<Wallpaper> favList;

    private DatabaseReference dbWallpapers, dbFavs;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpapers);

        Intent intent = getIntent();
        final String category = intent.getStringExtra("category");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(category);
        setSupportActionBar(toolbar);

        wallpaperList = new ArrayList<>();
        favList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WallpapersAdapter(this,wallpaperList);

        recyclerView.setAdapter(adapter);
        progressBar = findViewById(R.id.progressbar);

        dbWallpapers = FirebaseDatabase.getInstance().getReference("images").child(category);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            dbFavs = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("favourites")
                    .child(category);
            fetchFavWallpapers(category);
        }else {
            fetchWallpapers(category);
        }
    }

    private void fetchFavWallpapers(final String category){
        progressBar.setVisibility(View.VISIBLE);
        dbFavs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    progressBar.setVisibility(View.GONE);
                    for(DataSnapshot wallpaperSnapshot:dataSnapshot.getChildren()){

                        String id = wallpaperSnapshot.getKey();
                        String title = wallpaperSnapshot.child("title").getValue(String.class);
                        String desc = wallpaperSnapshot.child("desc").getValue(String.class);
                        String url = wallpaperSnapshot.child("url").getValue(String.class);

                        Wallpaper w = new Wallpaper(id,title,desc,url,category);
                        favList.add(w);
                    }
                }
                fetchWallpapers(category);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchWallpapers(final String category){
        progressBar.setVisibility(View.VISIBLE);
        dbWallpapers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    progressBar.setVisibility(View.GONE);
                    for(DataSnapshot wallpaperSnapshot:dataSnapshot.getChildren()){

                        String id = wallpaperSnapshot.getKey();
                        String title = wallpaperSnapshot.child("title").getValue(String.class);
                        String desc = wallpaperSnapshot.child("desc").getValue(String.class);
                        String url = wallpaperSnapshot.child("url").getValue(String.class);

                        Wallpaper w = new Wallpaper(id,title,desc,url,category);
                        if(isFavourite(w)){
                            w.isFavourite = true;
                        }
                        wallpaperList.add(w);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean isFavourite(Wallpaper w){
        for(Wallpaper f: favList){
            if(f.id.equals(w.id)){
                return true;
            }
        }
        return false;
    }
}


