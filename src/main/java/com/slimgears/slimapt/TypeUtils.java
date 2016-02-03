package com.slimgears.slimapt;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

/**
 * Created by Denis on 08-May-15.
 *
 */
public class TypeUtils {
    private final static Map<TypeName, Object> DEFAULT_VALUES = ImmutableMap.<TypeName, Object>builder()
            .put(TypeName.BOOLEAN, false)
            .put(TypeName.BYTE, (byte) 0)
            .put(TypeName.CHAR, '\0')
            .put(TypeName.DOUBLE, 0.0)
            .put(TypeName.FLOAT, 0.0f)
            .put(TypeName.INT, 0)
            .put(TypeName.LONG, 0L)
            .put(TypeName.SHORT, 0)
            .build();

    private final static Map<TypeName, Class> BOXED_TYPES = ImmutableMap.<TypeName, Class>builder()
            .put(TypeName.BOOLEAN, Boolean.class)
            .put(TypeName.BYTE, Byte.class)
            .put(TypeName.CHAR, Character.class)
            .put(TypeName.DOUBLE, Double.class)
            .put(TypeName.FLOAT, Float.class)
            .put(TypeName.INT, Integer.class)
            .put(TypeName.LONG, Long.class)
            .put(TypeName.SHORT, Short.class)
            .build();

    private final static Map<TypeName, TypeName> UNBOXED_TYPES = buildUnboxedTypes();

    public static Object defaultValue(TypeName typeName) {
        return DEFAULT_VALUES.getOrDefault(typeName, null);
    }

    public static String packageName(String qualifiedClassName) {
        int pos = qualifiedClassName.lastIndexOf('.');
        return (pos >= 0) ? qualifiedClassName.substring(0, pos) : "";
    }

    public static String simpleName(String qualifiedClassName) {
        String packageName = packageName(qualifiedClassName);
        return packageName.isEmpty() ? qualifiedClassName : qualifiedClassName.substring(packageName.length() + 1);
    }

    public static String qualifiedName(String packageName, String simpleName) {
        return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
    }

    public static TypeName getTypeName(TypeElement element) {
        return TypeName.get(element.asType());
    }

    public static String qualifiedName(TypeElement element) {
        if (element.getEnclosingElement() instanceof TypeElement) {
            return qualifiedName((TypeElement)element.getEnclosingElement()) + '$' + element.getSimpleName();
        }
        return element.getQualifiedName().toString();
    }

    public static ClassName getClassName(String qualifiedClassName) {
        return ClassName.get(packageName(qualifiedClassName), simpleName(qualifiedClassName));
    }

    public static String toCamelCase(String begin, String... parts) {
        String name = begin.length() > 0
                ? Character.toLowerCase(begin.charAt(0)) + begin.substring(1)
                : begin;

        for (String part : parts) {
            name += Character.toUpperCase(part.charAt(0)) + part.substring(1);
        }
        return name;
    }

    public interface AnnotationTypesGetter<TAnnotation extends Annotation> {
        Class[] getTypes(TAnnotation annotation) throws MirroredTypesException;
    }

    public interface AnnotationTypeGetter<TAnnotation extends Annotation> {
        Class getType(TAnnotation annotation) throws MirroredTypeException;
    }

    public static <TAnnotation extends Annotation> TypeName getTypeFromAnnotation(TAnnotation annotation, AnnotationTypeGetter<TAnnotation> getter) {
        try {
            return TypeName.get(getter.getType(annotation));
        } catch (MirroredTypeException e) {
            return TypeName.get(e.getTypeMirror());
        }
    }

    public static <TAnnotation extends Annotation> Collection<TypeName> getTypesFromAnnotation(TAnnotation annotation, AnnotationTypesGetter<TAnnotation> getter) {
        try {
            return Collections2.transform(Arrays.asList(getter.getTypes(annotation)), TypeName::get);
        } catch (MirroredTypesException e) {
            return Collections2.transform(e.getTypeMirrors(), TypeName::get);
        }
    }

    public static TypeName getTypeName(final TypeMirror typeMirror) {
        try {
            return TypeName.get(typeMirror);
        } catch (Exception e) {
            return ClassName.get(TypeUtils.packageName(typeMirror.toString()), TypeUtils.simpleName(typeMirror.toString()));
        }
    }

    public static TypeName getTypeName(TypeMirror typeMirror, String defaultPackageName) {
        String typePackage = TypeUtils.packageName(typeMirror.toString());
        if (typePackage.isEmpty()) typePackage = defaultPackageName;

        try {
            return TypeName.get(typeMirror);
        } catch (Exception e) {
            return ClassName.get(typePackage, TypeUtils.simpleName(typeMirror.toString()));
        }
    }

    public static TypeName unbox(TypeName type) {
        return UNBOXED_TYPES.getOrDefault(type, type);
    }

    public static TypeName box(TypeName type) {
        return  BOXED_TYPES.containsKey(type)
                ? TypeName.get(BOXED_TYPES.get(type))
                : type;
    }

    private static Map<TypeName, TypeName> buildUnboxedTypes() {
        ImmutableMap.Builder<TypeName, TypeName> builder = ImmutableMap.<TypeName, TypeName>builder();
        for (Map.Entry<TypeName, Class> entry : BOXED_TYPES.entrySet()) {
            builder.put(TypeName.get(entry.getValue()), entry.getKey());
        }
        return builder.build();
    }
}
