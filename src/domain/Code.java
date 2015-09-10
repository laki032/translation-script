package domain;

/**
 *
 * @author Lazar Vujadinovic
 */
public class Code {

    private String code;
    private String objectID;
    private String domainID;

    public Code(String codeToTranslate, String objectID, String domainID) {
        this.code = codeToTranslate;
        this.objectID = objectID;
        this.domainID = domainID;
    }

    public Code() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getDomainID() {
        return domainID;
    }

    public void setDomainID(String domainID) {
        this.domainID = domainID;
    }

}
