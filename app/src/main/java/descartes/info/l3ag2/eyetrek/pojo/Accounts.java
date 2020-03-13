package descartes.info.l3ag2.eyetrek.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ayaz ABDUL CADER on 19/02/2018.
 */

public class Accounts {
    @SerializedName("compte")
    @Expose
    private List<Account> accounts = null;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

}
