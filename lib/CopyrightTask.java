import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * 
 * Ant Task which updates / adds copyright notes in java files.
 * 
 * <p>
 * Use it like this inside a <code>build.xml</code>-file for <a
 * href="http://www.ant.org">ant</a>:
 * 
 * <pre>
 *   &lt;target name=&quot;copyright&quot; description=&quot;Update copyright notes in java files&quot;&gt;
 *     &lt;taskdef name=&quot;copyright&quot; classname=&quot;CopyrightTask&quot; classpath=&quot;lib&quot;&gt; &lt;/taskdef&gt;
 *     &lt;copyright file=&quot;LICENSE&quot;&gt;
 *       &lt;fileset dir=&quot;src&quot; includes=&quot;**&#47;*.java&quot;/&gt;
 *     &lt;/copyright&gt;
 *   &lt;/target&gt;
 * </pre>
 * 
 * or with an inlined copyright like this:
 * 
 * <pre>
 *   ...
 *   &lt;copyright text=&quot;/* Copyright (c) 2010 by Foo Bar.&amp;#10; * For 
 *      more information consult LICENCE.TXT *&#47;&quot;&gt;
 *     ...
 *   &lt;/copyright&gt;
 *   ...
 * </pre>
 * 
 * You may break lines in the text here. However, only the
 * <code>&amp;#10;</code> code inserts a line break.
 * 
 * <p>Compile this via:
 * <pre>
 *   javac -cp <i>/somepath/</i>ant.jar CopyrightTask.java
 * </pre>
 * 
 * @author mattias ulbrich
 * 
 */
public class CopyrightTask extends Task {

    private String licenseFilename;

    private String licenseText;

    private List<FileSet> filesets = new ArrayList<FileSet>();

    public void execute() {

        if (licenseFilename == null && licenseText == null) {
            throw new BuildException(
                    "Either 'file' or 'text' must be specified");
        }

        if (licenseFilename != null && licenseText != null) {
            throw new BuildException(
                    "Only one of 'file' or 'text' may be specified");
        }

        if (licenseText == null) {
            licenseText = textFromFile(new File(licenseFilename)).toString();
        }

        licenseText = licenseText.trim();

        if (!licenseText.startsWith("/*")) {
            throw new BuildException(
                    "The licence does not seem to be a java comment (does not start with '*/')");
        }

        if (!licenseText.endsWith("*/")) {
            throw new BuildException(
                    "The licence does not seem to be a java comment (does not end with '*/')");
        }

        for (FileSet fileset : filesets) {
            Iterator<?> it = fileset.iterator();
            while (it.hasNext()) {
                File f = ((FileResource) it.next()).getFile();
                try {
                    processFile(f);
                } catch (IOException e) {
                    throw new BuildException("Error while writing " + f, e);
                }
            }
        }
    }

    private static StringBuilder textFromFile(File f) {
        if (!f.exists()) {
            throw new BuildException("File not found: " + f);
        }

        StringBuilder sb = new StringBuilder();
        Reader reader = null;
        char[] buffer = new char[1000];
        try {
            reader = new FileReader(f);
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                sb.append(new String(buffer, 0, read));
            }
            return sb;
        } catch (Exception e) {
            throw new BuildException("Error while reading" + f, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void processFile(File f) throws IOException {

        StringBuilder content = textFromFile(f);

        if (content.charAt(0) == '/' && content.charAt(1) == '*') {
            int commentEnd = content.indexOf("*/", 2);
            content.replace(0, commentEnd + 2, licenseText);
        } else {
            content.insert(0, "\n").insert(0, licenseText);
        }

        FileWriter fw = new FileWriter(f);
        try {
            fw.append(content);
        } finally {
            fw.close();
        }
    }

    //
    // setter for Ant attributes and subelements.
    // 

    public void addFileSet(FileSet fileset) {
        if (!filesets.contains(fileset)) {
            filesets.add(fileset);
        }
    }

    public void setFile(String licenseFilename) {
        this.licenseFilename = licenseFilename;
    }

    public void setText(String licenseText) {
        this.licenseText = licenseText;
    }

}