package io.temco.guhada.data.model;

import com.google.gson.annotations.SerializedName;

public class BrandData {

    @SerializedName("id")
    public int id;

    @SerializedName("nameDefault")
    public String nameDefault;

    @SerializedName("nameEn")
    public String nameEn;

    @SerializedName("nameKo")
    public String nameKo;

    @SerializedName("isFavorite")
    public boolean isFavorite;
}