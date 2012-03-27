/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.utils.helper;

import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.filefilter.ExtensionFileFilter;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @version 1.0 Jan 18, 2010 9:00:48 AM
 * @author: Maksim Khadkevich
 */
public class HelperFile {

    protected static Logger logger = CRSLogger.getLogger(HelperFile.class);


    public static void createDir(String dirPath) {
        File tempDir = getFile(dirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
            logger.debug("Creating directory " + dirPath);
        }
    }

    /**
     * Creates fileList of <tt>dirPath</tt> using filefilter
     *
     * @param dirPath
     * @param outFilePath
     * @param filter
     * @param isUseQuotes
     */
    public static void createFileList(String dirPath, String outFilePath, FileFilter filter, boolean isUseQuotes) {
        try {
            FileWriter writer = new FileWriter(getFile(outFilePath));
            parseDirForCreateFileList(writer, getFile(dirPath), filter, isUseQuotes);
            writer.close();
        } catch (IOException e) {
            logger.error("Error occured");
            logger.error(Helper.getStackTrace(e));
        }
    }

    private static void parseDirForCreateFileList(FileWriter writer, File dir, FileFilter filter, boolean isUseQuotes) throws IOException {
        File[] fileList = dir.listFiles(filter);
        for (File f : fileList) {
            if (f.isDirectory()) {
                parseDirForCreateFileList(writer, f, filter, isUseQuotes);
            } else {
                if (isUseQuotes) {
                    writer.write("\"" + f.getAbsolutePath() + "\"\n");
                } else {
                    writer.write(f.getAbsolutePath() + "\n");
                }
            }
        }
    }

