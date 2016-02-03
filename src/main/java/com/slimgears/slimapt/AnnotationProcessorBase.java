// Copyright 2015 Denis Itskovich
// Refer to LICENSE.txt for license details
package com.slimgears.slimapt;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by Denis on 04-Apr-15
 * <File Description>
 */
public abstract class AnnotationProcessorBase extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    protected boolean processAnnotation(TypeElement annotationType, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotationType);
        for (Element element : annotatedElements) {
            try {
                if (element instanceof TypeElement && ! processType((TypeElement)element)) {
                    return false;
                }
                else if (element instanceof ExecutableElement && !processMethod((ExecutableElement)element)) {
                    return false;
                } else if (element instanceof VariableElement && !processField((VariableElement)element)) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    protected boolean processType(TypeElement typeElement) throws IOException { return false; }
    protected boolean processMethod(ExecutableElement methodElement) throws IOException { return false; }
    protected boolean processField(VariableElement variableElement) throws IOException { return false; }

    protected void writeType(String packageName, TypeSpec type) throws IOException {
        JavaFile javaFile = JavaFile
                .builder(packageName, type)
                .indent("    ")
                .build();
        javaFile.writeTo(processingEnv.getFiler());
    }
}
