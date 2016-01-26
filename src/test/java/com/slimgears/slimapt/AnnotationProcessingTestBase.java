// Copyright 2015 Denis Itskovich
// Refer to LICENSE.txt for license details
package com.slimgears.slimapt;

import com.google.common.collect.Iterables;
import com.google.testing.compile.JavaFileObjects;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.JavaFileObject;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Arrays.asList;

/**
 * Created by ditskovi on 1/25/2016.
 *
 */
public class AnnotationProcessingTestBase {
    protected static Iterable<JavaFileObject> inputFiles(String... files) {
        return fromResources("input", files);
    }

    protected static Iterable<JavaFileObject> expectedFiles(String... files) {
        return fromResources("output", files);
    }

    protected static Iterable<JavaFileObject> fromResources(final String path, String[] files) {
        return transform(asList(files), input -> JavaFileObjects.forResource(path + '/' + input));
    }

    protected static Iterable<AbstractProcessor> processedWith(AbstractProcessor... processors) {
        return asList(processors);
    }

    protected void testAnnotationProcessing(Iterable<AbstractProcessor> processor, Iterable<JavaFileObject> inputs, Iterable<JavaFileObject> expectedOutputs) {
        assert_()
                .about(javaSources())
                .that(inputs)
                .processedWith(processor)
                .compilesWithoutError()
                .and().generatesSources(Iterables.getFirst(expectedOutputs, null), toArray(skip(expectedOutputs, 1), JavaFileObject.class));
    }
}
