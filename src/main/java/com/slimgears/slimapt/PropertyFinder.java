// Copyright 2015 Denis Itskovich
// Refer to LICENSE.txt for license details
package com.slimgears.slimapt;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;

/**
 * Created by ditskovi on 1/28/2016.
 *
 */
public class PropertyFinder extends ElementVisitorBase<Void, Void> {
    private final Map<String, PropertyDescriptor> properties = new HashMap<>();
    private final Elements elementUtils;

    public PropertyFinder(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    public class PropertyDescriptor {
        String name;
        ExecutableElement getter;
        ExecutableElement setter;

        public boolean isValid() {
            return getter != null && setter != null;
        }

        public String getName() {
            return TypeUtils.toCamelCase(name);
        }

        public GetterSetterPropertyInfo createPropertyInfo() {
            return new GetterSetterPropertyInfo(elementUtils, getName(), getter, setter);
        }
    }

    @Override
    public Void visitExecutable(ExecutableElement element, Void param) {
        String name = element.getSimpleName().toString();
        if (name.startsWith("get")) {
            visitGetter(element, name.substring(3));
        } else if (name.startsWith("is")) {
            visitGetter(element, name.substring(2));
        } else if (name.startsWith("set")) {
            visitSetter(element, name.substring(3));
        }
        return null;
    }

    public Collection<PropertyDescriptor> getProperties() {
        return Stream.of(properties.values())
                .filter(PropertyDescriptor::isValid)
                .collect(Collectors.toList());
    }

    private void visitGetter(ExecutableElement getter, String name) {
        PropertyDescriptor descriptor = getDescriptor(name);
        descriptor.getter = getter;
    }

    private void visitSetter(ExecutableElement setter, String name) {
        PropertyDescriptor descriptor = getDescriptor(name);
        descriptor.setter = setter;
    }

    private PropertyDescriptor getDescriptor(String name) {
        PropertyDescriptor descriptor = properties.getOrDefault(name, null);
        if (descriptor == null) {
            descriptor = new PropertyDescriptor();
            descriptor.name = name;
            properties.put(name, descriptor);
        }
        return descriptor;
    }
}