    /**
     * Searches in a <code>dirPath</code> for the first occurence of file that has the same name as <code>fileName</code>
     * and extension <code>extension</code>
     *
     * @param fileName  fileName
     * @param dirPath   dirPath
     * @param extension extension
     * @return path of found file
     */
    public static String getPathForFileWithTheSameName(String fileName, String dirPath, String extension) {
        if (fileName == null || fileName.equals("")) {
            return null;
        }
        try {
            File dir = getFile(dirPath);
            File[] fileList = dir.listFiles(new ExtensionFileFilter(new String[]{extension}));
            String returnValue;
            for (File f : fileList) {
                if (f.isDirectory()) {
                    returnValue = getPathForFileWithTheSameName(fileName, f.getAbsolutePath(), extension);
                    if (returnValue != null) {
                        return returnValue;
                    }
                } else {
                    String fileNameWithoutExtension;
                    if ((fileName.lastIndexOf(".") < 0)) {
                        fileNameWithoutExtension = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
                    } else {
                        fileNameWithoutExtension = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.lastIndexOf("."));
                    }
                    if ((fileNameWithoutExtension + extension).equalsIgnoreCase(f.getName())) {
                        return f.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(String.format("Path for fileName %s in directory %s was not found", fileName, dirPath));
        }
        return null;
    }


    /**
     * @param fileName
     * @param dirPath
     * @param extensions
     * @return
     */
    public static List<String> getFilePathsIgnoreExtension(String fileName, String dirPath, String[] extensions) {
        if (fileName == null || fileName.equals("")) {
            throw new IllegalArgumentException(String.format("Could not take fileName %s as argument", fileName));
        }
        List<String> returnValues = new ArrayList<String>();
        try {
            File dir = getFile(dirPath);
            File[] fileList;
            if (extensions == null) {
                fileList = dir.listFiles();
            } else{
                fileList = dir.listFiles(new ExtensionFileFilter(extensions));
            }
            for (File f : fileList) {
                if(f.isDirectory()){
                    continue;
                }
                String fileNameWithoutExtension = getNameWithoutExtension(f.getName());
                String fileNameQuery = getNameWithoutExtension(fileName);
                if (fileNameWithoutExtension.equalsIgnoreCase(fileNameQuery)) {
                    returnValues.add(f.getPath());
                }
            }
        } catch (Exception e) {
            logger.debug(String.format("Path for fileName %s in directory %s was not found", fileName, dirPath));
            logger.debug(Helper.getStackTrace(e));
        }
        return returnValues;
    }


    public static List<String> getFilePathsIgnoreExtension(String fileName, String dirPath) {
        return getFilePathsIgnoreExtension(fileName, dirPath, null);
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            try {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException e) {
                logger.error(String.format("Could not copy dir %s to %s", sourceLocation.getPath(), targetLocation.getPath()));
                logger.error(Helper.getStackTrace(e));
            }
        }
    }


    public static void copyDirectory(String sourceLocation, String targetLocation) {
        File sourceDir = getFile(sourceLocation);
        File targetDir = getFile(targetLocation);
        copyDirectory(sourceDir, targetDir);
    }

    public static void copyFile(String srFile, String dtFile) {
        File sourceFile = getFile(srFile);
        File desFile = getFile(dtFile);
        copyFile(sourceFile, desFile);
    }

    public static void copyFile(File sourceFile, File destFile) {
        try {
            InputStream in = new FileInputStream(sourceFile);

            //For Append the file.
//      OutputStream out = new FileOutputStream(destFile,true);

            //For Overwrite the file.
            OutputStream out = new FileOutputStream(destFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            logger.debug(String.format("File %s copied to file %s", sourceFile.getPath(), destFile.getPath()));
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage() + " in the specified directory.");
//            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteDirectory(File directory) {
        if (!directory.exists()) {
            return;
        }
        if (directory.isFile()) {
            directory.delete();
        } else {
            File[] children = directory.listFiles();
            for (File child : children) {
                deleteDirectory(child);
            }
        }
        directory.delete();
    }

    public static void deleteDirectory(String dirPath) {
        deleteDirectory(getFile(dirPath));
    }


    public static File getFile(String filePath) {
        File outFile = null;
        if (filePath == null) {
            return null;
        }
        if (filePath.startsWith("\"")) {
            filePath = filePath.replaceAll("\"", "");
        }
        try {
            String filePath_out = URLDecoder.decode(filePath, System.getProperty("file.encoding"));
            outFile = new File(filePath_out);
            if (!outFile.exists()) {
//                logger.debug(String.originalFormat("Probable problem with file %s (does not exist)", outFile.getPath()));
            }
        } catch (Exception e) {
//            logger.error(String.originalFormat("Error with file %s", url));
            logger.error(Helper.getStackTrace(e));
        }
        return outFile;
    }

    /**
     * Returns File in the resource path location
     * @param resourcePath Resource relative file path
     * @return Found file
     * @throws FileNotFoundException If file was not found 
     */
    public static File getResourceFile(String resourcePath) throws FileNotFoundException{
        if(!resourcePath.startsWith("/")){
            resourcePath = "/" + resourcePath;
        }
        URL myTestURL = HelperFile.class.getResource(resourcePath);
        File myFile;
        try {
            myFile = new File(myTestURL.toURI());
        } catch (NullPointerException e) {
            throw new FileNotFoundException(String.format("Could not find resource %s in resources", resourcePath));
        } catch (URISyntaxException e) {
            throw new FileNotFoundException(String.format("Could not find resource %s in resources", resourcePath));
        }
        return myFile;
    }

    /**
     * Returns file path for a given resource
     * @param resourcePath Resource relative file path
     * @return   Found file path
     * @throws java.io.FileNotFoundException If file was not found
     */
    public static String getResourceFilePath(String resourcePath) throws FileNotFoundException{
       return getResourceFile(resourcePath).getPath();
    }


    public static String getShortFileName(String filePath) {
        return (getFile(filePath).getName());
    }

    public static String getShortFileNameWithoutExt(String filePath) {
        String fileName = getShortFileName(filePath);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws java.net.URISyntaxException
     * @throws IOException
     * @author Greg Briggs
     */
    public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
            * In case of a jar file, we can't actually find a directory.
            * Have to assume the same jar as clazz.
            */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }


    /**
     * Stores collection in a File
     *
     * @param collection      collection
     * @param filePathToStore filePathToStore
     * @param isToUseQuotes   isToUseQuotes
     */
    public static void saveCollectionInFile(Collection<String> collection, String filePathToStore, boolean isToUseQuotes) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePathToStore));
            for (String s : collection) {
                if (isToUseQuotes) {
                    writer.write("\"" + s + "\"\r\n");
                } else {
                    writer.write(s + "\r\n");
                }
            }
            writer.close();

        } catch (IOException e) {
            logger.error("IO Error occured");
            logger.error(Helper.getStackTrace(e));
        }
    }

    public static void saveCollectionInFile(Collection<String> collection, String filePathToStore) {
        saveCollectionInFile(collection, filePathToStore, false);
    }

    /**
     * Reads Strings from File
     *
     * @param fileName                 fileName, containing list of files
     * @param maxNumberOfTokensPerLine maxNumberOfTokensPerLine
     * @return map with time delays information
     */
    public static List<String> readTokensFromTextFile(String fileName, int maxNumberOfTokensPerLine) {
        File file = getFile(fileName);
        List<String> outList = new ArrayList<String>();
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line, token;
                while ((line = reader.readLine()) != null && line.length() > 1) {
                    int counter = 0;
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    while (tokenizer.hasMoreTokens() && counter <= maxNumberOfTokensPerLine) {
                        token = tokenizer.nextToken(" \t\n\r\f");
                        if (token.startsWith("\"") && !token.endsWith("\"")) {
                            token = token + tokenizer.nextToken("\"");
                            tokenizer.nextToken(" \t\n\r\f");
                        }
                        token = token.replaceAll("\"", "");
                        outList.add(token);
                        counter++;
                    }
                }
                reader.close();
            } catch (IOException e) {
                logger.error("Cannot read data from fileListTrain file ");
                logger.error(Helper.getStackTrace(e));
            }
        }
        return outList;
    }

    public static List<String[]> readTokensFromFileStrings(String filePath, int numberOfTokensPerLine) {
        List<String[]> out = new ArrayList<String[]>();

        File file = getFile(filePath);
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                String key;
                String value;

                while ((line = reader.readLine()) != null && line.length() > 1) {
                    String[] outStrings = new String[numberOfTokensPerLine];
                    for (int i = 0; i < outStrings.length; i++) {
                        outStrings[i] = "";
                    }

                    StringTokenizer tokenizer = new StringTokenizer(line);
                    for (int i = 0; i < numberOfTokensPerLine; i++) {
                        if (!tokenizer.hasMoreTokens()) {
                            break;
                        }
                        key = tokenizer.nextToken((" \t\n\r\f"));
                        if (key.startsWith("\"") && !key.endsWith("\"")) {
                            key = key + tokenizer.nextToken("\"");
                            tokenizer.nextToken(" \t\n\r\f");
                        }
                        outStrings[i] = key.replaceAll("\"", "");
                    }
                    out.add(outStrings);
                }
                reader.close();
            } catch (IOException e) {
                logger.error("Exception occured: ");
                logger.error(Helper.getStackTrace(e));
            }
        }

        return out;
    }


    public static List<String> readLinesFromTextFile(String fileName) {
        return readLinesFromTextFile(fileName, true);
    }

    public static List<String> readLinesFromTextFile(String fileName, boolean skipEmplyLines) {
        File file = getFile(fileName);
        List<String> outfileList = new ArrayList<String>();
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (skipEmplyLines && line.trim().length() < 1) {
                        continue;
                    }
                    line.replaceAll("\"", "");
                    outfileList.add(line.trim());
                }
                reader.close();
            } catch (IOException e) {
                logger.error("Cannot read data from fileListTrain file ");
                logger.error(Helper.getStackTrace(e));
            }
        } else {
            logger.error(String.format("File %s does not exist", fileName));
        }
        return outfileList;
    }

    public static float[] readFloatPerStringFromTextFile(String fileName) {
        List<String> strings = readLinesFromTextFile(fileName);
        float[] out = new float[strings.size()];
        int index = 0;
        for (String string : strings) {
            out[index++] = Float.valueOf(string);
        }
        return out;
    }


    /**
     * Reads Map structure from File
     *
     * @param filePathToStore fileName, containing list of files
     * @return map with time delays information
     */
    public static void saveMapInTextFile(Map map, String filePathToStore) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePathToStore));
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                writer.write(String.format("%s\t%s\r\n", Helper.getStringValueForObject(key), Helper.getStringValueForObject(value)));
            }
            writer.close();

        } catch (IOException e) {
            logger.error("IO Error occured");
            logger.error(Helper.getStackTrace(e));
        }
    }

    public static void saveDoubleDataInTextFile(double[] data, String filePathToStore) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePathToStore));
            for (int i = 0; i < data.length; i++) {
                writer.write(String.format("%8.3f\r\n", data[i]));
            }
            writer.close();

        } catch (IOException e) {
            logger.error("IO Error occured");
            logger.error(Helper.getStackTrace(e));
        }
    }


    public static void saveDoubleDataInTextFile(List<Double> data, String filePathToStore) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePathToStore));
            for (int i = 0; i < data.size(); i++) {
                writer.write(String.format("%8.3f\r\n", data.get(i)));
            }
            writer.close();

        } catch (IOException e) {
            logger.error("IO Error occured");
            logger.error(Helper.getStackTrace(e));
        }
    }

    /**
     * Reads Map structure from File
     *
     * @param fileName fileName, containing list of files
     * @return map with time delays information
     */
    public static Map<String, String> readMapFromTextFile(String fileName) {
        File file = getFile(fileName);
        if (file.exists()) {
            try {
                return readMapFromReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                logger.error("Cannot read data from fileListTrain file ");
                logger.error(Helper.getStackTrace(e));

            }
        }
        return null;
    }

    /**
     * Reads Map structure from Reader
     *
     * @param reader_
     * @return
     */
    public static Map<String, String> readMapFromReader(Reader reader_) {
        Map<String, String> outMap = new HashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(reader_);
            String line;
            String key;
            String value;
            while ((line = reader.readLine()) != null && line.length() > 1) {
                StringTokenizer tokenizer = new StringTokenizer(line);
                key = tokenizer.nextToken((" \t\n\r\f"));
                if (key.startsWith("\"") && !key.endsWith("\"")) {
                    key = key + tokenizer.nextToken("\"");
                    tokenizer.nextToken(" \t\n\r\f");
                }
                value = tokenizer.nextToken(" \t\n\r\f");
                if (value.startsWith("\"") && !value.endsWith("\"")) {
                    value = value + tokenizer.nextToken("\"");
                    tokenizer.nextToken(" \t\n\r\f");
                }
                outMap.put(key.replaceAll("\"", ""), value.replaceAll("\"", ""));
            }
            reader.close();
        } catch (IOException e) {
            logger.error("Exception occured: ");
            logger.error(Helper.getStackTrace(e));

        }
        return outMap;
    }


    public static String getExtension(String filePath) {
        if (filePath.contains(".")) {
            return filePath.substring(filePath.lastIndexOf("."));
        } else {
            return "";
        }
    }

    public static String getExtension(File file) {
        return getExtension(file.getName());
    }


    public static String getNameWithoutExtension(String filePath) {
        String shortName = getFile(filePath).getName();
        if (shortName.indexOf(".") > 0) {
            return shortName.substring(0, shortName.lastIndexOf("."));
        }
        return shortName;
    }

    public static String getFilePathWithoutExtension(String filePath) {
        if (filePath.indexOf(".") > 0) {
            return filePath.substring(0, filePath.lastIndexOf("."));
        }
        return filePath;
    }


    public static void renameFile(File src, File dest, boolean overwrite) {
        if (dest.exists() && overwrite) {
            dest.delete();
        }
        src.renameTo(dest);
    }


    /**
     * Renames all dirs under parent dir
     *
     * @param parentDirPath parentDirPath
     * @param dirNameFrom   dirNameFrom
     * @param dirNameTo     dirNameTo
     */
    public static void renameAllDirNames(String parentDirPath, String dirNameFrom, String dirNameTo) {
        File[] children = getFile(parentDirPath).listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                renameAllDirNames(child.getAbsolutePath(), dirNameFrom, dirNameTo);
            }
            if (getNameWithoutExtension(child.getName()).equals(dirNameFrom)) {
                File renameToFile = getFile(child.getParentFile() + File.separator + dirNameTo + getExtension(child.getName()));
                renameFile(child, renameToFile, true);
            }
        }
    }


    //TODO remove this method

    public static List<String> findFilesWithName(String rootDirPath, String fileName) {
        List<String> outList = new ArrayList<String>();

        File rootDir = getFile(rootDirPath);

        File[] files = rootDir.listFiles();
        for (File aFile : files) {
            if (aFile.getName().toLowerCase().contains(fileName.toLowerCase())) {
                outList.add(aFile.getPath());
            }
        }

        return outList;

    }


    public static List<File> findFiles(String dirPath, String extension) {
        return findFiles(dirPath, new String[]{extension});
    }

    public static List<File> findFiles(String dirPath, String[] extensions) {
        List<File> outList = new ArrayList<File>();
        addFilesToList(getFile(dirPath), extensions, outList);
        return outList;
    }

    private static void addFilesToList(File dir, String[] extensions, List<File> outList) {
        File[] files = dir.listFiles(new ExtensionFileFilter(extensions));
        for (File file : files) {
            if (file.isDirectory()) {
                addFilesToList(file, extensions, outList);
            } else {
                outList.add(file);
            }
        }
    }


}
