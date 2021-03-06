/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */



package jdk.tools.jaotc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;

import jdk.vm.ci.meta.ResolvedJavaMethod;

/**
 * A class encapsulating any user-specified compilation restrictions.
 */
final class CompilationSpec {

    /**
     * Set of method names to restrict compilation to.
     */
    private HashSet<String> compileOnlyStrings = new HashSet<>();
    private HashSet<Pattern> compileOnlyPatterns = new HashSet<>();

    /**
     * Set of method names that should be excluded from compilation.
     */
    private HashSet<String> excludeStrings = new HashSet<>();
    private HashSet<Pattern> excludePatterns = new HashSet<>();

    /**
     * Add a {@code compileOnly} directive to the compile-only list.
     *
     * @param pattern regex or non-regex pattern string
     */
    void addCompileOnlyPattern(String pattern) {
        if (pattern.contains("*")) {
            compileOnlyPatterns.add(Pattern.compile(pattern));
        } else {
            compileOnlyStrings.add(pattern);
        }
    }

    /**
     * Add an {@code exclude} directive to the exclude list.
     *
     * @param pattern regex or non-regex pattern string
     */
    void addExcludePattern(String pattern) {
        if (pattern.contains("*")) {
            excludePatterns.add(Pattern.compile(pattern));
        } else {
            excludeStrings.add(pattern);
        }
    }

    /**
     * Check if a given method is part of a restrictive compilation.
     *
     * @param method method to be checked
     * @return true or false
     */
    boolean shouldCompileMethod(ResolvedJavaMethod method) {
        if (compileWithRestrictions()) {
            // If there are user-specified compileOnly patterns, default action
            // is not to compile the method.
            boolean compileMethod = compileOnlyStrings.isEmpty() && compileOnlyPatterns.isEmpty();

            // Check if the method matches with any of the specified compileOnly patterns.
            String methodName = JavaMethodInfo.uniqueMethodName(method);

            // compileOnly
            if (!compileMethod) {
                compileMethod = compileOnlyStrings.contains(methodName);
            }
            if (!compileMethod) {
                Iterator<Pattern> it = compileOnlyPatterns.iterator();
                while (!compileMethod && it.hasNext()) {
                    Pattern pattern = it.next();
                    compileMethod = pattern.matcher(methodName).matches();
                }
            }

            // exclude
            if (compileMethod) {
                compileMethod = !excludeStrings.contains(methodName);
            }
            if (compileMethod) {
                Iterator<Pattern> it = excludePatterns.iterator();
                while (compileMethod && it.hasNext()) {
                    Pattern pattern = it.next();
                    compileMethod = !(pattern.matcher(methodName).matches());
                }
            }
            return compileMethod;
        }
        return true;
    }

    /**
     * Return true if compilation restrictions are specified.
     */
    private boolean compileWithRestrictions() {
        return !(compileOnlyStrings.isEmpty() && compileOnlyPatterns.isEmpty() && excludeStrings.isEmpty() && excludePatterns.isEmpty());
    }

}
