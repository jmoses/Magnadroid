package com.evancharlton.magnatune;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Integer, Boolean> {
	private static final String TAG = "DownloadTask";

	public DownloadAlbum activity;
	private String mUsername;
	private String mPassword;
	private String mAlbum;
	private String mArtist;

	public DownloadTask(String username, String password, String album, String artist) {
		mUsername = username;
		mPassword = password;
		mAlbum = album;
		mArtist = artist;
	}

	@Override
	protected void onPreExecute() {
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mUsername, mPassword.toCharArray());
			}
		});
		activity.startProgress();
		activity.acquireWakeLock();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		activity.update(activity.getString(progress[0]), progress[1], progress[2]);
		activity.acquireWakeLock();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		boolean success = true;
		publishProgress(R.string.downloading_message, -1, -1);
		final String filename = MagnatuneAPI.CACHE_DIRECTORY + String.format("download_%d.zip", System.currentTimeMillis());
		try {
			URL url = new URL(params[0]);
			InputStream is = (InputStream) url.getContent();
			URLConnection conn = url.openConnection();
			BufferedInputStream bis = new BufferedInputStream(is);
			FileOutputStream fos = new FileOutputStream(filename);
			byte[] buf = new byte[4096]; // 4KB buffer
			int count = 0;
			Log.d(TAG, "Starting download: " + params[0]);
			int position = 0;
			int length = conn.getContentLength();
			while ((count = bis.read(buf)) >= 0) {
				fos.write(buf, 0, count);
				position += count;
				publishProgress(R.string.downloading_message, position, length);
				if (isCancelled()) {
					Log.d(TAG, "Download cancelled!");
					success = false;
					break;
				}
			}
			fos.flush();
			fos.close();
			bis.close();
			if (!isCancelled()) {
				Log.d(TAG, "Done! Preparing for unzip ...");
				publishProgress(R.string.unzipping_message, -1, -1);
				ZipFile zip = new ZipFile(filename);
				Enumeration<? extends ZipEntry> entries = zip.entries();
				File song;
				ZipEntry entry;
				position = 0;
				length = zip.size();
				String outdir = null;
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					if (entry.isDirectory()) {
						Log.d(TAG, "Making folder: " + entry.getName());
						new File(MagnatuneAPI.MUSIC_DIRECTORY + entry.getName()).mkdirs();
					}
					// write the file
					Log.d(TAG, "Extracting: " + entry.getName());
					is = zip.getInputStream(entry);
					song = new File(MagnatuneAPI.MUSIC_DIRECTORY + entry.getName());
					outdir = song.getParentFile().getAbsolutePath();
					song.getParentFile().mkdirs();
					song.createNewFile();
					fos = new FileOutputStream(song);
					while ((count = is.read(buf)) >= 0) {
						if (isCancelled()) {
							success = false;
							break;
						}
						fos.write(buf, 0, count);
					}
					fos.flush();
					fos.close();
					publishProgress(R.string.unzipping_message, ++position, length);
					if (isCancelled()) {
						song.delete();
						success = false;
						break;
					}
					activity.scanFile(MagnatuneAPI.MUSIC_DIRECTORY + entry.getName(), null);
				}
				zip.close();
				if (outdir != null) {
					String cover = MagnatuneAPI.getCoverArtUrl(mArtist, mAlbum, 300);
					URL request = new URL(cover);
					publishProgress(R.string.downloading_cover_art, -1, -1);
					String coverPath = outdir + "/Folder.jpg";
					Log.d(TAG, "Downloading the cover art");
					Log.d(TAG, "Downloading to: " + coverPath);
					File coverFile = new File(coverPath);
					coverFile.createNewFile();
					position = 0;
					length = request.openConnection().getContentLength();
					bis = new BufferedInputStream((InputStream) request.getContent());
					fos = new FileOutputStream(coverFile);
					while ((count = bis.read(buf)) >= 0) {
						position += count;
						fos.write(buf, 0, count);
						publishProgress(R.string.downloading_cover_art, position, length);
					}
					activity.scanFile(coverPath, null);
					fos.flush();
					fos.close();
					bis.close();
				}
			}
			success = success && !isCancelled();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			success = false;
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		} finally {
			Log.d(TAG, "Done! Cleaning up the zip file ...");
			File zip = new File(filename);
			zip.delete();
			Log.d(TAG, "Terminating with status: " + success);
		}
		return success;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		activity.done();
		activity.releaseWakeLock();
	}
}