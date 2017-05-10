package xyz.moviseries.moviseries.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.moviseries.moviseries.R;
import xyz.moviseries.moviseries.adapters.EnlacesSerieMegaAdapter;
import xyz.moviseries.moviseries.api_clients.MoviseriesApiClient;
import xyz.moviseries.moviseries.api_services.MoviseriesApiService;
import xyz.moviseries.moviseries.custom_views.DMTextView;
import xyz.moviseries.moviseries.models.MEGAUrlSerie;
import xyz.moviseries.moviseries.models.Season;
import xyz.moviseries.moviseries.models.SeasonSerie;
import xyz.moviseries.moviseries.models.SerieScore;
import xyz.moviseries.moviseries.models.ViewSerie;

/**
 * Created by DARWIN on 10/5/2017.
 */

public class BottomSheetSerie extends BottomSheetDialogFragment {
    public static final String SERIE_ID = "BottomSheetSerie.seriee_id";
    public static final String NAME = "BottomSheetSerie.name";
    public static final String YEAR = "BottomSheetSerie.year";
    public static final String COVER = "BottomSheetSerie.cover";
    public static final String DESCRIPTION = "BottomSheetSerie.description";
    public static final String UPDATE_AT = "BottomSheetSerie.update_at";

    private String name, serie_id, year, cover, description, update_at;
    private Context context;
    private ImageView imageViewCover;
    private TextView textViewName, textViewUpdateAt, textViewVotos;
    private DMTextView textViewDescription;
    private SmileRating smileRating;

    private BottomSheetBehavior mBehavior;
    private SerieScore serie;


    private ArrayList<SeasonSerie> urls = new ArrayList<>();
    private ArrayList<MEGAUrlSerie> mega_urls = new ArrayList<>();
    private RecyclerView recyclerViewEnlaces, recyclerViewEnlacesMega;
    private EnlacesSerieMegaAdapter enlacesMegaAdapter;

    public static BottomSheetSerie newInstance(Bundle args) {
        BottomSheetSerie serieFragment = new BottomSheetSerie();
        serieFragment.setArguments(args);
        return serieFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        Bundle args = getArguments();
        name = args.getString(NAME);
        serie_id = args.getString(SERIE_ID);
        year = args.getString(YEAR);
        cover = args.getString(COVER);
        description = args.getString(DESCRIPTION);
        update_at = args.getString(UPDATE_AT);
    }


    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_serie, null);
        dialog.setContentView(contentView);

        imageViewCover = (ImageView) contentView.findViewById(R.id.cover);
        textViewName = (TextView) contentView.findViewById(R.id.name);
        textViewUpdateAt = (TextView) contentView.findViewById(R.id.timestamp);
        textViewDescription = (DMTextView) contentView.findViewById(R.id.short_description);
        textViewVotos = (TextView) contentView.findViewById(R.id.votos);
        smileRating = (SmileRating) contentView.findViewById(R.id.ratingView);
        recyclerViewEnlaces = (RecyclerView) contentView.findViewById(R.id.enlaces);
        recyclerViewEnlacesMega = (RecyclerView) contentView.findViewById(R.id.enlaces_mega);
        recyclerViewEnlacesMega.setLayoutManager(new LinearLayoutManager(context));
        enlacesMegaAdapter = new EnlacesSerieMegaAdapter(context, mega_urls);
        recyclerViewEnlacesMega.setAdapter(enlacesMegaAdapter);

        Picasso.with(context)
                .load(cover)
                .resize(351, 526)
                .centerCrop()
                .into(imageViewCover);
        textViewName.setText(name);
        textViewUpdateAt.setText(update_at);
        textViewDescription.setText(description);


        mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }


        new Load().execute();

    }


    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };


    private class Load extends AsyncTask<Void, Void, Void> implements Callback<ViewSerie> {
        private String url = "http://moviseries.xyz/android/serie/" + serie_id;

        @Override
        protected Void doInBackground(Void... voids) {
            MoviseriesApiService apiService = MoviseriesApiClient.getClient().create(MoviseriesApiService.class);
            Call<ViewSerie> call = apiService.getSerie(url);
            call.enqueue(this);
            return null;
        }

        @Override
        public void onResponse(Call<ViewSerie> call, Response<ViewSerie> response) {
            if (response != null) {
                if (response.body() != null) {


                    if (response.body().getSerie() != null) {
                        serie = response.body().getSerie();
                        float score = Float.parseFloat(serie.getScore());
                        if (score >= 0 && score < 2) {
                            smileRating.setSelectedSmile(BaseRating.TERRIBLE);
                        } else if (score >= 2 && score < 4) {
                            smileRating.setSelectedSmile(BaseRating.BAD);
                        } else if (score >= 4 && score < 6) {
                            smileRating.setSelectedSmile(BaseRating.OKAY);
                        } else if (score >= 6 && score < 8) {
                            smileRating.setSelectedSmile(BaseRating.GOOD);
                        } else {
                            smileRating.setSelectedSmile(BaseRating.GREAT);
                        }

                        textViewVotos.setText("# votos: " + serie.getVotos());

                    } else {
                        Log.i("apimovi", "null serie");
                    }


                    if (response.body().getMega_urls() != null) {

                        mega_urls.addAll(response.body().getMega_urls());
                        Log.i("apimovi", "x serie " + mega_urls.size());
                        if (mega_urls.size() > 0) {
                            enlacesMegaAdapter.notifyItemRangeInserted(0, mega_urls.size());
                            enlacesMegaAdapter.notifyDataSetChanged();
                        }

                    }

                    if (response.body().getSeasons() != null) {
                        urls.addAll(response.body().getSeasons());
                        if (urls.size() > 0) {
                            //enlacesAdapter.notifyItemRangeInserted(0, urls.size());
                            // enlacesAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<ViewSerie> call, Throwable t) {
            Log.i("apimovi", t.getMessage());
        }
    }

}