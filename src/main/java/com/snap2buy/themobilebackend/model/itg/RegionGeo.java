package com.snap2buy.themobilebackend.model.itg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Region")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegionGeo {
    @XmlElement
    private String regionId;
    @XmlElement
    private Boolean isDefault = new Boolean("false");
    @XmlElement
    private List<DivisionGeo> divisions = new ArrayList<DivisionGeo>();

    public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public List<DivisionGeo> getDivisions() {
		return divisions;
	}

	public void setDivisions(List<DivisionGeo> divisions) {
		this.divisions = divisions;
	}

	public RegionGeo(){
        super();
    }

	@Override
	public String toString() {
		return "RegionGeo [regionId=" + regionId + ", isDefault=" + isDefault + ", divisions=" + divisions + "]";
	}

}
