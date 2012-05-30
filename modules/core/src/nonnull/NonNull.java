/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * The annotation <code>NonNull</code> can be used to annotate methods or
 * their parameters (of reference type) to denote that the may not be null.
 * 
 * The annotations are documented in JavaDoc.
 * 
 * You can patch class files containing NonNull annotations with runtime checks
 * using the class {@link NonNullPatch}
 * 
 * You can use the classloader {@link NonNullClassLoader} to patch class files
 * dynamically at run time.
 */

public
  @Documented 
  @Retention(RetentionPolicy.CLASS) 
  
@interface NonNull {

}
