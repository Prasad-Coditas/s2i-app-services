package com.snap2buy.themobilebackend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anoop on 5/11/17.
 */
public class SnapResultList {
	
	List<PrmResult> SnapResultList = new ArrayList<PrmResult>();
	
	public SnapResultList(List<PrmResult> SnapResultList ) {
		this.SnapResultList = SnapResultList;
	}

	public List<PrmResult> getSnapResultList() {
		return SnapResultList;
	}

	public void setSnapResultList(List<PrmResult> snapResultList) {
		SnapResultList = snapResultList;
	}
	
	
}