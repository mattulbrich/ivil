package de.uka.iti.ivil.jbc.util;


/**
 * This class is used to build and revert Program names from/to
 * (Package,Class,Function)
 * 
 * @author timm.felden@felden.com
 * 
 */
final class EscapeProgram {
    
    /**
     * Info obtained from a program string, all fields are in escaped form.
     * 
     * @author timm.felden@felden.com
     */
    static class Info {
        final public String pack[];
        final public String className;
        final public String methodName;
        final public String[] signature;

        public Info(String pack[], String className, String methodName, String[] signature) {
            this.pack = pack;
            this.className = className;
            this.methodName = methodName;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Info))
                return false;

            Info i = (Info) o;
            return i.pack.equals(pack) && i.className.equals(className) && i.methodName.equals(methodName)
                    && i.signature.equals(signature);
        }

        @Override
        public int hashCode() {
            return pack.hashCode() + className.hashCode() + methodName.hashCode() + signature.hashCode();
        }
    }

    /**
     * The build method does not care, if the package information is present in
     * the class name or the package string. If you have a fully qualified class
     * name, pass the empty string as package and everything will work fine.
     * 
     * @param signature
     *            musst have length >= 1
     */
    static String build(String pack, String className, String functionName, String[] signature) {
        StringBuilder sb = new StringBuilder(pack.replace('.', '_'));
        if (!pack.isEmpty())
            sb.append("_");
        sb.append(className.replace('.', '_')).append("_").append(functionName);
        for (String s : signature)
            sb.append("__").append(s);
        return sb.toString();
    }

    static Info revert(String programName) {
        String className;
        String functionName;
        String[] signature;
        String[] pack;

        String[] parts = programName.split("__");
        signature = new String[parts.length - 1];
        for (int i = 0; i < signature.length; i++)
            signature[i] = parts[i + 1];

        parts = parts[0].split("_");
        pack = new String[parts.length - 2];
        for (int i = 0; i < parts.length - 2; i++)
            pack[i] = parts[i];
        className = parts[parts.length - 2];
        functionName = parts[parts.length - 1];

        return new Info(pack, className, functionName, signature);
    }
}
