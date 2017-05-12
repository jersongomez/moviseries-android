package xyz.moviseries.moviseries;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.moviseries.moviseries.adapters.CapitulosAdapter;
import xyz.moviseries.moviseries.api_clients.MoviseriesApiClient;
import xyz.moviseries.moviseries.api_services.MoviseriesApiService;
import xyz.moviseries.moviseries.models.Capitulo;
import xyz.moviseries.moviseries.models.UrlOnline;
import xyz.moviseries.moviseries.streaming.OpenLoad;
import xyz.moviseries.moviseries.streaming.StreamMoe;

public class SeasonActivity extends BaseActivity implements CapitulosAdapter.OnClickCapituloListener, YouTubePlayer.OnInitializedListener {

    public static final String SERIE_NAME = "SeasonActivity.serie_name";
    public static final String SEASON_ID = "SeasonActivity.season_id";
    public static final String SEASON_NUMBER = "SeasonActivity.season_number";
    public static final String COVER = "SeasonActivity.cover";
    public static final String TRAILER = "SeasonActivity.trailer";


    private String serie_name, season_id, season_number, cover, trailer;

    private RecyclerView recyclerView;
    private CapitulosAdapter adapter;
    private ArrayList<Capitulo> capitulos = new ArrayList<>();

    private OpenLoad openLoad;
    private StreamMoe streamMoe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season);
        openLoad = new OpenLoad(context);
        streamMoe = new StreamMoe(context);
        Intent intent = getIntent();
        serie_name = intent.getStringExtra(SERIE_NAME);
        season_id = intent.getStringExtra(SEASON_ID);
        season_number = intent.getStringExtra(SEASON_NUMBER);
        cover = intent.getStringExtra(COVER);
        trailer = intent.getStringExtra(TRAILER);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        YouTubePlayerFragment youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);


        getSupportActionBar().setDisplayShowTitleEnabled(false);



        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new CapitulosAdapter(context, capitulos);
        adapter.setOnClickCapituloListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);


        initCollapse();

        new Load().execute();

    }








    private void initCollapse() {



        final RelativeLayout content_collapse = (RelativeLayout) findViewById(R.id.content_collapse);
        TextView textViewToolbar = (TextView) findViewById(R.id.textViewToolbar);
        textViewToolbar.setText(serie_name + ": Temporada " + season_number);


        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        collapsingToolbarLayout.setTitle("");


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            int scrollRange = -1;
            boolean animate_out = false, animate_in = false;//variables para controlar que la animacion solo se muestre una sola vez mientra se hace scroll

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                collapsingToolbarLayout.setTitle("");
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }


                if (scrollRange + verticalOffset < 70) {
                    if (!animate_out) {
                        animate_out = true;
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                        content_collapse.startAnimation(anim);
                        content_collapse.setVisibility(View.INVISIBLE);
                        animate_in = false;
                    }
                } else {
                    if (!animate_in) {
                        animate_in = true;
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                        content_collapse.startAnimation(anim);
                        content_collapse.setVisibility(View.VISIBLE);
                        animate_out = false;
                    }

                }


            }


        });

    }






    @Override
    public void onCapituloClickPlay(Capitulo capitulo) {
        UrlOnline url = new UrlOnline(capitulo.getUrl_id(), capitulo.getFile_id(), capitulo.getQuality(), capitulo.getServer(), capitulo.getLanguage_name());

        if (capitulo.getServer().equals("stream.moe")) {
            streamMoe.initStreaming(url);
        } else if (capitulo.getServer().equals("openload")) {
            openLoad.initStreaming(url);
        }
    }

    @Override
    public void onCapituloClickDownload(Capitulo capitulo) {
        if (capitulo.getServer().equals("stream.moe")) {

        } else if (capitulo.getServer().equals("openload")) {
            String url = "https://openload.co/f/" + capitulo.getFile_id();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer.cueVideo(trailer);
        }
    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
    }


    private class Load extends AsyncTask<Void, Void, Void> implements Callback<List<Capitulo>> {
        private String url = "http://moviseries.xyz/android/season/" + season_id;

        @Override
        protected Void doInBackground(Void... voids) {
            MoviseriesApiService apiService = MoviseriesApiClient.getClient().create(MoviseriesApiService.class);
            Call<List<Capitulo>> call = apiService.getSeason(url);
            call.enqueue(this);
            return null;
        }


        @Override
        public void onResponse(Call<List<Capitulo>> call, Response<List<Capitulo>> response) {
            if (response != null) {
                if (response.body() != null) {
                    capitulos.addAll(response.body());

                    int n = capitulos.size();
                    if (n > 0) {
                        adapter.notifyItemRangeInserted(0, n);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<List<Capitulo>> call, Throwable t) {

        }
    }


}
