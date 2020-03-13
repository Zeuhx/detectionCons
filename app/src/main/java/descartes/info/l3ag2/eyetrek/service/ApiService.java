package descartes.info.l3ag2.eyetrek.service;

import descartes.info.l3ag2.eyetrek.pojo.Accounts;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Ayaz ABDUL CADER on 19/02/2018.
 */

public interface ApiService {


    public static final String LINK= "http://www.ens.math-info.univ-paris5.fr/~if04812/api.php/";

    /**
     * Récupération du compte suivant le mail
     * @param filter
     * @param transform
     * @return
     */

    @GET("compte")
    Call<Accounts> getAccount(
            @Query("filter") String filter,
            @Query("transform") int transform
    );

    /**
     * Ajout d'un compte
     * @param requestBody
     * @return
     */

    @POST("compte")
    Call<ResponseBody> setAccount(
            @Body RequestBody requestBody
    );

    @Headers({
            "login: l3ag2",
            "password: ripKitMouf3"
    })
    @PUT("compte/{id}")
    Call<ResponseBody> updateAccount(
            @Path("id") String id,
      @Body RequestBody requestBody
    );


}
