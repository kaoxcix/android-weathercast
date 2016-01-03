package kaoxcix.weathercast.util;

import android.content.Context;

import kaoxcix.weathercast.R;

public class assetUtils {
    private Context context;
    
    public assetUtils(Context context) {
        this.context = context;
    }
    
    //Return Color ImageId BackgroundId FontColor
    public Integer getWeatherImageId(int id){
        if(id >= 200 && id <= 232){return R.drawable.simple_weather_icon_27;} //Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.drawable.simple_weather_icon_27;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.drawable.simple_weather_icon_30;} //Breeze
        else if(id >= 300 && id <= 321){return R.drawable.simple_weather_icon_21;} //Drizzle
        else if(id >= 500 && id <= 531){return R.drawable.simple_weather_icon_11;} //Rain
        else if(id >= 600 && id <= 622){return R.drawable.simple_weather_icon_24_black;} //Snow-
        else if(id >= 701 && id <= 781){return R.drawable.simple_weather_icon_10;} //Atmosphere
        else if(id == 800){return R.drawable.simple_weather_icon_01;} //Clear sky
        else if(id >= 801 && id <= 804){return R.drawable.simple_weather_icon_06_black;} //Clouds-
        else if(id == 903){return R.drawable.simple_weather_icon_53_black;} //Cold-
        else if(id == 904){return R.drawable.simple_weather_icon_55;} //Hot
        else if(id == 905){return R.drawable.simple_weather_icon_30;} //Windy
        else if(id >= 957 && id <= 962){return R.drawable.simple_weather_icon_30;} //Gale
        else if(id == 906){return R.drawable.simple_weather_icon_28_black;} //Hail-
        else{return R.drawable.simple_weather_icon_04_black;} //-
    }

    public Integer getBackgroundColorId(int id){
        if(id >= 200 && id <= 232){return R.color.weatherThunderstormBlue;}//Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.color.weatherThunderstormBlue;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.color.weatherLightWindBrown;} //Breeze
        else if(id >= 300 && id <= 321){return R.color.weatherLightRainyBlue;} //Drizzle
        else if(id >= 500 && id <= 531){return R.color.weatherDarkRainyBlue;} //Rain
        else if(id >= 600 && id <= 622){return R.color.weatherSnowWhite;} //Snow-
        else if(id >= 701 && id <= 781){return R.color.weatherLightWindBrown;} //Atmosphere
        else if(id == 800){return R.color.weatherSunnyRed;} //Clear sky
        else if(id >= 801 && id <= 804){return R.color.weatherCloudCream;} //Clouds-
        else if(id == 903){return R.color.weatherSnowWhite;} //Cold-
        else if(id == 904){return R.color.weatherSunnyRed;} //Hot
        else if(id == 905){return R.color.weatherLightWindBrown;} //Windy
        else if(id >= 957 && id <= 962){return R.color.weatherDarkWindBrown;} //Gale
        else if(id == 906){return R.color.weatherSnowWhite;} //Hail-
        else{return R.color.weatherOther;} //-
    }

    public Integer getBackgroundDarkColorId(int id){
        if(id >= 200 && id <= 232){return R.color.weatherThunderstormBlueDark;}//Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.color.weatherThunderstormBlueDark;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.color.weatherLightWindBrownDark;} //Breeze
        else if(id >= 300 && id <= 321){return R.color.weatherLightRainyBlueDark;} //Drizzle
        else if(id >= 500 && id <= 531){return R.color.weatherDarkRainyBlueDark;} //Rain
        else if(id >= 600 && id <= 622){return R.color.weatherSnowWhiteDark;} //Snow-
        else if(id >= 701 && id <= 781){return R.color.weatherLightWindBrownDark;} //Atmosphere
        else if(id == 800){return R.color.weatherSunnyRedDark;} //Clear sky
        else if(id >= 801 && id <= 804){return R.color.weatherCloudCreamDark;} //Clouds-
        else if(id == 903){return R.color.weatherSnowWhiteDark;} //Cold-
        else if(id == 904){return R.color.weatherSunnyRedDark;} //Hot
        else if(id == 905){return R.color.weatherLightWindBrownDark;} //Windy
        else if(id >= 957 && id <= 962){return R.color.weatherDarkWindBrownDark;} //Gale
        else if(id == 906){return R.color.weatherSnowWhiteDark;} //Hail-
        else{return R.color.weatherOtherDark;} //-
    }

    public Integer getFontColorId(int id){
        if(id >= 200 && id <= 232){return R.color.weatherFontOpacityBlack;}//Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.color.weatherFontWhite;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.color.weatherFontWhite;} //Breeze
        else if(id >= 300 && id <= 321){return R.color.weatherFontWhite;} //Drizzle
        else if(id >= 500 && id <= 531){return R.color.weatherFontWhite;} //Rain
        else if(id >= 600 && id <= 622){return R.color.weatherFontOpacityBlack;} //Snow-
        else if(id >= 701 && id <= 781){return R.color.weatherFontWhite;} //Atmosphere
        else if(id == 800){return R.color.weatherFontWhite;} //Clear sky
        else if(id >= 801 && id <= 804){return R.color.weatherFontOpacityBlack;} //Clouds-
        else if(id == 903){return R.color.weatherFontOpacityBlack;} //Cold-
        else if(id == 904){return R.color.weatherFontWhite;} //Hot
        else if(id == 905){return R.color.weatherFontWhite;} //Windy
        else if(id >= 957 && id <= 962){return R.color.weatherFontWhite;} //Gale
        else if(id == 906){return R.color.weatherFontOpacityBlack;} //Hail-
        else{return R.color.weatherFontOpacityBlack;} //-
    }

    public Integer getSunImageId(int id,String imageId){
        if(id >= 200 && id <= 232){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Breeze
        else if(id >= 300 && id <= 321){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Drizzle
        else if(id >= 500 && id <= 531){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Rain
        else if(id >= 600 && id <= 622){return context.getResources().getIdentifier(imageId+"_black", "drawable", context.getPackageName());} //Snow-
        else if(id >= 701 && id <= 781){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Atmosphere
        else if(id == 800){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Clear sky
        else if(id >= 801 && id <= 804){return context.getResources().getIdentifier(imageId+"_black", "drawable", context.getPackageName());} //Clouds-
        else if(id == 903){return context.getResources().getIdentifier(imageId+"_black", "drawable", context.getPackageName());} //Cold-
        else if(id == 904){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Hot
        else if(id == 905){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Windy
        else if(id >= 957 && id <= 962){return context.getResources().getIdentifier(imageId, "drawable", context.getPackageName());} //Gale
        else if(id == 906){return context.getResources().getIdentifier(imageId+"_black", "drawable", context.getPackageName());} //Hail-
        else{return context.getResources().getIdentifier(imageId+"_black", "drawable", context.getPackageName());} //-
    }

    
}
