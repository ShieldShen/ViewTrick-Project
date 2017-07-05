package com.shie1d.viewtrick.compiler;

import com.shie1d.viewtrick.annos.BindView;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;

/**
 * Created by shenli on 2017/7/5.
 */

public class BindViewField {
    public VariableElement element;
    public int resId;

    @Override
    public String toString() {
        return "BindViewField{" +
                "element=" + element +
                ", resId=" + resId +
                '}';
    }

    public BindViewField(VariableElement element) {
        this.element = element;
        String fieldName = element.getSimpleName().toString();
        BindView annotation = element.getAnnotation(BindView.class);
        if (annotation == null || annotation.value() == 0) {
            throw new IllegalArgumentException(fieldName + " don't has a @BindView annotation nor value of annotation is missing");
        }
        resId = annotation.value();
    }
}
