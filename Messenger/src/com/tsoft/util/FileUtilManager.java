package com.tsoft.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

public class FileUtilManager {

	public static final String PHONE_SETTINGS_FILE_NAME = "phone_settings.txt";
	public static final String CONTENT_SETTINGS_FILE_NAME = "content_settings.txt";

	private static FileUtilManager _intance;

	private FileUtilManager() {

	}

	public static FileUtilManager getInstance() {
		if (_intance == null) {
			_intance = new FileUtilManager();
		}
		return _intance;
	}

	public void saveStringArray(Context context, ArrayList<String> list,
			String fileName) {
		BufferedWriter writer = null;
		FileWriter fWriter = null;
		try {
			File path = new File(context.getFilesDir(), "MessageBlock");

			// Create directory if not exist
			path.mkdir();

			// Create file object
			File file = new File(path, fileName);

			// Create bufferWriter
			fWriter = new FileWriter(file);
			writer = new BufferedWriter(fWriter);

			for (String str : list) {
				writer.write(str);
				writer.newLine();
			}
		} catch (Exception ex) {
		} finally {
			if (fWriter != null) {
				try {
					fWriter.close();
				} catch (IOException e) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public Set<String> readStringArray(Context context, String fileName) {
		Set<String> sets = new HashSet<String>();
		FileReader fileReader = null;
		BufferedReader reader = null;
		try {
			File path = new File(context.getFilesDir(), "MessageBlock");

			// Create file object
			File file = new File(path, fileName);

			// Create reader object
			fileReader = new FileReader(file);
			reader = new BufferedReader(fileReader);

			String str = "";
			while ((str = reader.readLine()) != null) {
				if (str.trim() != "" && !sets.contains(str)) {
					sets.add(str);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return sets;
	}
}
