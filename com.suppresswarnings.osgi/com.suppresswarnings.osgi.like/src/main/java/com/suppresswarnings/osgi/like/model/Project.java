package com.suppresswarnings.osgi.like.model;

import com.suppresswarnings.corpus.common.KeyValue;

public class Project {

	String projectid;
	String openid;
	String face;
	String uname;
	String time;
	String title;
	String pictures;
	String bonusCent;
	String target;
	String sponsor;
	String liked;
	String comment;
	String invest;
	Page<KeyValue> likes;
	Page<KeyValue> comments;
	Page<KeyValue> forwards;
	Page<KeyValue> invests;
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getInvest() {
		return invest;
	}
	public void setInvest(String invest) {
		this.invest = invest;
	}
	public String getLiked() {
		return liked;
	}
	public void setLiked(String liked) {
		this.liked = liked == null ? "0" : liked;
	}
	public String getSponsor() {
		return sponsor;
	}
	public void setSponsor(String sponsor) {
		this.sponsor = sponsor == null ? "0" : sponsor;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target == null ? "10000" : target;
	}
	public Page<KeyValue> getComments() {
		return comments;
	}
	public void setComments(Page<KeyValue> comments) {
		this.comments = comments;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public String getProjectid() {
		return projectid;
	}
	public void setProjectid(String projectid) {
		this.projectid = projectid;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPictures() {
		return pictures;
	}
	public void setPictures(String pictures) {
		this.pictures = pictures;
	}
	public String getBonusCent() {
		return bonusCent;
	}
	public void setBonusCent(String bonusCent) {
		this.bonusCent = bonusCent;
	}
	public Page<KeyValue> getLikes() {
		return likes;
	}
	public void setLikes(Page<KeyValue> likes) {
		this.likes = likes;
	}
	public Page<KeyValue> getForwards() {
		return forwards;
	}
	public void setForwards(Page<KeyValue> forwards) {
		this.forwards = forwards;
	}
	public Page<KeyValue> getInvests() {
		return invests;
	}
	public void setInvests(Page<KeyValue> invests) {
		this.invests = invests;
	}
	public void addPicture(String image) {
		if(pictures == null || pictures.length() < 1) {
			pictures = image;
		} else {
			pictures = pictures + "," + image;
		}
	}
	@Override
	public String toString() {
		return "Project [projectid=" + projectid + ", openid=" + openid + ", time=" + time + ", title=" + title
				+ ", pictures=" + pictures + ", bonusCent=" + bonusCent + "]";
	}
}
