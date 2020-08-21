package com.snap2buy.themobilebackend.model.itg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Division")
@XmlAccessorType(XmlAccessType.FIELD)
public class DivisionGeo {
    @XmlElement
    private String divisionId;
    @XmlElement
    private Boolean isDefault = new Boolean("false");
    @XmlElement
    private List<TerritoryGeo> territories = new ArrayList<TerritoryGeo>();

    public DivisionGeo(){
        super();
    }

	public String getDivisionId() {
		return divisionId;
	}

	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public List<TerritoryGeo> getTerritories() {
		return territories;
	}

	public void setTerritories(List<TerritoryGeo> territories) {
		this.territories = territories;
	}

	@Override
	public String toString() {
		return "DivisionGeo [divisionId=" + divisionId + ", isDefault=" + isDefault + ", territories=" + territories
				+ "]";
	}

}
