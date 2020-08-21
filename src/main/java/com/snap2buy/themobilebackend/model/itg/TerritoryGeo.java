package com.snap2buy.themobilebackend.model.itg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Territory")
@XmlAccessorType(XmlAccessType.FIELD)
public class TerritoryGeo {
    @XmlElement
    private String territoryId;
    @XmlElement
    private Boolean isDefault = new Boolean("false");

    public String getTerritoryId() {
		return territoryId;
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId = territoryId;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public TerritoryGeo(){
        super();
    }

	@Override
	public String toString() {
		return "TerritoryGeo [territoryId=" + territoryId + ", isDefault=" + isDefault + "]";
	}

}
