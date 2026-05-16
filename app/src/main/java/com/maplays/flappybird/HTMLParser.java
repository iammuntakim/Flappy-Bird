package com.maplays.flappybird;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.io.File;
import java.io.FileInputStream;

public class HTMLParser extends Activity {
    private WebView webView;
    private FrameLayout rootLayout;
    private ImageView splashIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE 
                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 
                      | View.SYSTEM_UI_FLAG_FULLSCREEN 
                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            
            window.getDecorView().setSystemUiVisibility(flags);
        }

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.WHITE);

        splashIcon = new ImageView(this);
        splashIcon.setImageDrawable(getPackageManager().getApplicationIcon(getApplicationInfo()));
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        iconParams.gravity = Gravity.CENTER;
        splashIcon.setLayoutParams(iconParams);
        rootLayout.addView(splashIcon);

        webView = new WebView(this);
        webView.setFitsSystemWindows(false);
        webView.setVisibility(View.GONE);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                splashIcon.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("file:///")) {
                    String filePath = url.substring(7);
                    if (filePath.contains("?")) {
                        filePath = filePath.substring(0, filePath.indexOf("?"));
                    }
                    if (filePath.contains("#")) {
                        filePath = filePath.substring(0, filePath.indexOf("#"));
                    }
                    File file = new File(filePath);
                    if (!file.exists()) {
                        File mappedFile = new File(file.getParent(), "files/" + file.getName());
                        if (mappedFile.exists()) {
                            file = mappedFile;
                        }
                    }
                    if (file.exists()) {
                        try {
                            String mimeType = "application/octet-stream";
                            String name = file.getName().toLowerCase();
                            if (name.endsWith(".css")) {
                                mimeType = "text/css";
                            } else if (name.endsWith(".js")) {
                                mimeType = "application/javascript";
                            } else if (name.endsWith(".html") || name.endsWith(".htm")) {
                                mimeType = "text/html";
                            } else if (name.endsWith(".png")) {
                                mimeType = "image/png";
                            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                                mimeType = "image/jpeg";
                            } else if (name.endsWith(".gif")) {
                                mimeType = "image/gif";
                            } else if (name.endsWith(".svg")) {
                                mimeType = "image/svg+xml";
                            } else if (name.endsWith(".woff")) {
                                mimeType = "font/woff";
                            } else if (name.endsWith(".woff2")) {
                                mimeType = "font/woff2";
                            } else if (name.endsWith(".ttf")) {
                                mimeType = "font/ttf";
                            } else if (name.endsWith(".mp3")) {
                                mimeType = "audio/mpeg";
                            } else if (name.endsWith(".mp4")) {
                                mimeType = "video/mp4";
                            }
                            FileInputStream stream = new FileInputStream(file);
                            return new WebResourceResponse(mimeType, "UTF-8", stream);
                        } catch (Exception e) {
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        rootLayout.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        setContentView(rootLayout);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String libDir = getApplicationInfo().nativeLibraryDir;
                String executablePath = libDir + "/libkdio.so";
                String archivePath = libDir + "/libannoation.so";
                
                final File cacheDir = new File(getCacheDir(), "unpacked_html");
                final String cachePath = cacheDir.getAbsolutePath();

                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                File propFile = new File(cachePath, "META-INF/KNFO.PROP");
                File manifestFile = new File(cachePath, "manifest.pb");

                if (!propFile.exists() || !manifestFile.exists()) {
                    try {
                        Process chmodProcess = Runtime.getRuntime().exec(new String[]{"chmod", "755", executablePath});
                        chmodProcess.waitFor();

                        Process execProcess = Runtime.getRuntime().exec(new String[]{
                            executablePath, "-d", archivePath, cachePath
                        });
                        execProcess.waitFor();
                    } catch (Exception e) {
                    }
                }

                if (propFile.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(propFile);
                        byte[] data = new byte[(int) propFile.length()];
                        fis.read(data);
                        fis.close();
                        String propContent = new String(data, "UTF-8");
                        if (propContent.contains("orientation=landscape")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                }
                            });
                        } else if (propContent.contains("orientation=portrait")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                }
                            });
                        }
                    } catch (Exception e) {
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.addJavascriptInterface(new WebAppInterface(cachePath), "AndroidInterface");
                        
                        String mainFileName = "annoation.html";
                        String archiveName = new File(archivePath).getName();
                        if (archiveName.startsWith("lib") && archiveName.endsWith(".so")) {
                            String baseName = archiveName.substring(3, archiveName.length() - 3);
                            mainFileName = baseName + ".html";
                        }
                        
                        File mainFile = new File(cachePath, mainFileName);
                        if (!mainFile.exists()) {
                            mainFile = new File(cachePath, "files/" + mainFileName);
                        }
                        if (!mainFile.exists()) {
                            mainFile = new File(cachePath, "file.html");
                        }
                        if (!mainFile.exists()) {
                            mainFile = new File(cachePath, "files/file.html");
                        }
                        
                        webView.loadUrl("file://" + mainFile.getAbsolutePath());
                    }
                });
            }
        }).start();
    }

    public class WebAppInterface {
        private String basePath;

        public WebAppInterface(String basePath) {
            this.basePath = basePath;
        }

        @JavascriptInterface
        public void switchPage(final String pageName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    File targetFile = new File(basePath, pageName);
                    if (!targetFile.exists()) {
                        targetFile = new File(basePath, "files/" + pageName);
                    }
                    webView.loadUrl("file://" + targetFile.getAbsolutePath());
                }
            });
        }
    }
}