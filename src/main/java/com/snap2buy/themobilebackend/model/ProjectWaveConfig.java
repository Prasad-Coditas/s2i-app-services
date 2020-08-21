package com.snap2buy.themobilebackend.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Anoop on 08/08/19.
 */
@XmlRootElement(name = "ProjectWaveConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectWaveConfig {
    
	@XmlElement
    String projectId;
	
	@XmlElement
    String waveId;
    
    @XmlElement
    String waveName;

    public ProjectWaveConfig() {
        super();
    }

    public ProjectWaveConfig(String projectId, String waveId, String waveName) {
        this.projectId = projectId;
    	this.waveId = waveId;
        this.waveName = waveName;
    }

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getWaveId() {
		return waveId;
	}

	public void setWaveId(String waveId) {
		this.waveId = waveId;
	}

	public String getWaveName() {
		return waveName;
	}

	public void setWaveName(String waveName) {
		this.waveName = waveName;
	}

	@Override
	public String toString() {
		return "ProjectWaveConfig [projectId=" + projectId + ", waveId=" + waveId + ", waveName=" + waveName + "]";
	}
}
