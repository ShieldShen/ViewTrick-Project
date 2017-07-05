package com.shie1d.viewtrick.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by shenli on 2017/7/5.
 */

public class BindViewContainer {
    private LinkedHashMap<String, BindViewField> boundFields;
    private String containerName;

    public BindViewContainer(String containerName) {
        this.containerName = containerName;
        boundFields = new LinkedHashMap<>();
    }

    public void put(BindViewField field) throws IllegalArgumentException {
        String fieldName = field.element.getSimpleName().toString();
        BindViewField bindViewField = boundFields.get(fieldName);
        if (bindViewField != null) {
            throw new IllegalArgumentException("Duplicate name " + fieldName + " used in " + containerName);
        }
        boundFields.put(fieldName, field);
    }

    public void generateCode(Filer filer, Elements elementUtils, Types typeUtils) throws IOException, ClassNotFoundException {
        TypeElement containerElement = elementUtils.getTypeElement(containerName);
        PackageElement packageElement = elementUtils.getPackageOf(containerElement);
        String packageName = packageElement.getQualifiedName().toString();
        String[] split = containerName.split("\\.");
        String clzName = split[split.length - 1];


        String paramView = "view";
        String paramTarget = "target";
        String typeT = "T";
        TypeVariableName genericType = TypeVariableName.get(typeT, TypeName.get(containerElement.asType()));
        ParameterSpec constructorParamView =
                ParameterSpec.builder(
                        TypeName.get(elementUtils.getTypeElement("android.view.View").asType()), paramView
                ).build();
        ParameterSpec constructorParamTarget =
                ParameterSpec.builder(
                        genericType, paramTarget
                ).build();


        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(constructorParamView)
                .addParameter(constructorParamTarget);
        for (BindViewField field :
                boundFields.values()) {
            builder.addStatement("$N.$N = ($T)$N.findViewById(" + field.resId + ")", paramTarget, field.element.getSimpleName(), ClassName.get(field.element.asType()), paramView);
        }
        MethodSpec constructor = builder.build();
        TypeSpec typeSpec = TypeSpec.classBuilder(clzName + "$$ViewBinder")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(genericType)
                .addMethod(constructor)
                .build();
        JavaFile.Builder fileBuilder = JavaFile.builder(packageName, typeSpec);
        fileBuilder.build().writeTo(filer);
    }

    @Override
    public String toString() {
        return "BindViewContainer{" +
                "boundFields=" + boundFields +
                ", containerName='" + containerName + '\'' +
                '}';
    }
}
