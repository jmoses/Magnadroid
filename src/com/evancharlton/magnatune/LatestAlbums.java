package com.evancharlton.magnatune;

public class LatestAlbums extends AlbumList {
	@Override
	protected LoadTask newLoadTask() {
		return new LoadAlbumsTask();
	}

	@Override
	protected String getUrl() {
		return MagnatuneAPI.API_BASE + "/albums/newest/?page=" + mPage;
	}
}