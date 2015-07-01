package com.lmdna.spider.protocol.rpc.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author ayumiono
 */
public class FileUploadObject implements Serializable {

	private static final long serialVersionUID = -3947893626459088418L;

	/** 文件名长度 */
	private int nameLength;
	/** 文件名 */
	private byte[] fileName;
	/** 文件长度 */
	private long contentLength;
	/** 文件内容 */
	private byte[] contents;
	/** 文件路径 */
	private byte[] filePath;
	/** 文件路径长度 */
	private int filePathLength;

	public FileUploadObject() {
	}

	public FileUploadObject(int nameLength, byte[] fileName,
			int contentLength, byte[] contents) {
		this.nameLength = nameLength;
		this.fileName = fileName;
		this.contentLength = contentLength;
		this.contents = contents;
	}

	public int getNameLength() {
		return nameLength;
	}

	public void setNameLength(int nameLength) {
		this.nameLength = nameLength;
	}

	public byte[] getFileName() {
		return fileName;
	}

	public void setFileName(byte[] fileName) {
		this.fileName = fileName;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}

	public byte[] getFilePath() {
		return filePath;
	}

	public void setFilePath(byte[] filePath) {
		this.filePath = filePath;
	}

	public int getFilePathLength() {
		return filePathLength;
	}

	public void setFilePathLength(int filePathLength) {
		this.filePathLength = filePathLength;
	}

	@Override
	public String toString() {
		return "[ nameLength : " + nameLength + " ,fileName : "
				+ Arrays.toString(fileName) + " ,contentLength : "
				+ contentLength + " ,contents : " + contentLength + "]";
	}
}
