package com.shie1d.viewtrick.compiler;

import com.google.auto.service.AutoService;
import com.shie1d.viewtrick.annos.BindView;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by shenli on 2017/6/29.
 */
@AutoService(Processor.class)
public class ViewTrickProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Filer mFiler;
    private Messager mMessager;
    private Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
        mTypeUtils = processingEnv.getTypeUtils();
    }

    private LinkedHashMap<String, BindViewContainer> containers;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            containers = groupFieldsByContainer(roundEnv);
            for (BindViewContainer container :
                    containers.values()) {
                container.generateCode(mFiler, mElementUtils, mTypeUtils);
            }

        } catch (IllegalArgumentException e) {
            err(e.getMessage());
        } catch (IOException e) {
            err(e.getMessage());
        } catch (ClassNotFoundException e) {
            err(e.getMessage());
        }
        return true;
    }

    private LinkedHashMap<String, BindViewContainer> groupFieldsByContainer(RoundEnvironment roundEnv) throws IllegalArgumentException {
        if (containers == null) {
            containers = new LinkedHashMap<>();
        } else {
            containers.clear();
        }

        Set<? extends Element> boundElements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element boundElement : boundElements) {
            String fieldName = boundElement.getSimpleName().toString();
            if (boundElement.getKind() != ElementKind.FIELD) {
                err("Annotation @BindView use in wrong target : " + fieldName);
                throw new IllegalArgumentException("Annotation @BindView use in wrong target : " + fieldName);
            }
            if (boundElement.getModifiers().contains(Modifier.PRIVATE)
                    || boundElement.getModifiers().contains(Modifier.STATIC)
                    || boundElement.getModifiers().contains(Modifier.FINAL)) {
                err(boundElement.getSimpleName().toString() + " bound by @BindView can't be private nor static nor final");
                throw new IllegalArgumentException(boundElement.getSimpleName().toString() + " bound by @BindView can't be private nor static nor final");
            }
            //Get field type element
            TypeMirror typeMirror = boundElement.asType();
            TypeElement curType = (TypeElement) mTypeUtils.asElement(typeMirror);
            String curClzName = curType.getQualifiedName().toString();
            String targetClzName = "android.view.View";
            while (true) {
                if (targetClzName.equals(curClzName)) {
                    break;
                }
                TypeMirror superclass = curType.getSuperclass();
                if (superclass == null || superclass.getKind() == TypeKind.NONE) {
                    err(boundElement.getSimpleName().toString() + " should be a child of android.view.View");
                    throw new IllegalArgumentException(boundElement.getSimpleName().toString() + " should be a child of android.view.View");
                }
                curType = (TypeElement) mTypeUtils.asElement(superclass);
                curClzName = curType.getQualifiedName().toString();
            }

            TypeElement containerElement = (TypeElement) boundElement.getEnclosingElement();
            if (containerElement.getModifiers().contains(Modifier.ABSTRACT)
                    || containerElement.getKind() == ElementKind.INTERFACE) {
                err("@BindView shouldn't used in abstract clz nor interface");
                throw new IllegalArgumentException("@BindView shouldn't used in abstract clz nor interface");
            }
            String containerName = containerElement.getQualifiedName().toString();
            BindViewField bindViewField = new BindViewField((VariableElement) boundElement);
            BindViewContainer bindViewContainer = containers.get(containerName);
            if (bindViewContainer == null) {
                bindViewContainer = new BindViewContainer(containerName);
                containers.put(containerName, bindViewContainer);
            }
            bindViewContainer.put(bindViewField);
        }


        return containers;
    }

//    private boolean isInvalidType(String fieldName, TypeElement clarifyType) {
//        if (clarifyType.getModifiers().contains(Modifier.PRIVATE)
//                || clarifyType.getModifiers().contains(Modifier.STATIC)
//                || clarifyType.getModifiers().contains(Modifier.FINAL)) {
//            err(fieldName + " can't be announced by private nor static nor final");
//            return true;
//        }
//        String curClassName = clarifyType.getQualifiedName().toString();
//        String targetClzName = "android.view.View";
//        while (true) {
//            if (targetClzName.equals(curClassName)) {
//
//            }
//        }
//        return true;
//
//    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        return annotations;
    }

    public void msg(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public void err(String msg) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, msg);
    }
}
