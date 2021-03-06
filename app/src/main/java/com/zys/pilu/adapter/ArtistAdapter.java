package com.zys.pilu.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zys.pilu.R;
import com.zys.pilu.activities.SongList;
import com.zys.pilu.common.AppContext;
import com.zys.pilu.db.DBManager;
import com.zys.pilu.models.Artist;
import com.zys.pilu.utils.MyEvalucatorUtil;
import com.zys.pilu.utils.SongProvider;
import com.indris.material.RippleView;

import java.util.List;

/**
 * Created by zys on 2016/7/10.
 */
public class ArtistAdapter extends BaseAdapter {
    private final String TAG = "ArtistAdapter";
    private List<Artist> artistList;
    private Context context;
    private DBManager dbMgr;
    private Drawable greyDrawable;
    private Drawable qingseDrawable;

    public ArtistAdapter(Context context) {
        this.context = context;
        dbMgr = new DBManager();
        greyDrawable = context.getResources().getDrawable(R.drawable.artist_grey);
        qingseDrawable = context.getResources().getDrawable(R.drawable.artist_good_qingse);
        artistList = SongProvider.getArtistList();
    }

    @Override
    public int getCount() {
        return artistList.size();
    }

    @Override
    public Object getItem(int position) {
        return artistList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(AppContext.getInstance()).inflate(R.layout.artist_item, null);
            holder = new Holder();
            holder.artistName = (TextView)convertView.findViewById(R.id.artistName);
            holder.root = (RelativeLayout)convertView.findViewById(R.id.rootLayout);
            holder.ripple = (RippleView)convertView.findViewById(R.id.rippleView);
            holder.enterIcon = (ImageView)convertView.findViewById(R.id.enterIcon);
            holder.artistCata = (ImageView)convertView.findViewById(R.id.artistCata);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        // Click Ripple and Enter List Activity
        holder.ripple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final float x = holder.enterIcon.getX();
                ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        holder.enterIcon.setRotation(360*(float)animation.getAnimatedValue());
                    }
                });
                anim.setInterpolator(new DecelerateInterpolator(1.5f));
                anim.setDuration(500);
                anim.start();

                String artistName = holder.artistName.getText().toString();
                startList(artistList.get(position).getName());

            }
        });
        final String artistName = artistList.get(position).getName();

        clearAnim(holder);
        // Set the Cata Drawable
        if (dbMgr.isFavoriteArtist(artistName)) {
            holder.artistCata.setImageDrawable(qingseDrawable);
        } else {
            holder.artistCata.setImageDrawable(greyDrawable);
        }

        // Set Cata Drawable OnCLickListener
        holder.artistCata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbMgr.isFavoriteArtist(artistName)) {
                    dbMgr.deleteFromFavoriteArtist(artistName);
                    clearAnim(holder);
                    startAnim(holder, greyDrawable);
                    //holder.artistCata.setImageDrawable(greyDrawable);
                } else {
                    dbMgr.addToFavoriteArtist(artistName);
                    //holder.artistCata.setImageDrawable(qingseDrawable);
                    clearAnim(holder);
                    startAnim(holder, qingseDrawable);
                }
            }
        });
        // Set Text
        String songCount = "(" +artistList.get(position).getSongListOfArtist().size()  + "首)";
        holder.artistName.setText(artistName + songCount);

        return convertView;
    }

    private class Holder {
        public RippleView ripple;
        public TextView artistName;
        public RelativeLayout root;
        public ImageView enterIcon;
        public ImageView artistCata;
        public ValueAnimator zoomOutAnim;
        public ValueAnimator zoomInAnim;
    }
    // Start List Activiy
    private void startList(final String artistName) {
        Handler handlerTimer = new Handler();
        handlerTimer.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(context, SongList.class);
                intent.putExtra("cata", artistName);
                context.startActivity(intent);
            }
        }, 500);
    }

    private void clearAnim(Holder holder) {
        if (holder.zoomOutAnim != null && holder.zoomOutAnim.isRunning())
            holder.zoomOutAnim.end();
        if (holder.zoomInAnim != null && holder.zoomInAnim.isRunning())
            holder.zoomInAnim.end();
    }

    private void startAnim(final Holder holder, final Drawable drawable) {
        holder.zoomOutAnim = ValueAnimator.ofFloat(1, 0).setDuration(100);
        holder.zoomOutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float)animation.getAnimatedValue();
                holder.artistCata.setScaleX(f);
                holder.artistCata.setScaleY(f);
            }
        });
        holder.zoomInAnim = ValueAnimator.ofFloat(0, 1).setDuration(2000);
        holder.zoomInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                holder.artistCata.setScaleX(f);
                holder.artistCata.setScaleY(f);
            }
        });
        MyEvalucatorUtil.JellyFloatAnim jelly = new MyEvalucatorUtil.JellyFloatAnim();
        jelly.setDuration(2000);
        jelly.setFirstTime(100);
        jelly.setAmp(0.03);
        holder.zoomInAnim.setEvaluator(jelly);
        holder.zoomOutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                holder.artistCata.setImageDrawable(drawable);
                holder.zoomInAnim.start();
            }
        });
        holder.zoomOutAnim.start();
    }

    public char getLetterByPosition(int position) {
        char[] pinyin = artistList.get(position).getPinyin().toCharArray();
        if (pinyin[0] <= 'Z' &&  pinyin[0] >= 'A')
            return pinyin[0];
        else
            return '#';
    }
    public int getPositionByLetter(char letter) {

        int index = findByBinarySearch(0, artistList.size() - 1, letter);
        // When Find Nothing
        if (index == -1 && letter != 'A') {
            return getPositionByLetter((char)(letter-1));
        }
        else  if (index == -1 && letter == 'A') {
            return getPositionByLetter('#');
        }
        // When found
        for (int i = index ; i > 0 ; i --) {
            char[] pinyin = artistList.get(i-1).getPinyin().toCharArray();
            if (pinyin[0] != letter) {
                return i;
            }
            if (i == 1) {
                return 0;
            }
        }
        return 0;
    }
    private int findByBinarySearch(int start, int end, char letter) {
        if (letter == '#')
            return 0;
        if (end < start)
            return -1;
        int middle = (end + start)/2;
        char[] pinyin = artistList.get(middle).getPinyin().toCharArray();
        if (pinyin[0] > letter) {
            return findByBinarySearch(start, middle - 1, letter);
        } else if (pinyin[0] < letter) {
            return findByBinarySearch(middle + 1, end, letter);
        } else {
            return middle;
        }
    }
}
