package com.iset.covtn.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.iset.covtn.CovtnApplication;
import com.iset.covtn.exceptions.StorageException;


@Service
public class FileSystemStorageService {

	private final Path rootLocation = Paths.get(CovtnApplication.currentLocation+ "/src/main/resources/static/images");


	public String store(MultipartFile file) throws StorageException {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			String filename = System.currentTimeMillis()+ "_"+ file.getOriginalFilename();

			System.out.println("filename: " + filename);

			Path destinationFile = this.rootLocation.resolve(
					Paths.get(filename))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
					return filename;
				
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file."+e.getMessage());
		}
	}

	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1)
				.filter(path -> !path.equals(this.rootLocation))
				.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageException(
						"Could not read file: " + file);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageException("Could not read file: " + filename, e);
		}
	}

	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	public void delete(String filename) {
		try {
			Path file = load(filename);
			Files.deleteIfExists(file);
		} catch (IOException e) {
			throw new StorageException("Failed to delete file: " + filename, e);
		}
	}

	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}
}