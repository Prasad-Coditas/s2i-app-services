package com.snap2buy.themobilebackend.model.itg;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "GeoHierarchy")
@XmlAccessorType(XmlAccessType.FIELD)
public class GenericGeo {
    @XmlElement
    private String id;
    @XmlElement
    private Boolean isDefault = new Boolean("false");
    @XmlElement
    private String childGeoLevelType;
    @XmlElement
    private List<GenericGeo> childGeoLevels = new ArrayList<GenericGeo>();

    public GenericGeo(){
        super();
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getChildGeoLevelType() {
		return childGeoLevelType;
	}

	public void setChildGeoLevelType(String childGeoLevelType) {
		this.childGeoLevelType = childGeoLevelType;
	}

	public List<GenericGeo> getChildGeoLevels() {
		return childGeoLevels;
	}

	public void setChildGeoLevels(List<GenericGeo> childGeoLevels) {
		this.childGeoLevels = childGeoLevels;
	}

	@Override
	public String toString() {
		return "GenericGeo [id=" + id + ", isDefault=" + isDefault + ", childGeoLevelType=" + childGeoLevelType
				+ ", childGeoLevels=" + childGeoLevels + "]";
	}

}
