package com.shi1d.viewtrick;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by shenli on 2017/7/5.
 */

public class ViewTrick {
    public static void bind(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        try {
            Class<?> clz = Class.forName(activity.getClass().getCanonicalName() + "$$ViewBinder");
            Constructor<?> constructor = clz.getConstructor(View.class, activity.getClass());
            constructor.newInstance(decorView, activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
