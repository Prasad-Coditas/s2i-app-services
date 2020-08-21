package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Harshal
 *
 */
@XmlRootElement(name = "MenuMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class MenuMap {
    @XmlElement
    String menuId;
    @XmlElement
    String isDefault;
    @XmlElement
    String source;

    public MenuMap() {
        super();
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "MenuMap{" +
                "menuId='" + menuId + '\'' +
                ", isDefault='" + isDefault + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
