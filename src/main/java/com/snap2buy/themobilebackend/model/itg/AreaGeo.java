package com.snap2buy.themobilebackend.model.itg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Area")
@XmlAccessorType(XmlAccessType.FIELD)
public class AreaGeo {
    @XmlElement
    private String areaId;
    @XmlElement
    private Boolean isDefault = new Boolean("false");
    @XmlElement
    private List<RegionGeo> regions = new ArrayList<RegionGeo>();

    public AreaGeo(){
        super();
    }

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public List<RegionGeo> getRegions() {
		return regions;
	}

	public void setRegions(List<RegionGeo> regions) {
		this.regions = regions;
	}

	@Override
	public String toString() {
		return "AreaGeo [areaId=" + areaId + ", isDefault=" + isDefault + ", regions=" + regions + "]";
	}


}
