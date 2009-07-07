package com.evancharlton.magnatune.objects;

public class Genre {
	public static final String ID = "genre_id";
	public static final String TITLE = "genre_title";

	private int mId = -1;
	private String mTitle = "";

	/**
	 * @return the id
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		mId = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		mTitle = title;
	}
}
