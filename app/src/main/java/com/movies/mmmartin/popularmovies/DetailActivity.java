package com.movies.mmmartin.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class DetailActivity extends AppCompatActivity implements DetailActivityFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putParcelable( DetailActivityFragment.DETAIL_URI, getIntent().getData() );
            arguments.putInt( DetailActivityFragment.DETAIL_MID, getIntent().getIntExtra(DetailActivityFragment.DETAIL_MID, 0) );

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }


    @Override
    public void onItemSelected(String key) {
//        Log.d("DetailActivity", "Sending http://www.youtube.com/watch?v="+key);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + key));
        startActivity(intent);
    }

}
