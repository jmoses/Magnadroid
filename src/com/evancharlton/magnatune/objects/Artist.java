package com.evancharlton.magnatune.objects;

public class Artist {
	public static final String ID = "artist_id";
	public static final String NAME = "artist_name";
	public static final String BIO = "artist_bio";
	public static final String STATE = "artist_state";
	public static final String COUNTRY = "artist_country";
	public static final String CITY = "artist_city";

	private int mId = -1;
	private String mName = "";
	private String mBio = "";
	private String mState = "";
	private String mCountry = "";
	private String mCity = "";

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
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * @return the bio
	 */
	public String getBio() {
		return mBio;
	}

	/**
	 * @param bio the bio to set
	 */
	public void setBio(String bio) {
		this.mBio = bio;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return mState;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.mState = state;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return mCountry;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.mCountry = country;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return mCity;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.mCity = city;
	}
}
