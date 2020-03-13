package descartes.info.l3ag2.eyetrek.pojo;

/**
 * Created by Ayaz ABDUL CADER on 11/03/2018.
 */

public class DidacticielPreferences {

    private boolean mainDidacticiel, settingsDidacticiel, profilDidacticiel, searchDidacticiel;

    public DidacticielPreferences(boolean mainDidacticiel, boolean settingsDidacticiel, boolean profilDidacticiel, boolean searchDidacticiel) {
        this.mainDidacticiel = mainDidacticiel;
        this.settingsDidacticiel = settingsDidacticiel;
        this.profilDidacticiel = profilDidacticiel;
        this.searchDidacticiel = searchDidacticiel;

    }

    public boolean isMainDidacticiel() {
        return mainDidacticiel;

    }

    public void setMainDidacticiel(boolean mainDidacticiel) {
        this.mainDidacticiel = mainDidacticiel;

    }

    public boolean isSettingsDidacticiel() {
        return settingsDidacticiel;

    }

    public void setSettingsDidacticiel(boolean settingsDidacticiel) {
        this.settingsDidacticiel = settingsDidacticiel;

    }

    public boolean isProfilDidacticiel() {
        return profilDidacticiel;

    }

    public void setProfilDidacticiel(boolean profilDidacticiel) {
        this.profilDidacticiel = profilDidacticiel;

    }

    public boolean isSearchDidacticiel() {
        return searchDidacticiel;

    }

    public void setSearchDidacticiel(boolean searchDidacticiel) {
        this.searchDidacticiel = searchDidacticiel;

    }

}
