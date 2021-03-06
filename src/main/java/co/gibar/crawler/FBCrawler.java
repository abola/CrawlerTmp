package co.gibar.crawler;

import co.gibar.crawler.exceptions.FBAccessTokenExpireException;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Abola Lee on 2015/8/29.
 */
public class FBCrawler extends WebCrawler{

    private String accessToken;

    private String clientId;
    private String clientSecret;

    private String apiVersion = "v2.4";

    private String graphApiErrorCode = "0";

    private int retryMaxCounter = 5;

    public FBCrawler(String longTermAccessToken){
        this.accessToken = longTermAccessToken;
    }

    public FBCrawler(String clientId, String clientSecret){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String crawl(String target){

        if (null == accessToken)
            this.accessToken = getAccessToken();
        try{
            return getGraphApi(target);
        }catch( FBAccessTokenExpireException ex ){
            this.accessToken = null;
            return crawl(target);
        }
    }

    /**
     * 取得 Facebook API access token
     * @return
     */
    protected String getAccessToken(){

        if  ( null != this.accessToken ) return this.accessToken;

        String requestAccessTokenUrl = "https://graph.facebook.com" +
                "/oauth/access_token" +
                "?client_id=" + this.clientId +
                "&client_secret=" + this.clientSecret +
                "&grant_type=client_credentials";
        String response = getUrl(requestAccessTokenUrl);


//        System.out.println(response);
        return response.split("=")[1];
    }

    protected String getGraphApi(String api) throws FBAccessTokenExpireException {

        this.graphApiErrorCode = "0";

        String request = "https://graph.facebook.com/" +
                this.apiVersion + "/" +
                api +
                "&locale=zh_TW" +
                "&access_token=" + this.accessToken;

        if ( "https".equals( api.substring(0,5) )  ){
            request = api;
        }

//        System.err.println( request );
        String response = getUrl(request);

//        System.err.println("response size:" +response.length());
        try{
            Type jsonType =  new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> transformmedResult = new Gson().fromJson(response, jsonType);

            this.graphApiErrorCode = JsonTools.getJsonPathValue( transformmedResult, "error.code", "0" );

        }catch(Exception ex){
            ex.printStackTrace();
        }
//
//        System.err.println("http status:" + this.responseCode );
        System.err.println("error code:" + Ints.tryParse(this.graphApiErrorCode.split("\\.")[0]));

        if ( 2 == Ints.tryParse(this.graphApiErrorCode.split("\\.")[0]) ){
            // out of retry max
            if ( retryMaxCounter-- <= 0 ) return response;
            System.err.println("Retry after 5 sec...("+ String.valueOf(retryMaxCounter) + ")" );
            try {
                Thread.currentThread().sleep(5000);
                return getGraphApi(api);
            }catch(Exception sleepEx){
                return getGraphApi(api);
            }
        }

        return response ;
    }


    public String getGraphApiErrorCode(){
        return this.graphApiErrorCode;
    }

//
//    public static void main(String args[]){
////        System.out.print("start...");
//        System.out.println(
//                new FBCrawler("626465174161774", "dbd550847406c13cd1da4085331ab54e")
//                .crawl("XDSelect?fields=id,name")
//        );
//    }
}
