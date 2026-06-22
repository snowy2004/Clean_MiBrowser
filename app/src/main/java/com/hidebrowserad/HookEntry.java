package com.hidebrowserad;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static final int ACTION_CHAT_GPT_ID = 0x7f0b0060;
    private static final int PROFILE_MENU_GAME_LIST_ID = 0x7f0b0b74;
    private static final int FLOAT_ENTRY_IMAGE_ID = 0x7f0b05b7;
    private static final int BTN_HOME_ID = 0x7f0b023a;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"com.mi.globalbrowser".equals(lpparam.packageName)) {
            return;
        }

        hookApplication(lpparam);
        hookSharedPreferences(lpparam);
        hookViewSetVisibility(lpparam);
        hookViewDraw(lpparam);
        hookViewGroupAddView(lpparam);
        hookViewClick(lpparam);
        hookFragment(lpparam);
        hookActivityLifeCycle(lpparam);
        hookHalfH5AdDialog(lpparam);
    }

    private void hookApplication(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> appClass = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(appClass, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context ctx = (Context) param.args[0];
                    try {
                        android.content.SharedPreferences.Editor editor = ctx.getSharedPreferences("com.mi.globalbrowser_preferences", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("key_new_homepage_simple", true);
                        editor.putBoolean("pref_new_home_game_channel_switch", false);
                        editor.putBoolean("pref_force_show_ads", false);
                        editor.putBoolean("recommendation_app_quicklink", false);
                        editor.commit();
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception ignored) {}
    }

    private void hookSharedPreferences(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> spClass = XposedHelpers.findClass("android.app.SharedPreferencesImpl", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(spClass, "getBoolean", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if ("key_new_homepage_simple".equals(key)) {
                        param.setResult(true);
                    } else if ("pref_new_home_game_channel_switch".equals(key)
                            || "pref_force_show_ads".equals(key)
                            || "recommendation_app_quicklink".equals(key)) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void hookViewSetVisibility(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> viewClass = XposedHelpers.findClass("android.view.View", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(viewClass, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View v = (View) param.thisObject;
                    int id = v.getId();
                    int newVis = (int) param.args[0];
                    if (newVis != View.GONE) {
                        if (id == ACTION_CHAT_GPT_ID || id == FLOAT_ENTRY_IMAGE_ID || id == PROFILE_MENU_GAME_LIST_ID) {
                            param.args[0] = View.GONE;
                            if (id == FLOAT_ENTRY_IMAGE_ID) {
                                hideParent(v);
                            }
                        }
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void hookViewDraw(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> viewClass = XposedHelpers.findClass("android.view.View", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(viewClass, "draw", android.graphics.Canvas.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View v = (View) param.thisObject;
                    int id = v.getId();
                    if (id == ACTION_CHAT_GPT_ID) {
                        v.setVisibility(View.GONE);
                        setZeroSize(v);
                        param.setResult(null);
                    } else if (id == FLOAT_ENTRY_IMAGE_ID) {
                        v.setVisibility(View.GONE);
                        hideParent(v);
                        param.setResult(null);
                    } else if (id == PROFILE_MENU_GAME_LIST_ID) {
                        v.setVisibility(View.GONE);
                        setZeroSize(v);
                        param.setResult(null);
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void hookViewGroupAddView(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> vgClass = XposedHelpers.findClass("android.view.ViewGroup", lpparam.classLoader);
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View child = (View) param.args[0];
                    if (child == null) return;
                    int id = child.getId();
                    if (id == ACTION_CHAT_GPT_ID || id == FLOAT_ENTRY_IMAGE_ID || id == PROFILE_MENU_GAME_LIST_ID) {
                        child.setVisibility(View.GONE);
                        setZeroSize(child);
                        if (id == FLOAT_ENTRY_IMAGE_ID) {
                            hideParent(child);
                        }
                    }
                }
            };
            XposedHelpers.findAndHookMethod(vgClass, "addView", View.class, hook);
            XposedHelpers.findAndHookMethod(vgClass, "addView", View.class, int.class, hook);
            XposedHelpers.findAndHookMethod(vgClass, "addView", View.class, int.class, ViewGroup.LayoutParams.class, hook);
            XposedHelpers.findAndHookMethod(vgClass, "addView", View.class, ViewGroup.LayoutParams.class, hook);
        } catch (Exception ignored) {}
    }

    private void hookFragment(XC_LoadPackage.LoadPackageParam lpparam) {
        hookFragmentMethod(lpparam, "androidx.fragment.app.Fragment", "onViewCreated");
        hookFragmentMethod(lpparam, "android.app.Fragment", "onViewCreated");
        hookFragmentMethod(lpparam, "androidx.fragment.app.Fragment", "onResume");
        hookFragmentMethod(lpparam, "android.app.Fragment", "onResume");
    }

    private void hookFragmentMethod(XC_LoadPackage.LoadPackageParam lpparam, String className, String methodName) {
        try {
            Class<?> cls = XposedHelpers.findClass(className, lpparam.classLoader);
            if ("onViewCreated".equals(methodName)) {
                XposedHelpers.findAndHookMethod(cls, methodName, View.class, android.os.Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        scanViewTree((View) param.args[0]);
                    }
                });
            } else {
                XposedHelpers.findAndHookMethod(cls, methodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View view = (View) XposedHelpers.getObjectField(param.thisObject, "mView");
                        scanViewTree(view);
                    }
                });
            }
        } catch (Exception ignored) {}
    }

    private void hookViewClick(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> viewClass = XposedHelpers.findClass("android.view.View", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(viewClass, "performClick", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    View v = (View) param.thisObject;
                    if (v.getId() == BTN_HOME_ID) {
                        View parent = v.getRootView();
                        if (parent != null) {
                            scanViewTree(parent);
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    scanViewTree(v.getRootView());
                                }
                            }, 100);
                            v.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    scanViewTree(v.getRootView());
                                }
                            }, 300);
                        }
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    private void hookActivityLifeCycle(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> actClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);

            XposedHelpers.findAndHookMethod(actClass, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Activity act = (android.app.Activity) param.thisObject;
                    scanViewTree(act.getWindow().getDecorView());
                }
            });

            XposedHelpers.findAndHookMethod(actClass, "onPostResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Activity act = (android.app.Activity) param.thisObject;
                    scanViewTree(act.getWindow().getDecorView());
                }
            });
        } catch (Exception ignored) {}
    }

    private void hookHalfH5AdDialog(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> cls = XposedHelpers.findClass("com.android.browser.nativead.HalfH5AdDialog", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(cls, "show", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(null);
                }
            });
        } catch (Exception ignored) {}
    }

    private void scanViewTree(View root) {
        if (root == null) return;
        int id = root.getId();
        if (id == ACTION_CHAT_GPT_ID) {
            setZeroSize(root);
            root.setVisibility(View.GONE);
        } else if (id == FLOAT_ENTRY_IMAGE_ID) {
            root.setVisibility(View.GONE);
            hideParent(root);
        } else if (id == PROFILE_MENU_GAME_LIST_ID) {
            setZeroSize(root);
            root.setVisibility(View.GONE);
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                scanViewTree(vg.getChildAt(i));
            }
        }
    }

    private void setZeroSize(View v) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp != null) {
            lp.width = 0;
            lp.height = 0;
            v.setLayoutParams(lp);
        }
    }

    private void hideParent(View v) {
        ViewGroup parent = (ViewGroup) v.getParent();
        if (parent != null) {
            ViewGroup.LayoutParams lp = parent.getLayoutParams();
            if (lp != null) {
                lp.width = 0;
                lp.height = 0;
                parent.setLayoutParams(lp);
            }
            parent.setAlpha(0f);
            parent.setVisibility(View.GONE);
        }
    }
}
