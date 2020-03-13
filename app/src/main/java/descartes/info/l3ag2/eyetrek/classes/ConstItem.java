package descartes.info.l3ag2.eyetrek.classes;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.widget.ImageView;

import com.google.gson.annotations.SerializedName;

import descartes.info.l3ag2.eyetrek.R;

public class ConstItem {

    @SerializedName("imageUrl")
    private int mImageUrl;
    @SerializedName("info")
    private String mInfo;
    @SerializedName("subTitle")
    private String mSubTitle;
    @SerializedName("title")
    private String mTitle;

    private int idConstellation;

    public ConstItem(int mImageUrl, String mInfo, String mSubTitle, String mTitle,int idConstellation) {
        this.mImageUrl = mImageUrl;
        this.mInfo = mInfo;
        this.mSubTitle = mSubTitle;
        this.mTitle = mTitle;
        this.idConstellation = idConstellation;
    }

    public int getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(int imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }


    public int getIdConstellation() {
        return idConstellation;
    }

    public void setIdConstellation(int idConstellation) {
        this.idConstellation = idConstellation;
    }
}