package ht.fds.mbds.elanjery.tp1.jsf;

import ht.fds.mbds.elanjery.tp1.llm.JsonAdapterPourGemini;
import ht.fds.mbds.elanjery.tp1.llm.LlmInteraction;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class Bb implements Serializable {

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // Mode debug (étape 6)
    private boolean debug;
    private String texteRequeteJson;
    private String texteReponseJson;

    // Appel du LLM (étape 7)
    @Inject
    private JsonAdapterPourGemini jsonAdapter;

    @Inject
    private FacesContext facesContext;

    /**
     * Libellé du rôle -> texte du rôle système envoyé au LLM.
     * La liste déroulante utilise le libellé comme valeur : c'est plus fiable
     * qu'un texte multiligne, qui provoquait "erreur de validation".
     */
    private static final Map<String, String> ROLES = new LinkedHashMap<>();

    static {
        ROLES.put("Assistant", """
                You are a helpful assistant. You help the user to find the information they need.
                If the user type a question, you answer it.
                """);
        ROLES.put("Traducteur Anglais-Français", """
                You are an interpreter. You translate from English to French and from French to English.
                If the user type a French text, you translate it into English.
                If the user type an English text, you translate it into French.
                If the text contains only one to three words, give some examples of usage of these words in English.
                """);
        ROLES.put("Guide touristique", """
                Your are a travel guide. If the user type the name of a country or of a town,
                you tell them what are the main places to visit in the country or the town
                are you tell them the average price of a meal.
                """);
    }

    public Bb() {
    }

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation == null ? "" : conversation);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Texte question vide",
                    "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        // Début de la conversation : on envoie le rôle système à jsonAdapter
        if (conversation.isEmpty()) {
            roleSystemeChangeable = false;
            jsonAdapter.setSystemRole(ROLES.get(roleSysteme));
        }

        // Code du prof
        try {
            LlmInteraction interaction = jsonAdapter.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();
        } catch (Exception e) {
            this.reponse = "Erreur : " + e.getMessage();
            FacesMessage message =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Problème de connexion avec l'API du LLM",
                            "Problème de connexion avec l'API du LLM" + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            facesContext.addMessage(null, message);
        }

        afficherConversation();
        return null;
    }

    public String effacer() {
        question = null;
        reponse = null;
        return null;
    }

    /**
     * Retourne la page courante : JSF considère que la vue change, donc une
     * nouvelle instance du bean (portée view) est créée et l'état est réinitialisé.
     */
    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        conversation.append("== User:\n")
                .append(question)
                .append("\n== Serveur:\n")
                .append(reponse)
                .append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        List<SelectItem> liste = new ArrayList<>();
        for (String libelle : ROLES.keySet()) {
            liste.add(new SelectItem(libelle, libelle));
        }
        return liste;
    }
}