/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.process;

/**
 *Default class which store Doctype value
 * @author Hyacinthe Tsague
 */
public class Doctype {
    private String dtdName;
    private String locationType;
    private String dtdFilePath;
    private boolean isInternal;
    private boolean isExternal;
    private String dtdContent;


    public String getDTDName() {
        return this.dtdName;
    }

    public String getLocationType() {
        return this.locationType;
    }

    public String dtdFilePath() {
        return this.dtdFilePath;
    }

    public void setDtdName(String dtdName) {
        this.dtdName = dtdName;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public void setDtdFilePath(String dtdFilePath) {
        this.dtdFilePath = dtdFilePath;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    public String getDtdContent() {
        return dtdContent;
    }

    public void setDtdContent(String dtdContent) {
        this.dtdContent = dtdContent;
    }
}
