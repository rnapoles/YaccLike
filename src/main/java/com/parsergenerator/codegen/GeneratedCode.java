package com.parsergenerator.codegen;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

/**
 * Container for generated Java code files.
 * This class holds the mapping of file names to their generated content,
 * providing methods to add, retrieve, and manage generated code files.
 */
public class GeneratedCode {
    private final Map<String, String> files;
    
    /**
     * Creates a new empty GeneratedCode container.
     */
    public GeneratedCode() {
        this.files = new HashMap<>();
    }
    
    /**
     * Adds a generated file to the container.
     *
     * @param fileName the name of the file (including .java extension)
     * @param content the generated Java code content
     * @throws IllegalArgumentException if fileName is null or empty, or content is null
     */
    public void addFile(String fileName, String content) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        
        files.put(fileName.trim(), content);
    }
    
    /**
     * Gets the content of a generated file.
     *
     * @param fileName the name of the file
     * @return the file content, or null if the file doesn't exist
     */
    public String getFileContent(String fileName) {
        if (fileName == null) {
            return null;
        }
        return files.get(fileName.trim());
    }
    
    /**
     * Checks if a file with the given name exists in the generated code.
     *
     * @param fileName the name of the file to check
     * @return true if the file exists, false otherwise
     */
    public boolean hasFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        return files.containsKey(fileName.trim());
    }
    
    /**
     * Returns the set of all generated file names.
     *
     * @return unmodifiable set of file names
     */
    public Set<String> getFileNames() {
        return Collections.unmodifiableSet(files.keySet());
    }
    
    /**
     * Returns an unmodifiable map of all files and their content.
     *
     * @return map of file names to content
     */
    public Map<String, String> getAllFiles() {
        return Collections.unmodifiableMap(files);
    }
    
    /**
     * Returns the number of generated files.
     *
     * @return the file count
     */
    public int getFileCount() {
        return files.size();
    }
    
    /**
     * Checks if the generated code container is empty.
     *
     * @return true if no files have been generated, false otherwise
     */
    public boolean isEmpty() {
        return files.isEmpty();
    }
    
    /**
     * Removes a file from the generated code.
     *
     * @param fileName the name of the file to remove
     * @return true if the file was removed, false if it didn't exist
     */
    public boolean removeFile(String fileName) {
        if (fileName == null) {
            return false;
        }
        return files.remove(fileName.trim()) != null;
    }
    
    /**
     * Clears all generated files.
     */
    public void clear() {
        files.clear();
    }
    
    /**
     * Returns the total size of all generated code in characters.
     *
     * @return the total character count across all files
     */
    public int getTotalSize() {
        return files.values().stream()
                   .mapToInt(String::length)
                   .sum();
    }
    
    @Override
    public String toString() {
        return "GeneratedCode{" +
                "fileCount=" + files.size() +
                ", totalSize=" + getTotalSize() +
                '}';
    }
    
    /**
     * Returns a detailed string representation including file names and sizes.
     *
     * @return detailed string representation
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GeneratedCode {\n");
        sb.append("  Files: ").append(files.size()).append("\n");
        sb.append("  Total Size: ").append(getTotalSize()).append(" characters\n");
        
        if (!files.isEmpty()) {
            sb.append("  File Details:\n");
            for (Map.Entry<String, String> entry : files.entrySet()) {
                sb.append("    ").append(entry.getKey())
                  .append(" (").append(entry.getValue().length()).append(" chars)\n");
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
}