package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.utils.YTutils;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditTagActivity extends AppCompatActivity {

    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.edit_ImageView)
    ImageView mainImage;
    //   @BindView(R.id.chooseImage) FloatingActionButton chooseImageButton;
    @BindView(R.id.TextView_title)
    EditText eTitle;
    @BindView(R.id.TextView_album)
    EditText eAlbum;
    @BindView(R.id.TextView_artist)
    EditText eArtist;
    @BindView(R.id.TextView_year)
    EditText eYear;
    @BindView(R.id.TextView_number)
    EditText eTrack;
    @BindView(R.id.TextView_album_artist)
    EditText eComposer;
    @BindView(R.id.TextView_location)
    EditText eComment;
    @BindView(R.id.TextView_genre)
    AutoCompleteTextView eGenre;
    @BindView(R.id.relativeLayout)
    RelativeLayout relativeLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.saveFab)
    FloatingActionButton saveFab;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        OFModel model = (OFModel) intent.getSerializableExtra("model");

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mainImage.setVisibility(View.GONE);
                    toolbar.setTitle("Edit Tags");
                    collapsingToolbarLayout.setTitle("Edit Tags");
                    //  chooseImageButton.hide();
                    toolbar.setSubtitle(model.getPath());
                    isShow = true;
                } else if (isShow) {
                    mainImage.setVisibility(View.VISIBLE);
                    //  chooseImageButton.show();
                    toolbar.setTitle(" "); //careful there should a space between double quote otherwise it wont work
                    collapsingToolbarLayout.setTitle(" "); //careful there should a space between double quote otherwise it wont work
                    toolbar.setSubtitle(" ");
                    isShow = false;
                }
            }
        });


        if (model != null) {
            File f = new File(model.getPath());
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, Uri.fromFile(f));

            byte[] data = mmr.getEmbeddedPicture();

            if (data != null)
                mainImage.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));

            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (title != null) eTitle.setText(title);
            else eTitle.setText(model.getTitle());

            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (album != null)
                eAlbum.setText(album);

            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist != null)
                eArtist.setText(artist);

            String year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            if (year != null)
                eYear.setText(year);

            String track = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            if (track != null)
                eTrack.setText(track);

            String genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            if (genre != null)
                eGenre.setText(genre);

            String album_artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
            if (album_artist != null)
                eComposer.setText(album_artist);

            MusicMetadataSet metadataSet = null;
            try {
                metadataSet = new MyID3().read(f);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (metadataSet != null) {
                IMusicMetadata metadata = metadataSet.getSimplified();
                String comment = metadata.getComment();
                if (comment != null)
                    eComment.setText(comment);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                    getResources().getStringArray(R.array.autocomplete_genre));
            eGenre.setAdapter(adapter);

            eGenre.setOnTouchListener((view, motionEvent) -> {
                eGenre.requestFocus();
                UIUtil.showKeyboard(EditTagActivity.this, eGenre);
                eGenre.showDropDown();
                return true;
            });

            saveFab.setOnClickListener(view -> {

                MusicMetadataSet src_set = null;
                try {
                    src_set = new MyID3().read(f);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                ImageData imageData = null;
                try {
                    Bitmap bitmap = YTutils.drawableToBitmap(mainImage.getDrawable());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] bitmapdata = stream.toByteArray();
                    imageData = new ImageData(bitmapdata, "image/jpeg", "background", 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File dst = new File(f.getPath() + "_new.mp3");

                MusicMetadata meta = new MusicMetadata(model.getTitle());
                if (imageData != null) {
                    meta.addPicture(imageData);
                }
                meta.setAlbum(eAlbum.getText().toString());
                meta.setArtist(eArtist.getText().toString());
                meta.setSongTitle(model.getTitle());
                meta.setYear(eYear.getText().toString());
                if (!eTrack.getText().toString().isEmpty())
                    meta.setTrackNumber(Integer.parseInt(eTrack.getText().toString()));
                meta.setGenre(eGenre.getText().toString());
                meta.setComposer(eComposer.getText().toString());
                meta.setComment(eComment.getText().toString());

                try {
                    new MyID3().write(f, dst, src_set, meta);

                    /** Let's try to edit the tag name in file*/
                    String localFile = f.getParent().replace("/", "_") + ".csv";
                    String filePath = new File(getFilesDir(), "locals/" + localFile).getPath();
                    String text = YTutils.readContent(this, filePath);
                    if (text != null && !text.isEmpty()) {
                        StringBuilder builder = new StringBuilder();
                        String[] items = text.split("\n|\r");
                        for (String item : items) {
                            if (item.isEmpty()) continue;
                            if (item.startsWith(f.getPath())) {

                                String timeString = "0";
                                try {
                                    timeString = item.split("\\|")[3];
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                builder.append(f.getPath()).append("|").append(eArtist.getText().toString())
                                        .append("|").append(eAlbum.getText().toString()).append("|")
                                        .append(timeString).append("|").append(f.lastModified()).append("\n");
                            } else builder.append(item).append("\n");
                        }
                        YTutils.writeContent(this, filePath, builder.toString());
                    }

                    f.delete();
                    dst.renameTo(f);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ID3WriteException e) {
                    e.printStackTrace();
                }

                finish();

            });

        } else Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tag_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_image) {
            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent1.setType("image/*");
            intent1.addCategory(Intent.CATEGORY_OPENABLE);

            try {
                startActivityForResult(
                        Intent.createChooser(intent1, "Select an image file"),
                        1);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(this, "No file manager installed", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();

                    String path = YTutils.getPath(this, uri);

                    File f = new File(path);
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions);
                    bitmap = Bitmap.createScaledBitmap(bitmap, mainImage.getWidth(), mainImage.getHeight(), true);
                    mainImage.setImageBitmap(bitmap);

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
