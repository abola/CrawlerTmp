package co.gibar.crawler;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abola Lee on 2015/8/29.
 */
abstract public class AbstractCrawler implements Crawler {

    @Override
    public List<Map<String, Object>> crawlJson(String target){
        String result = this.crawl(target);

        if ( null == result || 0 == result.length() ) {
            return Lists.newArrayList() ;
        }
        else if ( result.substring(0,1).equals("[") ){
            // collection type
            Type jsonType =  new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType();
            return new Gson().fromJson(result, jsonType);
        }
        else {
            // object type
            Type jsonType =  new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> transformmedResult = new Gson().fromJson(result, jsonType);

            return Lists.newArrayList( transformmedResult );
        }

    }



}